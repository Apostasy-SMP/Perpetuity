package net.apostasy.perpetuity.network.s2c;

import net.apostasy.perpetuity.Perpetuity;
import net.apostasy.perpetuity.registry.ModSounds;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record RiftBangPayload() implements CustomPayload {
    public static final Id<RiftBangPayload> ID = new Id<>(Perpetuity.id("rift_bang"));

    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static final PacketCodec<RegistryByteBuf, RiftBangPayload> CODEC = PacketCodec.unit(new RiftBangPayload());

    public static class Receiever implements ClientPlayNetworking.PlayPayloadHandler<RiftBangPayload> {
        public void receive(RiftBangPayload payload, ClientPlayNetworking.Context context) {
            context.client().getSoundManager().play(PositionedSoundInstance.ui(ModSounds.DISTANT_BANG, 1));
        }
    }
}
