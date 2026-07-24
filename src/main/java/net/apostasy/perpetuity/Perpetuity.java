package net.apostasy.perpetuity;

import net.apostasy.perpetuity.component.ModDataComponents;
import net.apostasy.perpetuity.registry.ModBlocks;
import net.apostasy.perpetuity.registry.ModItems;
import net.apostasy.perpetuity.registry.ModStats;
import net.apostasy.perpetuity.remnant.RemnantDataCollector;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

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
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
