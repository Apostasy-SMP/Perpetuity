package net.apostasy.perpetuity.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Infinite labyrinth topology, computed on demand and without global state.
 *
 * <p>The world is a lattice of 25x25 cells; {@link #exitMask(int, int)} answers which sides of any
 * cell carry a corridor, from the seed and the cell's own coordinates alone. Chunks can therefore
 * generate in any order on any thread and still agree on where corridors meet.
 *
 * <p>Three properties hold by construction rather than by tuning:
 * <ul>
 *   <li>Every corridor cell has degree 2 or 4, never 1 or 3 - only the tiles whose arms are all
 *       length 12 tile on an exact 25 pitch, so odd degrees have no piece that fits.</li>
 *   <li>The network is connected and has no dead ends.</li>
 *   <li>No corridor opens into a pocket, which is the empty space branches grow into.</li>
 * </ul>
 *
 * <p>Four layers produce that:
 * <ol>
 *   <li><b>A hierarchical spanning tree</b> over coarse cells. Each 8x8 block gets a randomised DFS
 *       maze seeded by its coordinates; blocks group into super-blocks the same way up to
 *       {@link #MAX_LEVEL}, each parent link opening exactly one doorway. A tree at every level
 *       composes into one infinite tree.</li>
 *   <li><b>The contour of that tree</b> - each coarse cell contributes its perimeter ring, and
 *       linked cells have their rings spliced, leaving one connected cycle.</li>
 *   <li><b>Pockets</b>, the empty ring interiors. {@link #ports} offers the cells that can open into
 *       one; {@link LabyrinthBranches} fills it.</li>
 *   <li><b>Hubs</b>, where all four edges of a face are absent and all four get added at once.
 *       Adding cannot disconnect anything and lifts four cells from degree 2 to 4, giving the
 *       network junctions instead of one unbroken loop.</li>
 * </ol>
 */
public final class LabyrinthMaze {
    public static final int NORTH = 1;
    public static final int EAST = 2;
    public static final int SOUTH = 4;
    public static final int WEST = 8;

    /** Coarse cells along one side of a hierarchy block. */
    private static final int GROUP = 8;
    /**
     * Lattice cells per coarse cell, per axis. The ring uses the perimeter and frees the
     * {@code (SUB-2)^2} interior for branches; larger values mean bigger branch rooms and a thinner
     * corridor grid.
     */
    private static final int SUB = 5;
    /** Levels before falling back to a fixed tree. Level 7 already exceeds the world border. */
    private static final int MAX_LEVEL = 7;

    private static final int E_BIT = 1;
    private static final int S_BIT = 2;

    /** Fraction of eligible ring corners that become a hub. */
    private static final float HUB_CHANCE = 0.8f;

    private final long seed;
    private final Map<GroupKey, byte[]> groupCache = new ConcurrentHashMap<>();

    public LabyrinthMaze(long seed) {
        this.seed = seed;
    }

    // ------------------------------------------------------------------ public

    /**
     * Which sides of lattice cell {@code (cx, cz)} carry a corridor, as a mask of
     * {@link #NORTH}/{@link #EAST}/{@link #SOUTH}/{@link #WEST}. Always has 2 or 4 bits set, and is
     * always consistent with the neighbouring cells' answers.
     */
    public int exitMask(int cx, int cz) {
        if (isPocket(cx, cz)) return 0;
        int mask = contourMask(cx, cz);

        // Only a turn can become a hub, via the one face its two absent edges bound.
        int fx;
        int fz;
        if ((mask & (EAST | SOUTH)) == 0) {
            fx = cx;
            fz = cz;
        } else if ((mask & (WEST | SOUTH)) == 0) {
            fx = cx - 1;
            fz = cz;
        } else if ((mask & (EAST | NORTH)) == 0) {
            fx = cx;
            fz = cz - 1;
        } else if ((mask & (WEST | NORTH)) == 0) {
            fx = cx - 1;
            fz = cz - 1;
        } else {
            return mask;
        }

        if (faceApplies(fx, fz)) {
            mask |= (cx == fx) ? EAST : WEST;
            mask |= (cz == fz) ? SOUTH : NORTH;
        }
        return mask;
    }

    // ---------------------------------------------------------------- contour

    /**
     * The corridor cycle before hubs are cut in: two bits, or zero for a pocket.
     *
     * <p>A splice drops one edge per side of a shared boundary and adds two crossings, so each
     * affected cell trades one edge for one and degrees stay at 2 however many sides are spliced.
     */
    private int contourMask(int cx, int cz) {
        int i = Math.floorMod(cx, SUB);
        int j = Math.floorMod(cz, SUB);
        if (i > 0 && i < SUB - 1 && j > 0 && j < SUB - 1) return 0;

        int coarseX = Math.floorDiv(cx, SUB);
        int coarseZ = Math.floorDiv(cz, SUB);
        int mask = ringMask(i, j);

        // Keyed to the coarse edge, so both sides of a boundary pick the same doorway.
        if (i == SUB - 1 && edgeEast(coarseX, coarseZ, 0)) {
            mask = spliceVertical(mask, j, spliceDoorway(coarseX, coarseZ, 0), EAST);
        }
        if (i == 0 && edgeEast(coarseX - 1, coarseZ, 0)) {
            mask = spliceVertical(mask, j, spliceDoorway(coarseX - 1, coarseZ, 0), WEST);
        }
        if (j == SUB - 1 && edgeSouth(coarseX, coarseZ, 0)) {
            mask = spliceHorizontal(mask, i, spliceDoorway(coarseX, coarseZ, 1), SOUTH);
        }
        if (j == 0 && edgeSouth(coarseX, coarseZ - 1, 0)) {
            mask = spliceHorizontal(mask, i, spliceDoorway(coarseX, coarseZ - 1, 1), NORTH);
        }
        return mask;
    }

    /** The bare perimeter ring: corners turn, mid-edge cells run straight. */
    private static int ringMask(int i, int j) {
        if (j == 0) return i == 0 ? (EAST | SOUTH) : i == SUB - 1 ? (WEST | SOUTH) : (EAST | WEST);
        if (j == SUB - 1) return i == 0 ? (EAST | NORTH) : i == SUB - 1 ? (WEST | NORTH) : (EAST | WEST);
        return NORTH | SOUTH;
    }

    /** Splices a ring's east or west side; the doorway names which edge is dropped. */
    private static int spliceVertical(int mask, int j, int doorway, int crossing) {
        if (j == doorway) return (mask & ~SOUTH) | crossing;
        if (j == doorway + 1) return (mask & ~NORTH) | crossing;
        return mask;
    }

    /** Splices a ring's north or south side. */
    private static int spliceHorizontal(int mask, int i, int doorway, int crossing) {
        if (i == doorway) return (mask & ~EAST) | crossing;
        if (i == doorway + 1) return (mask & ~WEST) | crossing;
        return mask;
    }

    /** Which of a side's two edges the splice drops. Both rings read the same coarse edge. */
    private int spliceDoorway(int coarseX, int coarseZ, int axis) {
        return (int) Math.floorMod(mix(seed, hash2(coarseX, coarseZ), 0x5F11CEL + axis), SUB - 1);
    }

    /** True where no corridor is placed - the empty interior of a coarse ring, kept for branches. */
    public boolean isPocket(int cx, int cz) {
        int i = Math.floorMod(cx, SUB);
        int j = Math.floorMod(cz, SUB);
        return i > 0 && i < SUB - 1 && j > 0 && j < SUB - 1;
    }

    public static int sub() {
        return SUB;
    }

    public int ringX(int cx) {
        return Math.floorDiv(cx, SUB);
    }

    public int ringZ(int cz) {
        return Math.floorDiv(cz, SUB);
    }

    // --------------------------------------------------------------- branches

    /**
     * A candidate cell that could open a side arm into its ring's interior. {@link LabyrinthBranches}
     * decides which are actually used, since only it knows whether there is room behind them.
     */
    public record Port(int cellX, int cellZ, int inward) {}

    /** Fraction of eligible cells offered to the branch engine. */
    private static final float PORT_CHANCE = 0.55f;

    /**
     * Cells of a ring that could open an arm inwards: on the perimeter, next to the interior, and
     * left running straight past it by the splices. Several per ring is fine - the engine resolves
     * collisions, and two on the same side are what let a branch rejoin the corridor.
     */
    public List<Port> ports(int ringX, int ringZ) {
        List<Port> ports = new ArrayList<>();
        for (int k = 1; k < SUB - 1; k++) {
            add(ports, ringX * SUB + k, ringZ * SUB, SOUTH, EAST | WEST);
            add(ports, ringX * SUB + k, ringZ * SUB + SUB - 1, NORTH, EAST | WEST);
            add(ports, ringX * SUB, ringZ * SUB + k, EAST, NORTH | SOUTH);
            add(ports, ringX * SUB + SUB - 1, ringZ * SUB + k, WEST, NORTH | SOUTH);
        }
        return ports;
    }

    private void add(List<Port> ports, int cellX, int cellZ, int inward, int needs) {
        if (exitMask(cellX, cellZ) != needs) return;
        if ((mix(seed, hash2(cellX, cellZ), 0xB4A9C4L) >>> 11) * 0x1.0p-53 >= PORT_CHANCE) return;
        ports.add(new Port(cellX, cellZ, inward));
    }

    /** The interior block area of a ring, expanded by {@code margin} on each side. */
    public int[] pocketRegion(int ringX, int ringZ, int cell, int margin, int minY, int maxY) {
        int x0 = (ringX * SUB + 1) * cell - margin;
        int z0 = (ringZ * SUB + 1) * cell - margin;
        int x1 = (ringX * SUB + SUB - 1) * cell - 1 + margin;
        int z1 = (ringZ * SUB + SUB - 1) * cell - 1 + margin;
        return new int[]{x0, minY, z0, x1, maxY, z1};
    }

    /** The perimeter cells of a ring, in order, for collision checks against branch growth. */
    public List<long[]> ringCells(int ringX, int ringZ) {
        List<long[]> cells = new ArrayList<>();
        for (int j = 0; j < SUB; j++) {
            for (int i = 0; i < SUB; i++) {
                if (i > 0 && i < SUB - 1 && j > 0 && j < SUB - 1) continue;
                cells.add(new long[]{ringX * SUB + i, ringZ * SUB + j});
            }
        }
        return cells;
    }

    // ------------------------------------------------------------ crossroads

    /**
     * A face becomes a hub only when all four of its edges are absent.
     *
     * <p>That one condition covers three concerns: adding edges cannot disconnect anything; each
     * corner gains exactly two, so degrees stay even and go 2 to 4; and since a corridor cell has
     * exactly two absent edges, only a turn can qualify and only for one of its four faces - so no
     * two hubs share a cell and evaluation order cannot matter.
     */
    private boolean faceApplies(int fx, int fz) {
        // A face touching a pocket would run corridors into the space branches need.
        if (isPocket(fx, fz) || isPocket(fx + 1, fz) || isPocket(fx, fz + 1) || isPocket(fx + 1, fz + 1)) {
            return false;
        }

        if ((contourMask(fx, fz) & EAST) != 0) return false;
        if ((contourMask(fx, fz + 1) & EAST) != 0) return false;
        if ((contourMask(fx, fz) & SOUTH) != 0) return false;
        if ((contourMask(fx + 1, fz) & SOUTH) != 0) return false;

        return (mix(seed, hash2(fx, fz), 0x5EED) >>> 11) * 0x1.0p-53 < HUB_CHANCE;
    }

    // ------------------------------------------------------------ coarse tree

    /** Is the coarse tree edge between {@code (x, z)} and {@code (x + 1, z)} open? */
    private boolean edgeEast(int x, int z, int level) {
        if (level > MAX_LEVEL) return z == 0;

        int gx = Math.floorDiv(x, GROUP);
        int gz = Math.floorDiv(z, GROUP);
        int lx = Math.floorMod(x, GROUP);
        int lz = Math.floorMod(z, GROUP);

        if (lx < GROUP - 1) return (groupMaze(gx, gz, level)[lz * GROUP + lx] & E_BIT) != 0;

        // Crossing a block boundary: the parent decides whether they link, and which row.
        if (!edgeEast(gx, gz, level + 1)) return false;
        return doorway(gx, gz, level, 0) == lz;
    }

    /** Is the coarse tree edge between {@code (x, z)} and {@code (x, z + 1)} open? */
    private boolean edgeSouth(int x, int z, int level) {
        if (level > MAX_LEVEL) return true;

        int gx = Math.floorDiv(x, GROUP);
        int gz = Math.floorDiv(z, GROUP);
        int lx = Math.floorMod(x, GROUP);
        int lz = Math.floorMod(z, GROUP);

        if (lz < GROUP - 1) return (groupMaze(gx, gz, level)[lz * GROUP + lx] & S_BIT) != 0;

        if (!edgeSouth(gx, gz, level + 1)) return false;
        return doorway(gx, gz, level, 1) == lx;
    }

    private int doorway(int gx, int gz, int level, int axis) {
        return (int) Math.floorMod(mix(seed, hash2(gx, gz), (level << 1) | axis | 0x40000000L), GROUP);
    }

    private byte[] groupMaze(int gx, int gz, int level) {
        return groupCache.computeIfAbsent(new GroupKey(gx, gz, level), this::buildGroupMaze);
    }

    /** Randomised depth-first maze over one 8x8 block of coarse cells. */
    private byte[] buildGroupMaze(GroupKey key) {
        byte[] edges = new byte[GROUP * GROUP];
        boolean[] visited = new boolean[GROUP * GROUP];
        int[] stack = new int[GROUP * GROUP];
        Random random = new Random(mix(seed, hash2(key.gx, key.gz), key.level));

        int sp = 0;
        stack[sp++] = 0;
        visited[0] = true;

        int[] candidate = new int[4];
        int[] direction = new int[4];

        while (sp > 0) {
            int current = stack[sp - 1];
            int cx = current % GROUP;
            int cz = current / GROUP;

            int count = 0;
            if (cz > 0 && !visited[current - GROUP]) {
                candidate[count] = current - GROUP;
                direction[count++] = 0;
            }
            if (cx < GROUP - 1 && !visited[current + 1]) {
                candidate[count] = current + 1;
                direction[count++] = 1;
            }
            if (cz < GROUP - 1 && !visited[current + GROUP]) {
                candidate[count] = current + GROUP;
                direction[count++] = 2;
            }
            if (cx > 0 && !visited[current - 1]) {
                candidate[count] = current - 1;
                direction[count++] = 3;
            }

            if (count == 0) {
                sp--;
                continue;
            }

            int pick = random.nextInt(count);
            int next = candidate[pick];
            switch (direction[pick]) {
                case 0 -> edges[next] |= S_BIT;      // carve north: south edge of the cell above
                case 1 -> edges[current] |= E_BIT;
                case 2 -> edges[current] |= S_BIT;
                default -> edges[next] |= E_BIT;     // carve west: east edge of the cell to the left
            }
            visited[next] = true;
            stack[sp++] = next;
        }
        return edges;
    }

    // ---------------------------------------------------------------- hashing

    private static long mix(long a, long b, long c) {
        long z = a * 0x9E3779B97F4A7C15L + b * 0xBF58476D1CE4E5B9L + c * 0x94D049BB133111EBL;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    private static long hash2(int x, int z) {
        return ((long) x << 32) ^ (z & 0xFFFFFFFFL);
    }

    private record GroupKey(int gx, int gz, int level) {}
}