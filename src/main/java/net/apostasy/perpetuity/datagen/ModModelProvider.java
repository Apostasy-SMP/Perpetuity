package net.apostasy.perpetuity.datagen;

import net.apostasy.perpetuity.registry.ModBlocks;
import net.apostasy.perpetuity.registry.ModItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.Block;
import net.minecraft.block.CakeBlock;
import net.minecraft.client.data.*;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class ModModelProvider extends FabricModelProvider {
    public static final Model CAKE = new Model(Optional.of(Identifier.ofVanilla("block/cake")), Optional.empty(), TextureKey.PARTICLE, TextureKey.TOP, TextureKey.BOTTOM, TextureKey.SIDE);
    public static final Model CAKE_SLICE1 = new Model(Optional.of(Identifier.ofVanilla("block/cake_slice1")), Optional.empty(), TextureKey.PARTICLE, TextureKey.TOP, TextureKey.BOTTOM, TextureKey.SIDE, TextureKey.INSIDE);
    public static final Model CAKE_SLICE2 = new Model(Optional.of(Identifier.ofVanilla("block/cake_slice2")), Optional.empty(), TextureKey.PARTICLE, TextureKey.TOP, TextureKey.BOTTOM, TextureKey.SIDE, TextureKey.INSIDE);
    public static final Model CAKE_SLICE3 = new Model(Optional.of(Identifier.ofVanilla("block/cake_slice3")), Optional.empty(), TextureKey.PARTICLE, TextureKey.TOP, TextureKey.BOTTOM, TextureKey.SIDE, TextureKey.INSIDE);
    public static final Model CAKE_SLICE4 = new Model(Optional.of(Identifier.ofVanilla("block/cake_slice4")), Optional.empty(), TextureKey.PARTICLE, TextureKey.TOP, TextureKey.BOTTOM, TextureKey.SIDE, TextureKey.INSIDE);
    public static final Model CAKE_SLICE5 = new Model(Optional.of(Identifier.ofVanilla("block/cake_slice5")), Optional.empty(), TextureKey.PARTICLE, TextureKey.TOP, TextureKey.BOTTOM, TextureKey.SIDE, TextureKey.INSIDE);
    public static final Model CAKE_SLICE6 = new Model(Optional.of(Identifier.ofVanilla("block/cake_slice6")), Optional.empty(), TextureKey.PARTICLE, TextureKey.TOP, TextureKey.BOTTOM, TextureKey.SIDE, TextureKey.INSIDE);

    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {
        createCake(ModBlocks.EXPERIENCE_CAKE, generator);
    }

    @Override
    public void generateItemModels(ItemModelGenerator generator) {
//        generator.register(ModItems.REMNANT, Models.GENERATED); // Unnecessary
        generator.register(ModBlocks.EXPERIENCE_CAKE.asItem(), Models.GENERATED);
    }

    private static void createCake(Block cakeBlock, BlockStateModelGenerator generator) {
        TextureMap textureMap = new TextureMap()
                .put(TextureKey.PARTICLE, TextureMap.getSubId(cakeBlock, "_side"))
                .put(TextureKey.TOP, TextureMap.getSubId(cakeBlock, "_top"))
                .put(TextureKey.BOTTOM, TextureMap.getSubId(cakeBlock, "_bottom"))
                .put(TextureKey.SIDE, TextureMap.getSubId(cakeBlock, "_side"))
                .put(TextureKey.INSIDE, TextureMap.getSubId(cakeBlock, "_inner"));

        BlockModelDefinitionCreator states = VariantsBlockModelDefinitionCreator.of(cakeBlock)
                .with(BlockStateVariantMap.models(CakeBlock.BITES)
                        .register(0, BlockStateModelGenerator.createWeightedVariant(CAKE.upload(cakeBlock, textureMap, generator.modelCollector)))
                        .register(1, BlockStateModelGenerator.createWeightedVariant(CAKE_SLICE1.upload(cakeBlock, "_slice1", textureMap, generator.modelCollector)))
                        .register(2, BlockStateModelGenerator.createWeightedVariant(CAKE_SLICE2.upload(cakeBlock, "_slice2", textureMap, generator.modelCollector)))
                        .register(3, BlockStateModelGenerator.createWeightedVariant(CAKE_SLICE3.upload(cakeBlock, "_slice3", textureMap, generator.modelCollector)))
                        .register(4, BlockStateModelGenerator.createWeightedVariant(CAKE_SLICE4.upload(cakeBlock, "_slice4", textureMap, generator.modelCollector)))
                        .register(5, BlockStateModelGenerator.createWeightedVariant(CAKE_SLICE5.upload(cakeBlock, "_slice5", textureMap, generator.modelCollector)))
                        .register(6, BlockStateModelGenerator.createWeightedVariant(CAKE_SLICE6.upload(cakeBlock, "_slice6", textureMap, generator.modelCollector)))
                );

        generator.blockStateCollector.accept(states);
    }
}
