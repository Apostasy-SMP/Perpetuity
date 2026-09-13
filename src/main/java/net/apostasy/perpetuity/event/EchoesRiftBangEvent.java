package net.apostasy.perpetuity.event;

import net.apostasy.perpetuity.network.s2c.RiftBangPayload;
import net.apostasy.perpetuity.util.EchoUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * @author Chemthunder
 */
public class EchoesRiftBangEvent implements ServerTickEvents.StartWorldTick {
    public void onStartTick(ServerWorld world) {
        if (world.getRegistryKey() == EchoUtil.ECHOES_KEY) {
            if (world.getRandom().nextInt(300) == 0) {
                for (ServerPlayerEntity player : world.getPlayers()) {
                    ServerPlayNetworking.send(player, new RiftBangPayload());
                }
            }
        }
    }
}
