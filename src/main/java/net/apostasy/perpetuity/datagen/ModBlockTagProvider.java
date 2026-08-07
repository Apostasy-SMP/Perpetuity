package net.apostasy.perpetuity.datagen;

import net.apostasy.perpetuity.registry.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.@NonNull WrapperLookup registries) {
        builder(BlockTags.PICKAXE_MINEABLE)
                .setReplace(false)
                .add(key(ModBlocks.RENOVITE_BLOCK))
                .add(key(ModBlocks.RENOVITE_PYLON));
        builder(BlockTags.NEEDS_STONE_TOOL)
                .setReplace(false)
                .add(key(ModBlocks.RENOVITE_BLOCK))
                .add(key(ModBlocks.RENOVITE_PYLON));
    }

    protected RegistryKey<Block> key(Block block) {
        return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Registries.BLOCK.getEntry(block).getIdAsString()));
    }
}
