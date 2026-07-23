package net.apostasy.perpetuity;

import net.apostasy.perpetuity.registry.ModItems;
import net.apostasy.perpetuity.remnant.RemnantDataCollector;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Perpetuity implements ModInitializer {
	public static final String MOD_ID = "perpetuity";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerModItems();
		ResourceLoader.get(ResourceType.SERVER_DATA).registerReloader(id("remnant_data"), new RemnantDataCollector());
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
