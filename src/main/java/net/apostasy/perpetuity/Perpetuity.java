package net.apostasy.perpetuity;

import net.apostasy.perpetuity.component.ModDataComponents;
import net.apostasy.perpetuity.network.GrantAdvancementPayload;
import net.apostasy.perpetuity.registry.ModBlocks;
import net.apostasy.perpetuity.registry.ModItems;
import net.apostasy.perpetuity.registry.ModStats;
import net.apostasy.perpetuity.remnant.RemnantDataCollector;
import net.apostasy.perpetuity.util.AdvancementUtil;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Perpetuity implements ModInitializer {
	public static final String MOD_ID = "perpetuity";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.init();
		ModDataComponents.init();
		ModBlocks.init();
		ModStats.init();
		ResourceLoader.get(ResourceType.SERVER_DATA).registerReloader(id("remnant_data"), new RemnantDataCollector());

		PayloadTypeRegistry.playC2S().register(GrantAdvancementPayload.ID, GrantAdvancementPayload.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(GrantAdvancementPayload.ID, (payload, context) -> AdvancementUtil.grantAdvancement(context.player(), payload.advancement()));
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
