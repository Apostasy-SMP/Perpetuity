package net.apostasy.perpetuity.event;

import net.apostasy.perpetuity.util.EchoUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

/**
 * @author Chemthunder
 */
public class PortalEchoesEvent implements ServerTickEvents.StartWorldTick {
    public void onStartTick(ServerWorld world) {
        if (world.getRegistryKey() == EchoUtil.ECHOES_KEY) {
            for (ServerPlayerEntity player : world.getPlayers()) {
                if (player.getY() < EchoUtil.MIN_Y_HEIGHT) {
                    player.teleportTo(
                            new TeleportTarget(
                                    world,
                                    new Vec3d(
                                            player.getX(),
                                            25,
                                            player.getZ()
                                    ),
                                    new Vec3d(
                                            player.getVelocity().x,
                                            0,
                                            player.getVelocity().z
                                    ),
                                    player.getYaw(),
                                    player.getPitch(),
                                    TeleportTarget.NO_OP
                            )
                    );
                }
            }
        }
    }
}
