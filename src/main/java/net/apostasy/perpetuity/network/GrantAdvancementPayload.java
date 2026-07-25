package net.apostasy.perpetuity.network;

import net.apostasy.perpetuity.Perpetuity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record GrantAdvancementPayload(Identifier advancement) implements CustomPayload {
    public static final Id<GrantAdvancementPayload> ID = new Id<>(Perpetuity.id("grant_advancement"));
    public static final PacketCodec<RegistryByteBuf, GrantAdvancementPayload> PACKET_CODEC =
            PacketCodec.tuple(Identifier.PACKET_CODEC, GrantAdvancementPayload::advancement, GrantAdvancementPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
