package net.apostasy.perpetuity.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModLangProvider extends FabricLanguageProvider {
    public ModLangProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add("item.perpetuity.remnant", "Remnant"); // Generic name in case something goes wrong :c
        translationBuilder.add("item.remnant.copper", "Copper Remnant");
        translationBuilder.add("item.remnant.iron", "Iron Remnant");
        translationBuilder.add("item.remnant.gold", "Golden Remnant");
        translationBuilder.add("item.remnant.lapis", "Lapis Remnant");
        translationBuilder.add("item.remnant.diamond", "Diamond Remnant");
        translationBuilder.add("item.remnant.netherite", "Netherite Remnant");
        translationBuilder.add("item.remnant.experience", "Experience Remnant");

        translationBuilder.add("block.perpetuity.experience_cake", "Experience Cake");

        translationBuilder.add("advancements.perpetuity.root.title", "Perpetuity");
        translationBuilder.add("advancements.perpetuity.root.description", "Welcome to Perpetuity!");
        translationBuilder.add("advancements.perpetuity.obtain_remnant.title", "Gone but Not Forgotten");
        translationBuilder.add("advancements.perpetuity.obtain_remnant.description", "Break an item to get its Remnant");
        translationBuilder.add("advancements.perpetuity.obtain_experience_cake.title", "That's Crazy");
        translationBuilder.add("advancements.perpetuity.obtain_experience_cake.description", "Craft.");
    }
}
