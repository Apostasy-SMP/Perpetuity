package net.apostasy.perpetuity.datagen;

import net.apostasy.perpetuity.registry.ModTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.@NonNull WrapperLookup registries) {
        builder(ModTags.IGNORED_BY_PYLON);
        builder(ModTags.UNREPAIRABLE_WITH_RENOVITE);
    }

    protected RegistryKey<Item> key(Item item) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Registries.ITEM.getEntry(item).getIdAsString()));
    }
}
