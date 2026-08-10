package net.apostasy.perpetuity.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class ModLangProvider extends FabricLanguageProvider {
    public ModLangProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.@NonNull WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add("itemGroup.perpetuity.perpetuity", "Perpetuity");

        translationBuilder.add("item.perpetuity.remnant", "Remnant"); // Generic name in case something goes wrong :c
        translationBuilder.add("item.remnant.copper", "Copper Remnant");
        translationBuilder.add("item.remnant.iron", "Iron Remnant");
        translationBuilder.add("item.remnant.gold", "Golden Remnant");
        translationBuilder.add("item.remnant.lapis", "Lapis Remnant");
        translationBuilder.add("item.remnant.diamond", "Diamond Remnant");
        translationBuilder.add("item.remnant.netherite", "Netherite Remnant");
        translationBuilder.add("item.remnant.experience", "Experience Remnant");
        translationBuilder.add("item.remnant.trident", "Trident Remnant");
        translationBuilder.add("item.remnant.mace", "Mace Remnant");
        translationBuilder.add("item.remnant.bow", "Bow Remnant");
        translationBuilder.add("item.remnant.crossbow", "Crossbow Remnant");
        translationBuilder.add("item.remnant.scute", "Scute Remnant");
        translationBuilder.add("item.remnant.amethyst", "Amethyst Remnant");

        translationBuilder.add("item.perpetuity.renovite", "Renovite");

        translationBuilder.add("block.perpetuity.experience_cake", "Experience Cake");
        translationBuilder.add("block.perpetuity.renovite_pylon", "Renovite Pylon");
        translationBuilder.add("block.perpetuity.renovite_block", "Renovite Block");

        translationBuilder.add("advancements.perpetuity.root.title", "Perpetuity");
        translationBuilder.add("advancements.perpetuity.root.description", "Welcome to Perpetuity!");
        translationBuilder.add("advancements.perpetuity.obtain_remnant.title", "Gone but Not Forgotten");
        translationBuilder.add("advancements.perpetuity.obtain_remnant.description", "Break an item to get its Remnant");
        translationBuilder.add("advancements.perpetuity.experience_cake_repair.title", "That's Crazy.");
        translationBuilder.add("advancements.perpetuity.experience_cake_repair.description", "Craft.");
        translationBuilder.add("advancements.perpetuity.remnant_anvil_repair.title", "Back to The Basics");
        translationBuilder.add("advancements.perpetuity.remnant_anvil_repair.description", "Repair a Remnant the old fashioned way");

        translationBuilder.add("tooltip.remnant.repairs_into", "Repairs into ");
        translationBuilder.add("tooltip.remnant.preview", "[%1$s] to preview item");
        translationBuilder.add("tooltip.tool.times_broken", "Broken %1$s times");
    }
}
