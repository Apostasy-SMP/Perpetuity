package net.apostasy.perpetuity;

import net.apostasy.perpetuity.data.ModDataComponents;
import net.apostasy.perpetuity.registry.ModItems;
import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Perpetuity implements ModInitializer {
	public static final String MOD_ID = "perpetuity";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerModItems();
		ModDataComponents.registerDataComponents();
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
