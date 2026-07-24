package net.apostasy.perpetuity.registry;

import net.apostasy.perpetuity.Perpetuity;
import net.apostasy.perpetuity.item.RemnantItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import java.util.function.Function;

public class ModItems {
    public static final RemnantItem REMNANT = registerItem(
            "remnant",
            RemnantItem::new,
            RemnantItem.SETTINGS
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

    public static void init() {

    }
}
