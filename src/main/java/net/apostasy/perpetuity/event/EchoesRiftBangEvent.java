package net.apostasy.perpetuity.event;

import net.apostasy.perpetuity.network.s2c.RiftBangPayload;
import net.apostasy.perpetuity.util.EchoUtil;
import net.apostasy.perpetuity.world.LabyrinthChunkGenerator;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

/**
 * @author Chemthunder
 */
public class EchoesRiftBangEvent implements ServerTickEvents.StartWorldTick {
    private static final double MIN_DISTANCE = 64.0;
    private static final double EXTRA_DISTANCE = 64.0;

    public void onStartTick(ServerWorld world) {
        if (world.getRegistryKey().equals(EchoUtil.ECHOES_KEY)) {
            if (world.getRandom().nextInt(300) == 0) {
                for (ServerPlayerEntity player : world.getPlayers()) {
                    double angle = world.getRandom().nextDouble() * Math.TAU;
                    double distance = MIN_DISTANCE + world.getRandom().nextDouble() * EXTRA_DISTANCE;
                    Vec3d strike = new Vec3d(
                            player.getX() + Math.cos(angle) * distance,
                            LabyrinthChunkGenerator.TERRAIN_TOP_Y,
                            player.getZ() + Math.sin(angle) * distance
                    );
                    ServerPlayNetworking.send(player,
                            new RiftBangPayload(strike, world.getRandom().nextInt()));
                }
            }
        }
    }
}
