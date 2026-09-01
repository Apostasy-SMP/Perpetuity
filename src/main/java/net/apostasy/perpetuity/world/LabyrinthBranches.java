package net.apostasy.perpetuity.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Grows the branch network inside one ring's empty interior.
 *
 * <p>A small jigsaw engine rather than a fixed rule: pieces are chosen by trying each of their
 * connectors against the open end, so a new NBT gives the engine new moves for free. Branches
 * therefore chain, fork wherever a piece has three or four connectors, and rejoin where two open
 * ends coincide exactly.
 *
 * <p>Rejoining is limited by geometry, not tuning. A stub stops 4 blocks short of its cell
 * boundary, so a branch off a north-side stub runs on the lattice shifted by (0,-4) and a
 * south-side stub sits on (0,+4) - those never meet. Two stubs on the same side share a shift, and
 * there a loop closes exactly.
 *
 * <p>No open end is ever left facing nothing: each reserves the space its cap would occupy and no
 * piece may be placed over a live reservation, so a cap is always still placeable when growth stops.
 */
public final class LabyrinthBranches {

    /** A piece the engine decided to place. */
    public record Placed(PieceShape shape, int rotation, int x, int y, int z) {}

    /** A stub on the ring a branch may grow from, and the corridor cell that owns it. */
    public record Port(int x, int y, int z, int facing, int hostCellX, int hostCellZ) {}

    /** @param accepted ports actually taken up; cells not listed stay plain straights */
    public record Plan(List<Placed> pieces, List<Port> accepted, int loops) {}

    /** Boxes the engine must not build into - the ring tiles surrounding the pocket. */
    public interface Obstacles {
        boolean blocked(int[] box);
    }

    private static final int MAX_PIECES = 16;
    private static final int MAX_DEPTH = 5;
    /** Pieces a loop-closing route may use. */
    private static final int ROUTE_DEPTH = 3;

    private LabyrinthBranches() {}

    private record Open(int x, int y, int z, int facing, int depth, int[] reserved) {}

    public static Plan build(long seed, int ringX, int ringZ, int[] region,
                             List<PieceShape> pieces, PieceShape cap, List<Port> candidates,
                             Obstacles obstacles) {
        Random random = new Random(mix(seed, ringX, ringZ));

        List<Placed> placed = new ArrayList<>();
        List<int[]> boxes = new ArrayList<>();
        List<Open> open = new ArrayList<>();
        List<Port> accepted = new ArrayList<>();
        int loops = 0;

        // Only take a port if its branch could at least be capped.
        for (Port port : shuffled(candidates, random)) {
            int[] reserved = capBox(cap, port.x(), port.y(), port.z(), port.facing());
            if (reserved == null || !fits(reserved, region, boxes, open, obstacles, List.of())) continue;
            accepted.add(port);
            open.add(new Open(port.x(), port.y(), port.z(), port.facing(), 0, reserved));
        }

        // Link pairs of ports into loops first. Left to free growth this almost never happens: both
        // ports grow their own branch, so neither is still open when the other could reach it.
        List<Open> ends = new ArrayList<>(open);
        for (int[] pair : pairs(ends.size(), random)) {
            if (placed.size() >= MAX_PIECES) break;
            Open a = ends.get(pair[0]);
            Open b = ends.get(pair[1]);
            // Either may already have been consumed by an earlier pair.
            if (!open.contains(a) || !open.contains(b)) continue;

            List<Placed> route = findRoute(a, b, region, pieces, boxes, open, obstacles, ROUTE_DEPTH);
            if (route == null) continue;

            for (Placed step : route) {
                placed.add(step);
                boxes.add(step.shape().box(step.rotation(), step.x(), step.y(), step.z()));
            }
            open.remove(b);
            open.remove(a);
            loops++;
        }

        int cursor = 0;
        while (cursor < open.size() && placed.size() < MAX_PIECES) {
            Open end = open.get(cursor);

            int partner = mate(open, cursor, end);
            if (partner >= 0) {
                // Two open ends meet exactly, so neither needs a cap.
                open.remove(Math.max(cursor, partner));
                open.remove(Math.min(cursor, partner));
                loops++;
                cursor = 0;
                continue;
            }

            Placed grown = end.depth() < MAX_DEPTH
                    ? tryGrow(end, region, pieces, cap, boxes, open, obstacles, random)
                    : null;
            if (grown == null) {
                cursor++;
                continue;
            }

            placed.add(grown);
            boxes.add(grown.shape().box(grown.rotation(), grown.x(), grown.y(), grown.z()));
            open.remove(cursor);

            int mating = matingIndex(grown, end);
            List<PieceShape.Conn> exposed = grown.shape()
                    .place(grown.rotation(), grown.x(), grown.y(), grown.z());
            for (int i = 0; i < exposed.size(); i++) {
                if (i == mating) continue;
                PieceShape.Conn c = exposed.get(i);
                open.add(new Open(c.x(), c.y(), c.z(), c.facing(), end.depth() + 1,
                        capBox(cap, c.x(), c.y(), c.z(), c.facing())));
            }
            cursor = 0;
        }

        // Whatever is still open takes the cap it reserved, which is guaranteed to fit.
        for (Open end : open) {
            int[] origin = capOrigin(cap, end.x(), end.y(), end.z(), end.facing());
            if (origin != null) placed.add(new Placed(cap, origin[3], origin[0], origin[1], origin[2]));
        }
        return new Plan(placed, accepted, loops);
    }

