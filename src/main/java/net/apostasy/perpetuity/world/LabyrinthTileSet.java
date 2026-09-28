package net.apostasy.perpetuity.world;

import net.apostasy.perpetuity.Perpetuity;
import net.minecraft.block.JigsawBlock;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import java.util.*;

/**
 * Reads the authored NBTs and works out what each piece is, so adding a tile means dropping in a
 * file and naming it here.
 *
 * <p>Exits come from the jigsaw blocks. On a 25x25 corridor tile, an exit carries a lattice
 * connection exactly when it is {@link #ARM} blocks from the piece's centre on that axis. Larger
 * landmark pieces are free-form branch pieces. Roles follow from geometry: the cap is whichever
 * piece has one connector, a v2 host adds one lattice arm to the ring, and corridor tiles are
 * matched by their lattice arms alone.
 *
 * <p>Weights decide how eagerly the branch engine reaches for a piece. Compact paths and corners
 * stay common, while junctions and large landmarks still enter the seeded draw often enough to
 * produce recognisably different pockets.
 */
public final class LabyrinthTileSet {
	/** Blocks per lattice cell. */
	public static final int CELL = 25;
	/** Distance from a piece's centre to an exit that reaches the cell boundary. */
	public static final int ARM = 12;
	/** World Y shared by every jigsaw doorway, regardless of its local height in the NBT. */
	public static final int PATH_Y = 7;

	/** Piece name to weight; everything else about a piece is read from its NBT. */
	private static final Map<String, Integer> PIECES = new LinkedHashMap<>();

	static {
		PIECES.put("grasslandpath", 4);
		PIECES.put("grasslandcorner", 4);
		PIECES.put("grasslandtpath", 3);
		PIECES.put("grasslandcrossroad", 2);
		PIECES.put("grasslanddeadend", 1);
		PIECES.put("grasslandcathedral", 2);
		PIECES.put("grasslandpool", 2);
		PIECES.put("grasslandremnant", 2);
	}

	private final Map<String, PieceShape> shapes = new LinkedHashMap<>();
	private final Map<String, Identifier> ids = new LinkedHashMap<>();
	private final List<PieceShape> branchSet = new ArrayList<>();
	private PieceShape cap;

	private LabyrinthTileSet() {}

	public static LabyrinthTileSet load(StructureTemplateManager templates) {
		LabyrinthTileSet set = new LabyrinthTileSet();

		for (Map.Entry<String, Integer> entry : PIECES.entrySet()) {
			Identifier id = Perpetuity.id("labyrinth/grassland/" + entry.getKey());
			Optional<StructureTemplate> loaded = templates.getTemplate(id);
			if (loaded.isEmpty()) {
				Perpetuity.LOGGER.error("Labyrinth: missing structure template {}", id);
				continue;
			}
			PieceShape shape = read(entry.getKey(), loaded.get(), entry.getValue());
			set.shapes.put(entry.getKey(), shape);
			set.ids.put(entry.getKey(), id);
		}

		for (PieceShape shape : set.shapes.values()) {
			if (shape.connectors().size() == 1 && (set.cap == null
					|| shape.connectors().size() < set.cap.connectors().size())) {
				set.cap = shape;
			}
		}
		for (PieceShape shape : set.shapes.values()) {
			// The cap is placed explicitly on open ends; letting growth pick it would end most
			// branches after one piece.
			if (shape != set.cap) set.branchSet.add(shape);
		}
		if (set.cap == null) {
			Perpetuity.LOGGER.error("Labyrinth: no single-connector piece to cap branches with");
		}
		return set;
	}

	private static PieceShape read(String name, StructureTemplate template, int weight) {
		Vec3i size = template.getSize();
		List<PieceShape.Conn> connectors = new ArrayList<>();

		for (StructureTemplate.JigsawBlockInfo info : template.getJigsawInfos(BlockPos.ORIGIN, net.minecraft.util.BlockRotation.NONE)) {
			Direction facing = JigsawBlock.getFacing(info.info().state());
			if (facing.getAxis().isVertical()) continue;

			BlockPos pos = info.info().pos();
			boolean horizontalAxis = facing.getAxis() == Direction.Axis.X;
			int along = horizontalAxis ? pos.getX() : pos.getZ();
			int centre = (horizontalAxis ? size.getX() : size.getZ()) / 2;

			connectors.add(new PieceShape.Conn(pos.getX(), pos.getY(), pos.getZ(),
					facingIndex(facing), Math.abs(along - centre) == ARM));
		}
		return new PieceShape(name, size.getX(), size.getY(), size.getZ(), connectors, weight);
	}

