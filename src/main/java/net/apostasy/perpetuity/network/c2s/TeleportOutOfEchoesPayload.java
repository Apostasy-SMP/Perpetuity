package net.apostasy.perpetuity.network.c2s;

import net.apostasy.perpetuity.Perpetuity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

public record TeleportOutOfEchoesPayload() implements CustomPayload {
    public static final Id<TeleportOutOfEchoesPayload> ID = new Id<>(Perpetuity.id("teleport_out_of_echoes"));

    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static final PacketCodec<RegistryByteBuf, TeleportOutOfEchoesPayload> CODEC = PacketCodec.unit(new TeleportOutOfEchoesPayload());

    public static void send() {
        ClientPlayNetworking.send(new TeleportOutOfEchoesPayload());
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<TeleportOutOfEchoesPayload> {
        public void receive(TeleportOutOfEchoesPayload payload, ServerPlayNetworking.Context context) {
            PlayerEntity player = context.player();

            player.teleportTo(
                    new TeleportTarget(
                            context.server().getOverworld(),
                            new Vec3d(
                                    player.getX(),
                                    player.getY() + 300,
                                    player.getZ()
                            ),
                            player.getVelocity(),
                            player.getYaw(),
                            player.getPitch(),
                            TeleportTarget.NO_OP
                    )
            );
        }
    }
}