    // ----------------------------------------------------------------- routing

    /**
     * Searches for a short run of pieces from {@code from} landing exactly on {@code to}, joining two
     * stubs into a loop. Only two-connector pieces are used, so the route is a passage rather than a
     * junction and the search stays small enough to run for every pair.
     */
    private static List<Placed> findRoute(Open from, Open to, int[] region, List<PieceShape> pieces,
                                          List<int[]> boxes, List<Open> open, Obstacles obstacles, int depth) {
        if (depth <= 0) return null;

        int targetX = from.x() + PieceShape.stepX(from.facing());
        int targetY = from.y();
        int targetZ = from.z() + PieceShape.stepZ(from.facing());
        int need = PieceShape.opposite(from.facing());

        int landX = to.x() + PieceShape.stepX(to.facing());
        int landZ = to.z() + PieceShape.stepZ(to.facing());
        int landFacing = PieceShape.opposite(to.facing());

        for (PieceShape shape : pieces) {
            if (shape.connectors().size() != 2) continue;

            for (int rotation = 0; rotation < 4; rotation++) {
                for (int k = 0; k < 2; k++) {
                    if (PieceShape.rotateFacing(shape.connectors().get(k).facing(), rotation) != need) continue;

                    int[] origin = shape.originForConn(k, rotation, targetX, targetY, targetZ);
                    int[] box = shape.box(rotation, origin[0], origin[1], origin[2]);
                    if (!PieceShape.contains(region, box) || obstacles.blocked(box)) continue;
                    if (overlapsAny(box, boxes)) continue;
                    if (blockedByReservations(box, open, from, to)) continue;

                    PieceShape.Conn far = shape.place(rotation, origin[0], origin[1], origin[2]).get(1 - k);
                    Placed step = new Placed(shape, rotation, origin[0], origin[1], origin[2]);

                    // Landed on the far stub: the route is complete.
                    if (far.x() == landX && far.y() == to.y() && far.z() == landZ
                            && far.facing() == landFacing) {
                        List<Placed> route = new ArrayList<>();
                        route.add(step);
                        return route;
                    }

                    List<int[]> extended = new ArrayList<>(boxes);
                    extended.add(box);
                    Open next = new Open(far.x(), far.y(), far.z(), far.facing(), 0, null);
                    List<Placed> rest = findRoute(next, to, region, pieces, extended, open, obstacles, depth - 1);
                    if (rest != null) {
                        List<Placed> route = new ArrayList<>();
                        route.add(step);
                        route.addAll(rest);
                        return route;
                    }
                }
            }
        }
        return null;
    }

    private static boolean overlapsAny(int[] box, List<int[]> boxes) {
        for (int[] other : boxes) if (PieceShape.intersects(box, other)) return true;
        return false;
    }

    private static boolean blockedByReservations(int[] box, List<Open> open, Open a, Open b) {
        for (Open o : open) {
            if (o == a || o == b || o.reserved() == null) continue;
            if (PieceShape.intersects(box, o.reserved())) return true;
        }
        return false;
    }

