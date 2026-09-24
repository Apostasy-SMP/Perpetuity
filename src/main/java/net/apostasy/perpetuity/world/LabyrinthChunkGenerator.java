package net.apostasy.perpetuity.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.apostasy.perpetuity.Perpetuity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Chunk generator for the infinite labyrinth. Each chunk starts as a stone plateau with a grass
 * surface; authored templates then carve the corridors and rooms through it by placing their air
 * as well as their visible blocks.
 *
 * <p>{@link LabyrinthMaze} answers per cell which sides carry a corridor, from the cell's own
 * coordinates, so neighbouring chunks always agree. {@link LabyrinthBranches} fills each ring's
 * interior; that is a search rather than a formula, so its result is cached per ring.
 *
 * <p>Everything is clipped to the chunk being generated, and a piece straddling a boundary is
 * stamped identically from both sides - no cross-chunk writes, no ordering dependency.
 */
public class LabyrinthChunkGenerator extends ChunkGenerator {
	public static final MapCodec<LabyrinthChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(
					BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource)
			).apply(instance, LabyrinthChunkGenerator::new));

	/** Enough headroom for the 48-block cathedral and remnant templates. */
	public static final int WORLD_HEIGHT = 64;
	public static final int MIN_Y = 0;
	/** Solid plateau left between the corridors that the NBT templates carve out. */
	public static final int TERRAIN_TOP_Y = 24;

	/** Extra space around a ring's nominal interior available to branch pieces. */
	private static final int MARGIN = 4;
	private static final int PLAN_CACHE = 256;

	private volatile LabyrinthMaze maze;
	private volatile long mazeSeed;
	private volatile LabyrinthTileSet tiles;

	/** Ring layouts are a search, not a formula, so they are cached. */
	private final Map<Long, LabyrinthBranches.Plan> plans =
			java.util.Collections.synchronizedMap(new LinkedHashMap<>(PLAN_CACHE, 0.75f, true) {
				@Override
				protected boolean removeEldestEntry(Map.Entry<Long, LabyrinthBranches.Plan> eldest) {
					return size() > PLAN_CACHE;
				}
			});

	private final java.util.Set<String> validated = java.util.concurrent.ConcurrentHashMap.newKeySet();

	public LabyrinthChunkGenerator(BiomeSource biomeSource) {
		super(biomeSource);
	}

	@Override
	protected MapCodec<? extends ChunkGenerator> getCodec() {
		return CODEC;
	}

	// ------------------------------------------------------------- generation

	@Override
	public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor) {
		ChunkPos chunkPos = chunk.getPos();
		BlockBox chunkBox = new BlockBox(
				chunkPos.getStartX(), MIN_Y, chunkPos.getStartZ(),
				chunkPos.getEndX(), MIN_Y + WORLD_HEIGHT - 1, chunkPos.getEndZ());

		StructureTemplateManager templates = world.toServerWorld().getStructureTemplateManager();
		LabyrinthTileSet tileSet = tiles(templates);
		if (tileSet.cap() == null) return;

		LabyrinthMaze maze = maze(world.toServerWorld().getSeed());
		Random random = Random.create(chunkPos.toLong());

		// One cell of margin: corridor tiles fit their own cell, branch pieces reach a little into
		// their host's. Placements missing this chunk are rejected on their bounding box.
		int minCellX = Math.floorDiv(chunkPos.getStartX(), LabyrinthTileSet.CELL) - 1;
		int maxCellX = Math.floorDiv(chunkPos.getEndX(), LabyrinthTileSet.CELL) + 1;
		int minCellZ = Math.floorDiv(chunkPos.getStartZ(), LabyrinthTileSet.CELL) - 1;
		int maxCellZ = Math.floorDiv(chunkPos.getEndZ(), LabyrinthTileSet.CELL) + 1;

		for (int cellX = minCellX; cellX <= maxCellX; cellX++) {
			for (int cellZ = minCellZ; cellZ <= maxCellZ; cellZ++) {
				stampCorridor(world, templates, tileSet, maze, random, chunkBox, cellX, cellZ);
			}
		}

		for (int ringX = maze.ringX(minCellX); ringX <= maze.ringX(maxCellX); ringX++) {
			for (int ringZ = maze.ringZ(minCellZ); ringZ <= maze.ringZ(maxCellZ); ringZ++) {
				for (LabyrinthBranches.Placed piece : plan(maze, tileSet, ringX, ringZ).pieces()) {
					stamp(world, templates, tileSet, random, chunkBox,
							piece.shape(), piece.rotation(), new BlockPos(piece.x(), piece.y(), piece.z()));
				}
			}
		}
	}

	// ------------------------------------------------------------- the lattice

	private void stampCorridor(StructureWorldAccess world, StructureTemplateManager templates,
	                           LabyrinthTileSet tileSet, LabyrinthMaze maze, Random random,
	                           BlockBox chunkBox, int cellX, int cellZ) {
		int mask = maze.exitMask(cellX, cellZ);
		if (mask == 0) return;

		int inward = hostedDirection(maze, tileSet, cellX, cellZ);
		Object[] choice = inward >= 0
				? tileSet.host(mask, inward)
				: tileSet.corridor(mask, variant(cellX, cellZ));
		if (choice == null) {
			Perpetuity.LOGGER.warn("Labyrinth: no tile presents exits {} at cell {},{}", mask, cellX, cellZ);
			return;
		}

		PieceShape shape = (PieceShape) choice[0];
		int rotation = (Integer) choice[1];
		BlockPos origin = latticeOrigin(shape, rotation, mask, cellX, cellZ);
		if (origin == null) return;

		validateExits(shape, rotation, mask, cellX, cellZ, origin);
		stamp(world, templates, tileSet, random, chunkBox, shape, rotation, origin);
	}

	/** The direction this cell opens a branch in, or -1 if it is a plain corridor cell. */
	private int hostedDirection(LabyrinthMaze maze, LabyrinthTileSet tileSet, int cellX, int cellZ) {
		int ringX = maze.ringX(cellX);
		int ringZ = maze.ringZ(cellZ);
		for (LabyrinthBranches.Port port : plan(maze, tileSet, ringX, ringZ).accepted()) {
			if (port.hostCellX() == cellX && port.hostCellZ() == cellZ) return port.facing();
		}
		return -1;
	}

	/** Anchored on an exit rather than the bounding box, so it lands exactly on the cell boundary. */
	private BlockPos latticeOrigin(PieceShape shape, int rotation, int mask, int cellX, int cellZ) {
		int anchorMask = Integer.lowestOneBit(mask);
		int anchor = LabyrinthTileSet.facingOfMask(anchorMask);

		List<PieceShape.Conn> connectors = shape.connectors();
		for (int i = 0; i < connectors.size(); i++) {
			PieceShape.Conn c = connectors.get(i);
			if (!c.lattice() || PieceShape.rotateFacing(c.facing(), rotation) != anchor) continue;

			int[] origin = shape.originForConn(i, rotation,
					exitX(cellX, anchor), LabyrinthTileSet.PATH_Y, exitZ(cellZ, anchor));
			return new BlockPos(origin[0], origin[1], origin[2]);
		}
		Perpetuity.LOGGER.warn("Labyrinth: {} has no lattice exit facing {} under rotation {}",
				shape.id(), anchor, rotation);
		return null;
	}

	private static int exitX(int cellX, int facing) {
		if (facing == PieceShape.WEST) return cellX * LabyrinthTileSet.CELL;
		if (facing == PieceShape.EAST) return cellX * LabyrinthTileSet.CELL + LabyrinthTileSet.CELL - 1;
		return cellX * LabyrinthTileSet.CELL + LabyrinthTileSet.ARM;
	}

	private static int exitZ(int cellZ, int facing) {
		if (facing == PieceShape.NORTH) return cellZ * LabyrinthTileSet.CELL;
		if (facing == PieceShape.SOUTH) return cellZ * LabyrinthTileSet.CELL + LabyrinthTileSet.CELL - 1;
		return cellZ * LabyrinthTileSet.CELL + LabyrinthTileSet.ARM;
	}

	// ------------------------------------------------------------ the branches

	private LabyrinthBranches.Plan plan(LabyrinthMaze maze, LabyrinthTileSet tileSet, int ringX, int ringZ) {
		long key = ((long) ringX << 32) ^ (ringZ & 0xFFFFFFFFL);
		LabyrinthBranches.Plan cached = plans.get(key);
		if (cached != null) return cached;

		LabyrinthBranches.Plan built = buildPlan(maze, tileSet, ringX, ringZ);
		plans.put(key, built);
		return built;
	}

	private LabyrinthBranches.Plan buildPlan(LabyrinthMaze maze, LabyrinthTileSet tileSet, int ringX, int ringZ) {
		// Walls the branch engine must respect. A host swaps in a T tile of the same footprint.
		List<int[]> obstacles = new ArrayList<>();
		for (long[] cell : maze.ringCells(ringX, ringZ)) {
			int cellX = (int) cell[0];
			int cellZ = (int) cell[1];
			int mask = maze.exitMask(cellX, cellZ);
			if (mask == 0) continue;

			Object[] choice = tileSet.corridor(mask, variant(cellX, cellZ));
			if (choice == null) continue;
			PieceShape shape = (PieceShape) choice[0];
			int rotation = (Integer) choice[1];
			BlockPos origin = latticeOrigin(shape, rotation, mask, cellX, cellZ);
			if (origin != null) {
				obstacles.add(shape.box(rotation, origin.getX(), origin.getY(), origin.getZ()));
			}
		}

		int[] region = maze.pocketRegion(ringX, ringZ, LabyrinthTileSet.CELL, MARGIN,
				MIN_Y, MIN_Y + WORLD_HEIGHT - 1);

		List<LabyrinthBranches.Port> candidates = new ArrayList<>();
		for (LabyrinthMaze.Port port : maze.ports(ringX, ringZ)) {
			int facing = LabyrinthTileSet.facingOfMask(port.inward());
			candidates.add(new LabyrinthBranches.Port(
					exitX(port.cellX(), facing),
					LabyrinthTileSet.PATH_Y,
					exitZ(port.cellZ(), facing),
					facing, port.cellX(), port.cellZ()));
		}

		List<int[]> walls = obstacles;
		return LabyrinthBranches.build(mazeSeed, ringX, ringZ, region,
				tileSet.branchSet(), tileSet.cap(), candidates,
				box -> {
					for (int[] wall : walls) if (PieceShape.intersects(box, wall)) return true;
					return false;
				});
	}

	// --------------------------------------------------------------- stamping

	private void stamp(StructureWorldAccess world, StructureTemplateManager templates, LabyrinthTileSet tileSet,
	                   Random random, BlockBox chunkBox, PieceShape shape, int rotation, BlockPos origin) {
		int[] box = shape.box(rotation, origin.getX(), origin.getY(), origin.getZ());
		if (box[3] < chunkBox.getMinX() || box[0] > chunkBox.getMaxX()
				|| box[5] < chunkBox.getMinZ() || box[2] > chunkBox.getMaxZ()) {
			return;
		}

		StructureTemplate template = template(templates, tileSet, shape);
		if (template == null) return;

		BlockRotation blockRotation = LabyrinthTileSet.rotation(rotation);
		StructurePlacementData data = new StructurePlacementData()
				.setRotation(blockRotation)
				.setIgnoreEntities(true)
				.setUpdateNeighbors(false)
				.addProcessor(BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS)
				.setBoundingBox(chunkBox);

		template.place(world, origin, origin, data, random, Block.NOTIFY_LISTENERS);
		clearJigsawBlocks(world, template, origin, blockRotation, chunkBox);
	}

	private StructureTemplate template(StructureTemplateManager templates, LabyrinthTileSet tileSet,
	                                   PieceShape shape) {
		Optional<StructureTemplate> loaded = templates.getTemplate(tileSet.idOf(shape));
		if (loaded.isEmpty()) {
			Perpetuity.LOGGER.error("Labyrinth: missing structure template for {}", shape.id());
			return null;
		}
		return loaded.get();
	}

	/** The authored jigsaw blocks all declare {@code minecraft:air} as their final state. */
	private void clearJigsawBlocks(StructureWorldAccess world, StructureTemplate template, BlockPos origin,
	                               BlockRotation rotation, BlockBox chunkBox) {
		BlockState air = Blocks.AIR.getDefaultState();
		for (StructureTemplate.JigsawBlockInfo info : template.getJigsawInfos(origin, rotation)) {
			BlockPos pos = info.info().pos();
			if (chunkBox.contains(pos)) world.setBlockState(pos, air, Block.NOTIFY_LISTENERS);
		}
	}

	/**
	 * Checks once per piece and rotation that every lattice exit lands on its cell boundary. A silent
	 * mismatch shows up in game as a corridor walled off short of its neighbour.
	 */
	private void validateExits(PieceShape shape, int rotation, int mask, int cellX, int cellZ, BlockPos origin) {
		if (!validated.add(shape.id() + "/" + rotation)) return;

		for (PieceShape.Conn c : shape.place(rotation, origin.getX(), origin.getY(), origin.getZ())) {
			if (!c.lattice()) continue;
			if ((mask & LabyrinthTileSet.maskOf(c.facing())) == 0) continue;

			int wantX = exitX(cellX, c.facing());
			int wantZ = exitZ(cellZ, c.facing());
			if (c.x() != wantX || c.y() != LabyrinthTileSet.PATH_Y || c.z() != wantZ) {
				Perpetuity.LOGGER.error(
						"Labyrinth: {} rotated {} puts its {} exit at {},{},{} but the lattice needs {},{},{}"
								+ " - that corridor will not meet its neighbour",
						shape.id(), rotation, LabyrinthTileSet.direction(c.facing()),
						c.x(), c.y(), c.z(), wantX, LabyrinthTileSet.PATH_Y, wantZ);
			}
		}
	}

	// ---------------------------------------------------------------- helpers

	private LabyrinthMaze maze(long seed) {
		LabyrinthMaze current = maze;
		if (current == null || mazeSeed != seed) {
			current = new LabyrinthMaze(seed);
			maze = current;
			mazeSeed = seed;
			plans.clear();
		}
		return current;
	}

	private LabyrinthTileSet tiles(StructureTemplateManager templates) {
		LabyrinthTileSet current = tiles;
		if (current == null) {
			current = LabyrinthTileSet.load(templates);
			tiles = current;
		}
		return current;
	}

	private static long variant(int cellX, int cellZ) {
		long v = ((long) cellX << 32) ^ (cellZ & 0xFFFFFFFFL);
		v = (v ^ (v >>> 30)) * 0xBF58476D1CE4E5B9L;
		v = (v ^ (v >>> 27)) * 0x94D049BB133111EBL;
		return (v ^ (v >>> 31)) >>> 1;
	}

	// ------------------------------------------------------- nothing else runs

	@Override
	public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig,
	                                              StructureAccessor structureAccessor, Chunk chunk) {
		return CompletableFuture.completedFuture(chunk);
	}

	@Override
	public void carve(ChunkRegion region, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess,
	                  StructureAccessor structureAccessor, Chunk chunk) {
	}

	@Override
	public void buildSurface(ChunkRegion region, StructureAccessor structureAccessor,
	                         NoiseConfig noiseConfig, Chunk chunk) {
		ChunkPos chunkPos = chunk.getPos();
		BlockPos.Mutable pos = new BlockPos.Mutable();
		BlockState stone = Blocks.STONE.getDefaultState();
		BlockState grass = Blocks.GRASS_BLOCK.getDefaultState();

		for (int x = chunkPos.getStartX(); x <= chunkPos.getEndX(); x++) {
			for (int z = chunkPos.getStartZ(); z <= chunkPos.getEndZ(); z++) {
				for (int y = MIN_Y; y < TERRAIN_TOP_Y; y++) {
					chunk.setBlockState(pos.set(x, y, z), stone, 0);
				}
				chunk.setBlockState(pos.set(x, TERRAIN_TOP_Y, z), grass, 0);
			}
		}
	}

	@Override
	public void populateEntities(ChunkRegion region) {
	}

	@Override
	public int getWorldHeight() {
		return WORLD_HEIGHT;
	}

	@Override
	public int getMinimumY() {
		return MIN_Y;
	}

	@Override
	public int getSeaLevel() {
		return MIN_Y;
	}

	@Override
	public int getSpawnHeight(HeightLimitView world) {
		return LabyrinthTileSet.PATH_Y;
	}

	@Override
	public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
		return TERRAIN_TOP_Y + 1;
	}

	@Override
	public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
		BlockState[] column = new BlockState[world.getHeight()];
		Arrays.fill(column, Blocks.AIR.getDefaultState());
		int bottom = world.getBottomY();
		int stoneStart = Math.max(MIN_Y, bottom);
		int stoneEnd = Math.min(TERRAIN_TOP_Y, bottom + world.getHeight());
		for (int y = stoneStart; y < stoneEnd; y++) {
			column[y - bottom] = Blocks.STONE.getDefaultState();
		}
		if (TERRAIN_TOP_Y >= bottom && TERRAIN_TOP_Y < bottom + world.getHeight()) {
			column[TERRAIN_TOP_Y - bottom] = Blocks.GRASS_BLOCK.getDefaultState();
		}
		return new VerticalBlockSample(world.getBottomY(), column);
	}

	@Override
	public void appendDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {
	}
}
