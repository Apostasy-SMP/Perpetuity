package net.apostasy.perpetuity.world;

import java.util.ArrayList;
import java.util.List;

/**
 * One labyrinth piece's geometry, in plain Java so the layout logic can be tested outside Minecraft.
 * Everything is derived from the authored NBT rather than declared by hand - see
 * {@link LabyrinthTileSet}.
 *
 * <p>Rotations index {@code BlockRotation.values()} (0 NONE, 1 CW90, 2 CW180, 3 CCW90) and turn
 * about the template origin, matching vanilla placement with a default pivot. Facings are 0 north,
 * 1 east, 2 south, 3 west.
 */
public record PieceShape(String id, int sizeX, int sizeY, int sizeZ, List<Conn> connectors, int weight) {

    /**
     * @param lattice whether this arm reaches a cell boundary. Short arms do not, and are only used
     *                to start or continue a branch.
     */
    public record Conn(int x, int y, int z, int facing, boolean lattice) {}

    public static final int NORTH = 0, EAST = 1, SOUTH = 2, WEST = 3;

    public static int opposite(int facing) {
        return (facing + 2) & 3;
    }

    public static int rotateFacing(int facing, int rotation) {
        return (facing + rotation) & 3;
    }

    public static int stepX(int facing) {
        return facing == EAST ? 1 : facing == WEST ? -1 : 0;
    }

    public static int stepZ(int facing) {
        return facing == SOUTH ? 1 : facing == NORTH ? -1 : 0;
    }

    public static int rotX(int rotation, int x, int z) {
        return switch (rotation) {
            case 1 -> -z;
            case 2 -> -x;
            case 3 -> z;
            default -> x;
        };
    }

    public static int rotZ(int rotation, int x, int z) {
        return switch (rotation) {
            case 1 -> x;
            case 2 -> -z;
            case 3 -> -x;
            default -> z;
        };
    }

    /** Connectors as presented once rotated and placed at the given origin. */
    public List<Conn> place(int rotation, int originX, int originY, int originZ) {
        List<Conn> result = new ArrayList<>(connectors.size());
        for (Conn c : connectors) {
            result.add(new Conn(
                    originX + rotX(rotation, c.x(), c.z()),
                    originY + c.y(),
                    originZ + rotZ(rotation, c.x(), c.z()),
                    rotateFacing(c.facing(), rotation),
                    c.lattice()));
        }
        return result;
    }

    /** Bounding box as {x0, y0, z0, x1, y1, z1}, inclusive. */
    public int[] box(int rotation, int originX, int originY, int originZ) {
        int ax = rotX(rotation, 0, 0), az = rotZ(rotation, 0, 0);
        int bx = rotX(rotation, sizeX - 1, sizeZ - 1), bz = rotZ(rotation, sizeX - 1, sizeZ - 1);
        return new int[]{
                originX + Math.min(ax, bx), originY, originZ + Math.min(az, bz),
                originX + Math.max(ax, bx), originY + sizeY - 1, originZ + Math.max(az, bz)};
    }

    /**
     * Origin placing connector {@code index} at the given world position. Anchoring on a connector
     * rather than the bounding box keeps placement exact however the rotation shifts the box.
     */
    public int[] originForConn(int index, int rotation, int atX, int atY, int atZ) {
        Conn c = connectors.get(index);
        return new int[]{
                atX - rotX(rotation, c.x(), c.z()),
                atY - c.y(),
                atZ - rotZ(rotation, c.x(), c.z())};
    }

    public static boolean intersects(int[] a, int[] b) {
        return a[0] <= b[3] && a[3] >= b[0]
                && a[1] <= b[4] && a[4] >= b[1]
                && a[2] <= b[5] && a[5] >= b[2];
    }

    public static boolean contains(int[] outer, int[] inner) {
        return inner[0] >= outer[0] && inner[3] <= outer[3]
                && inner[1] >= outer[1] && inner[4] <= outer[4]
                && inner[2] >= outer[2] && inner[5] <= outer[5];
    }
}