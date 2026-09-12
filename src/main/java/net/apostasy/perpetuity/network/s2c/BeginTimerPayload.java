package net.apostasy.perpetuity.network.s2c;

import net.apostasy.perpetuity.Perpetuity;
import net.apostasy.perpetuity.client.event.TeleportOutOfEchoesEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record BeginTimerPayload(int timeTillRelease) implements CustomPayload {
    public static final Id<BeginTimerPayload> ID = new Id<>(Perpetuity.id("begin_timer"));

    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static final PacketCodec<RegistryByteBuf, BeginTimerPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, BeginTimerPayload::timeTillRelease,
            BeginTimerPayload::new
    );

    public static class Receiver implements ClientPlayNetworking.PlayPayloadHandler<BeginTimerPayload> {
        public void receive(BeginTimerPayload payload, ClientPlayNetworking.Context context) {
            TeleportOutOfEchoesEvents.execute(payload.timeTillRelease);
        }
    }
}
