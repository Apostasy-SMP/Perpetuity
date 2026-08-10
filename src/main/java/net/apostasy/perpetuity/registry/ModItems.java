package net.apostasy.perpetuity.registry;

import net.apostasy.perpetuity.Perpetuity;
import net.apostasy.perpetuity.item.RemnantItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

import java.util.function.Function;

public class ModItems {
    public static final RemnantItem REMNANT = registerItem(
            "remnant",
            RemnantItem::new,
            RemnantItem.SETTINGS
    );

    public static final Item RENOVITE = registerItem(
            "renovite",
            Item::new,
            new Item.Settings()
    );

    public static <I extends Item> I registerItem(String name, Function<Item.Settings, I> itemFactory, Item.Settings settings) {
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Perpetuity.id(name));
        I item = itemFactory.apply(settings.registryKey(itemKey));
        Registry.register(Registries.ITEM, itemKey, item);
        return item;
    }

    public static <I extends BlockItem> I registerBlockItem(String name, Function<Item.Settings, I> itemFactory, Item.Settings settings) {
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Perpetuity.id(name));
        I item = itemFactory.apply(settings.registryKey(itemKey).useBlockPrefixedTranslationKey());
        Registry.register(Registries.ITEM, itemKey, item);
        return item;
    }

    public static final ItemGroup GROUP = FabricItemGroup.builder()
            .icon(REMNANT::getDefaultStack)
            .displayName(Text.translatable("itemGroup.perpetuity.perpetuity"))
            .build();

    public static void init() {
        Registry.register(Registries.ITEM_GROUP, Perpetuity.id(Perpetuity.MOD_ID), GROUP);
        ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(RegistryKeys.ITEM_GROUP, Perpetuity.id(Perpetuity.MOD_ID))).register(group -> {
            group.add(RENOVITE);
            group.add(ModBlocks.RENOVITE_BLOCK.asItem());
            group.add(ModBlocks.RENOVITE_PYLON.asItem());
            group.add(REMNANT);
            group.add(ModBlocks.EXPERIENCE_CAKE.asItem());
        });
    }
}