	// ------------------------------------------------------------------ lookup

	public PieceShape cap() {
		return cap;
	}

	public List<PieceShape> branchSet() {
		return branchSet;
	}

	public Identifier idOf(PieceShape shape) {
		return ids.get(shape.id());
	}

	/** The lattice exits a piece presents under a rotation, as a {@link LabyrinthMaze} mask. */
	public static int latticeMask(PieceShape shape, int rotation) {
		int mask = 0;
		for (PieceShape.Conn c : shape.connectors()) {
			if (c.lattice()) mask |= maskOf(PieceShape.rotateFacing(c.facing(), rotation));
		}
		return mask;
	}

	/**
	 * A plain corridor tile: matches the mask exactly and has no stub, so nothing points at a
	 * neighbour it cannot reach.
	 *
	 * @return {@code {shape, rotation}} or null
	 */
	public Object[] corridor(int mask, long variant) {
		List<Object[]> matches = new ArrayList<>();
		for (PieceShape shape : shapes.values()) {
			if (hasStub(shape)) continue;
			for (int rotation = 0; rotation < 4; rotation++) {
				if (latticeMask(shape, rotation) == mask) {
					for (int i = 0; i < Math.max(1, shape.weight()); i++) {
						matches.add(new Object[]{shape, rotation});
					}
					break;
				}
			}
		}
		if (matches.isEmpty()) return null;
		return matches.get((int) Math.floorMod(variant, matches.size()));
	}

	/**
	 * A tile that keeps the ring's existing exits and adds one full arm toward {@code inward}.
	 * The v2 T-path is a complete 25x25 tile, so its branch exit sits on the next cell boundary
	 * instead of ending in a short in-template stub.
	 */
	public Object[] host(int mask, int inward) {
		int hostedMask = mask | maskOf(inward);
		for (PieceShape shape : shapes.values()) {
			for (int rotation = 0; rotation < 4; rotation++) {
				if (latticeMask(shape, rotation) == hostedMask) {
					return new Object[]{shape, rotation};
				}
			}
		}

		// Keep support for older short-arm host pieces in resource packs.
		for (PieceShape shape : shapes.values()) {
			for (int rotation = 0; rotation < 4; rotation++) {
				if (latticeMask(shape, rotation) != mask) continue;
				for (PieceShape.Conn c : shape.connectors()) {
					if (!c.lattice() && PieceShape.rotateFacing(c.facing(), rotation) == inward) {
						return new Object[]{shape, rotation};
					}
				}
			}
		}
		return null;
	}

	private static boolean hasStub(PieceShape shape) {
		for (PieceShape.Conn c : shape.connectors()) if (!c.lattice()) return true;
		return false;
	}

	// ------------------------------------------------------------- conversions

	public static int facingIndex(Direction direction) {
		return switch (direction) {
			case NORTH -> PieceShape.NORTH;
			case EAST -> PieceShape.EAST;
			case SOUTH -> PieceShape.SOUTH;
			default -> PieceShape.WEST;
		};
	}

	public static Direction direction(int facing) {
		return switch (facing) {
			case PieceShape.NORTH -> Direction.NORTH;
			case PieceShape.EAST -> Direction.EAST;
			case PieceShape.SOUTH -> Direction.SOUTH;
			default -> Direction.WEST;
		};
	}

	public static int maskOf(int facing) {
		return switch (facing) {
			case PieceShape.NORTH -> LabyrinthMaze.NORTH;
			case PieceShape.EAST -> LabyrinthMaze.EAST;
			case PieceShape.SOUTH -> LabyrinthMaze.SOUTH;
			default -> LabyrinthMaze.WEST;
		};
	}

	public static int facingOfMask(int mask) {
		return switch (mask) {
			case LabyrinthMaze.NORTH -> PieceShape.NORTH;
			case LabyrinthMaze.EAST -> PieceShape.EAST;
			case LabyrinthMaze.SOUTH -> PieceShape.SOUTH;
			default -> PieceShape.WEST;
		};
	}

	public static net.minecraft.util.BlockRotation rotation(int index) {
		return net.minecraft.util.BlockRotation.values()[index];
	}
}
