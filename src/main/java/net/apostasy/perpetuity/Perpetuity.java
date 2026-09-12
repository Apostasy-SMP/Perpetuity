package net.apostasy.perpetuity;

import net.apostasy.perpetuity.command.TeleportToEchoesCommand;
import net.apostasy.perpetuity.component.ModDataComponents;
import net.apostasy.perpetuity.event.PortalEchoesEvent;
import net.apostasy.perpetuity.network.c2s.GrantAdvancementPayload;
import net.apostasy.perpetuity.network.c2s.TeleportOutOfEchoesPayload;
import net.apostasy.perpetuity.network.s2c.BeginTimerPayload;
import net.apostasy.perpetuity.network.s2c.RiftBangPayload;
import net.apostasy.perpetuity.registry.*;
import net.apostasy.perpetuity.remnant.RemnantDataCollector;
import net.apostasy.perpetuity.util.AdvancementUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Perpetuity implements ModInitializer {
	public static final String MOD_ID = "perpetuity";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public void onInitialize() {
		ModItems.init();
		ModDataComponents.init();
		ModBlocks.init();
		ModStats.init();
		ModBlockEntities.init();
		ModWorldGen.init();
		ModSounds.init();

		ResourceLoader.get(ResourceType.SERVER_DATA).registerReloader(id("remnant_data"), new RemnantDataCollector());

		CommandRegistrationCallback.EVENT.register(new TeleportToEchoesCommand());
		ServerTickEvents.START_WORLD_TICK.register(new PortalEchoesEvent());

		PayloadTypeRegistry.playC2S().register(GrantAdvancementPayload.ID, GrantAdvancementPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(TeleportOutOfEchoesPayload.ID, TeleportOutOfEchoesPayload.CODEC);

		PayloadTypeRegistry.playS2C().register(BeginTimerPayload.ID, BeginTimerPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(RiftBangPayload.ID, RiftBangPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(GrantAdvancementPayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.server().getPlayerManager().getPlayer(payload.player());
			if (player != null) AdvancementUtil.grantAdvancement(player, payload.advancement());
		});

		ServerPlayNetworking.registerGlobalReceiver(TeleportOutOfEchoesPayload.ID, new TeleportOutOfEchoesPayload.Receiver());
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
