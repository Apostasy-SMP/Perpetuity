package net.apostasy.perpetuity.client;

import net.apostasy.perpetuity.client.event.TeleportOutOfEchoesEvents;
import net.apostasy.perpetuity.client.geckolib.render.RenovitePylonRenderer;
import net.apostasy.perpetuity.client.render.EchoesSkyPass;
import net.apostasy.perpetuity.network.s2c.BeginTimerPayload;
import net.apostasy.perpetuity.network.s2c.RiftBangPayload;
import net.apostasy.perpetuity.registry.ModBlockEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.text.Text;

public class PerpetuityClient implements ClientModInitializer {
    public static final RenderStateDataKey<Boolean> IN_ECHOES = RenderStateDataKey.create();

    public void onInitializeClient() {
        BlockEntityRendererFactories.register(ModBlockEntities.RENOVITE_PYLON, (context) -> new RenovitePylonRenderer());
        EchoesSkyPass.init();

        TeleportOutOfEchoesEvents.init();

        ClientPlayNetworking.registerGlobalReceiver(BeginTimerPayload.ID, new BeginTimerPayload.Receiver());
        ClientPlayNetworking.registerGlobalReceiver(RiftBangPayload.ID, new RiftBangPayload.Receiver());
    }

    public static Text getSneakKeyName() {
        return Text.translatable(MinecraftClient.getInstance().options.sneakKey.getBoundKeyTranslationKey());
    }
}
