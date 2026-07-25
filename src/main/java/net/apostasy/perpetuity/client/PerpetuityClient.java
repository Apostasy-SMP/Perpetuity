package net.apostasy.perpetuity.client;

import net.apostasy.perpetuity.PerpetuityConstants;
import net.apostasy.perpetuity.client.geckolib.render.RenovitePylonRenderer;
import net.apostasy.perpetuity.registry.ModBlockEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class PerpetuityClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) return;
            if (client.world.getTime() % 10 == 0) {
                Identifier id = PerpetuityConstants.GENERIC_REMNANT_PREVIEWS.removeFirst();
                PerpetuityConstants.GENERIC_REMNANT_PREVIEWS.addLast(id); // who up making their constants anything but constant 🗣️🗣️🔥 ~Aussie
            }
        });

        BlockEntityRendererFactories.register(ModBlockEntities.RENOVITE_PYLON, (context) -> new RenovitePylonRenderer());
    }

    public static Text getSneakKeyName() {
        return Text.translatable(MinecraftClient.getInstance().options.sneakKey.getBoundKeyTranslationKey());
    }
}
