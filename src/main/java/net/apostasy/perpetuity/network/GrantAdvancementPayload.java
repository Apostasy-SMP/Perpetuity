package net.apostasy.perpetuity.network;

import net.apostasy.perpetuity.Perpetuity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record GrantAdvancementPayload(Identifier advancement, UUID player) implements CustomPayload {
    public static final Id<GrantAdvancementPayload> ID = new Id<>(Perpetuity.id("grant_advancement"));
    public static final PacketCodec<RegistryByteBuf, GrantAdvancementPayload> PACKET_CODEC =
            PacketCodec.tuple(Identifier.PACKET_CODEC, GrantAdvancementPayload::advancement, Uuids.PACKET_CODEC, GrantAdvancementPayload::player, GrantAdvancementPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
