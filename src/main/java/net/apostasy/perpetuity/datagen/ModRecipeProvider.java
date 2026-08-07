package net.apostasy.perpetuity.datagen;

import net.apostasy.perpetuity.registry.ModBlocks;
import net.apostasy.perpetuity.registry.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Blocks;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected @NonNull RecipeGenerator getRecipeGenerator(RegistryWrapper.@NonNull WrapperLookup registries, @NonNull RecipeExporter exporter) {
        return new RecipeGenerator(registries, exporter) {
            @Override
            public void generate() {
                createShaped(RecipeCategory.MISC, ModBlocks.RENOVITE_PYLON)
                        .pattern(" R ")
                        .pattern(" R ")
                        .pattern("DAD")
                        .input('D', Blocks.POLISHED_DEEPSLATE_SLAB)
                        .input('A', Blocks.AMETHYST_BLOCK)
                        .input('R', ModBlocks.RENOVITE_BLOCK);

                createShaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.RENOVITE_BLOCK)
                        .pattern("RRR")
                        .pattern("RRR")
                        .pattern("RRR")
                        .input('R', ModItems.RENOVITE);

                createShapeless(RecipeCategory.TOOLS, ModItems.RENOVITE)
                        .input(Blocks.AMETHYST_CLUSTER)
                        .input(Items.EXPERIENCE_BOTTLE, 4);

                createShapeless(RecipeCategory.MISC, ModItems.RENOVITE, 9)
                        .input(ModBlocks.RENOVITE_BLOCK);

                createShaped(RecipeCategory.FOOD, ModBlocks.EXPERIENCE_CAKE)
                        .pattern("EEE")
                        .pattern("SGS")
                        .pattern("WWW")
                        .input('E', Items.EXPERIENCE_BOTTLE)
                        .input('S', Items.SUGAR)
                        .input('W', Items.WHEAT)
                        .input('G', ItemTags.EGGS);
            }
        };
    }

    @Override
    public String getName() {
        return "recipes";
    }
}
