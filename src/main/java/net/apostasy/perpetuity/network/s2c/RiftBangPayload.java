package net.apostasy.perpetuity.network.s2c;

import net.apostasy.perpetuity.Perpetuity;
import net.apostasy.perpetuity.client.render.EchoesSkyPass;
import net.apostasy.perpetuity.registry.ModSounds;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;

public record RiftBangPayload(Vec3d position, int seed) implements CustomPayload {
    public static final Id<RiftBangPayload> ID = new Id<>(Perpetuity.id("rift_bang"));

    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static final PacketCodec<RegistryByteBuf, RiftBangPayload> CODEC = PacketCodec.tuple(
            Vec3d.PACKET_CODEC, RiftBangPayload::position,
            PacketCodecs.INTEGER, RiftBangPayload::seed,
            RiftBangPayload::new
    );

    public static class Receiver implements ClientPlayNetworking.PlayPayloadHandler<RiftBangPayload> {
        public void receive(RiftBangPayload payload, ClientPlayNetworking.Context context) {
            var client = context.client();
            EchoesSkyPass.flash(payload.position(), payload.seed());

            if (client.world == null) return;
            float pitch = 0.9F + Math.floorMod(payload.seed(), 21) * 0.01F;
            PositionedSoundInstance bang = new PositionedSoundInstance(
                    ModSounds.DISTANT_BANG,
                    SoundCategory.AMBIENT,
                    8.0F,
                    pitch,
                    client.world.getRandom(),
                    payload.position().x,
                    payload.position().y,
                    payload.position().z
            );

            double distance = client.player == null
                    ? 0.0
                    : new Vec3d(client.player.getX(), client.player.getY(), client.player.getZ())
                            .distanceTo(payload.position());
            int delayTicks = Math.max(1, (int) Math.round(distance / 17.15));
            client.getSoundManager().play(bang, delayTicks);
        }
    }
}