    /** All unordered index pairs, in seeded order. */
    private static List<int[]> pairs(int count, Random random) {
        List<int[]> all = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            for (int j = i + 1; j < count; j++) all.add(new int[]{i, j});
        }
        return shuffled(all, random);
    }

    // ------------------------------------------------------------------ growth

    private static Placed tryGrow(Open end, int[] region, List<PieceShape> pieces, PieceShape cap,
                                  List<int[]> boxes, List<Open> open, Obstacles obstacles, Random random) {
        int targetX = end.x() + PieceShape.stepX(end.facing());
        int targetY = end.y();
        int targetZ = end.z() + PieceShape.stepZ(end.facing());
        int need = PieceShape.opposite(end.facing());

        for (PieceShape shape : weighted(pieces, random)) {
            for (int rotation : shuffledRotations(random)) {
                for (int k = 0; k < shape.connectors().size(); k++) {
                    if (PieceShape.rotateFacing(shape.connectors().get(k).facing(), rotation) != need) continue;

                    int[] origin = shape.originForConn(k, rotation, targetX, targetY, targetZ);
                    int[] box = shape.box(rotation, origin[0], origin[1], origin[2]);

                    // Release the reservations of ends this piece joins: their caps are no longer
                    // wanted, and otherwise the far end's own cap always blocks the join.
                    List<Open> joined = joins(shape, rotation, origin, k, open, end);
                    joined.add(end);

                    if (!fits(box, region, boxes, open, obstacles, joined)) continue;
                    if (!capsStillFit(shape, rotation, origin, k, box, region, cap, boxes, open, obstacles,
                            joined)) {
                        continue;
                    }
                    return new Placed(shape, rotation, origin[0], origin[1], origin[2]);
                }
            }
        }
        return null;
    }

    /** Checked before committing a piece, so growth can never strand an arm. */
    private static boolean capsStillFit(PieceShape shape, int rotation, int[] origin, int mating, int[] box,
                                        int[] region, PieceShape cap, List<int[]> boxes, List<Open> open,
                                        Obstacles obstacles, List<Open> released) {
        List<PieceShape.Conn> exposed = shape.place(rotation, origin[0], origin[1], origin[2]);
        List<int[]> pending = new ArrayList<>();

        for (int i = 0; i < exposed.size(); i++) {
            if (i == mating) continue;
            PieceShape.Conn c = exposed.get(i);
            if (meetsOpen(open, c, released)) continue;

            int[] capBox = capBox(cap, c.x(), c.y(), c.z(), c.facing());
            if (capBox == null || !PieceShape.contains(region, capBox)) return false;
            if (PieceShape.intersects(capBox, box) || obstacles.blocked(capBox)) return false;
            for (int[] other : boxes) if (PieceShape.intersects(capBox, other)) return false;
            for (Open o : open) {
                if (released.contains(o) || o.reserved() == null) continue;
                if (PieceShape.intersects(capBox, o.reserved())) return false;
            }
            for (int[] other : pending) if (PieceShape.intersects(capBox, other)) return false;
            pending.add(capBox);
        }
        return true;
    }

    private static boolean fits(int[] box, int[] region, List<int[]> boxes, List<Open> open,
                                Obstacles obstacles, List<Open> released) {
        if (!PieceShape.contains(region, box)) return false;
        if (obstacles.blocked(box)) return false;
        for (int[] other : boxes) if (PieceShape.intersects(box, other)) return false;
        for (Open o : open) {
            if (released.contains(o) || o.reserved() == null) continue;
            if (PieceShape.intersects(box, o.reserved())) return false;
        }
        return true;
    }

    /** The open ends a piece placed here would join, other than the one it grows from. */
    private static List<Open> joins(PieceShape shape, int rotation, int[] origin, int mating,
                                    List<Open> open, Open growingFrom) {
        List<Open> found = new ArrayList<>();
        List<PieceShape.Conn> exposed = shape.place(rotation, origin[0], origin[1], origin[2]);
        for (int i = 0; i < exposed.size(); i++) {
            if (i == mating) continue;
            PieceShape.Conn c = exposed.get(i);
            int x = c.x() + PieceShape.stepX(c.facing());
            int z = c.z() + PieceShape.stepZ(c.facing());
            int need = PieceShape.opposite(c.facing());
            for (Open o : open) {
                if (o == growingFrom) continue;
                if (o.x() == x && o.y() == c.y() && o.z() == z && o.facing() == need) found.add(o);
            }
        }
        return found;
    }

    // ------------------------------------------------------------------ mating

    /** Index of an open end this one joins exactly, or -1. */
    private static int mate(List<Open> open, int self, Open end) {
        int x = end.x() + PieceShape.stepX(end.facing());
        int z = end.z() + PieceShape.stepZ(end.facing());
        int need = PieceShape.opposite(end.facing());
        for (int i = 0; i < open.size(); i++) {
            if (i == self) continue;
            Open other = open.get(i);
            if (other.x() == x && other.y() == end.y() && other.z() == z && other.facing() == need) return i;
        }
        return -1;
    }

    private static boolean meetsOpen(List<Open> open, PieceShape.Conn c, List<Open> released) {
        int x = c.x() + PieceShape.stepX(c.facing());
        int z = c.z() + PieceShape.stepZ(c.facing());
        int need = PieceShape.opposite(c.facing());
        for (Open o : open) {
            if (o.x() == x && o.y() == c.y() && o.z() == z && o.facing() == need) return true;
        }
        return false;
    }

    private static int matingIndex(Placed placed, Open end) {
        int x = end.x() + PieceShape.stepX(end.facing());
        int z = end.z() + PieceShape.stepZ(end.facing());
        int need = PieceShape.opposite(end.facing());
        List<PieceShape.Conn> exposed = placed.shape()
                .place(placed.rotation(), placed.x(), placed.y(), placed.z());
        for (int i = 0; i < exposed.size(); i++) {
            PieceShape.Conn c = exposed.get(i);
            if (c.x() == x && c.z() == z && c.facing() == need) return i;
        }
        return -1;
    }

    // --------------------------------------------------------------------- cap

    /** {originX, originY, originZ, rotation} placing the cap against an open end, or null. */
    private static int[] capOrigin(PieceShape cap, int x, int y, int z, int facing) {
        int need = PieceShape.opposite(facing);
        int targetX = x + PieceShape.stepX(facing);
        int targetZ = z + PieceShape.stepZ(facing);
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int k = 0; k < cap.connectors().size(); k++) {
                if (PieceShape.rotateFacing(cap.connectors().get(k).facing(), rotation) != need) continue;
                int[] origin = cap.originForConn(k, rotation, targetX, y, targetZ);
                return new int[]{origin[0], origin[1], origin[2], rotation};
            }
        }
        return null;
    }

    private static int[] capBox(PieceShape cap, int x, int y, int z, int facing) {
        int[] origin = capOrigin(cap, x, y, z, facing);
        return origin == null ? null : cap.box(origin[3], origin[0], origin[1], origin[2]);
    }

    // ---------------------------------------------------------------- ordering

    private static <T> List<T> shuffled(List<T> source, Random random) {
        List<T> copy = new ArrayList<>(source);
        for (int i = copy.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            T swap = copy.get(i);
            copy.set(i, copy.get(j));
            copy.set(j, swap);
        }
        return copy;
    }

    /** Shuffled, but a piece's weight is how many times it enters the draw. */
    private static List<PieceShape> weighted(List<PieceShape> pieces, Random random) {
        List<PieceShape> pool = new ArrayList<>();
        for (PieceShape shape : pieces) {
            for (int i = 0; i < Math.max(1, shape.weight()); i++) pool.add(shape);
        }
        return shuffled(pool, random);
    }

    private static int[] shuffledRotations(Random random) {
        int[] order = {0, 1, 2, 3};
        for (int i = 3; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int swap = order[i];
            order[i] = order[j];
            order[j] = swap;
        }
        return order;
    }

    private static long mix(long seed, int x, int z) {
        long v = seed * 0x9E3779B97F4A7C15L + (long) x * 0xBF58476D1CE4E5B9L + (long) z * 0x94D049BB133111EBL;
        v = (v ^ (v >>> 30)) * 0xBF58476D1CE4E5B9L;
        v = (v ^ (v >>> 27)) * 0x94D049BB133111EBL;
        return v ^ (v >>> 31);
    }
}