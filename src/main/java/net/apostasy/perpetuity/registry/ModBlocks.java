package net.apostasy.perpetuity.registry;

import net.apostasy.perpetuity.Perpetuity;
import net.apostasy.perpetuity.block.ExperienceCakeBlock;
import net.apostasy.perpetuity.block.RenovitePylonBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;

import java.util.function.Function;

public class ModBlocks {
    public static final Block EXPERIENCE_CAKE = register(
            "experience_cake",
            ExperienceCakeBlock::new,
            AbstractBlock.Settings.create()
                    .solid()
                    .strength(0.5F)
                    .sounds(BlockSoundGroup.WOOL)
                    .pistonBehavior(PistonBehavior.DESTROY)
                    .mapColor(MapColor.DARK_GREEN),
            true
    );

    public static final Block RENOVITE_PYLON = register(
            "renovite_pylon",
            RenovitePylonBlock::new,
            AbstractBlock.Settings.create()
                    .solid()
                    .strength(3.0F)
                    .sounds(BlockSoundGroup.TUFF)
                    .pistonBehavior(PistonBehavior.BLOCK)
                    .mapColor(MapColor.GRAY)
                    .requiresTool(),
            1
    );

    public static final Block RENOVITE_BLOCK = register(
            "renovite_block",
            Block::new,
            AbstractBlock.Settings.create()
                    .solid()
                    .strength(3.0F)
                    .sounds(BlockSoundGroup.TUFF)
                    .mapColor(MapColor.GRAY)
                    .requiresTool(),
            true
    );

    private static Block register(String name, Function<AbstractBlock.Settings, Block> blockFactory, AbstractBlock.Settings settings, boolean shouldRegisterItem) {
        RegistryKey<Block> blockKey = keyOfBlock(name);
        Block block = blockFactory.apply(settings.registryKey(blockKey));

        if (shouldRegisterItem) {
            RegistryKey<Item> itemKey = keyOfItem(name);

            BlockItem blockItem = new BlockItem(block, new Item.Settings().registryKey(itemKey).useBlockPrefixedTranslationKey());
            Registry.register(Registries.ITEM, itemKey, blockItem);
        }

        return Registry.register(Registries.BLOCK, blockKey, block);
    }

    private static Block register(String name, Function<AbstractBlock.Settings, Block> blockFactory, AbstractBlock.Settings settings, int stackSize) {
        RegistryKey<Block> blockKey = keyOfBlock(name);
        Block block = blockFactory.apply(settings.registryKey(blockKey));

        RegistryKey<Item> itemKey = keyOfItem(name);

        BlockItem blockItem = new BlockItem(block, new Item.Settings().registryKey(itemKey).useBlockPrefixedTranslationKey().maxCount(stackSize));
        Registry.register(Registries.ITEM, itemKey, blockItem);

        return Registry.register(Registries.BLOCK, blockKey, block);
    }

    private static RegistryKey<Block> keyOfBlock(String name) {
        return RegistryKey.of(RegistryKeys.BLOCK, Perpetuity.id(name));
    }

    private static RegistryKey<Item> keyOfItem(String name) {
        return RegistryKey.of(RegistryKeys.ITEM, Perpetuity.id(name));
    }

    public static void init() {

    }
}
