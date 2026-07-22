package net.apostasy.perpetuity.registry;

import net.apostasy.perpetuity.Perpetuity;
import net.apostasy.perpetuity.item.DiamondRemnantItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.ArmorMaterials;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import java.util.function.Function;

public class ModItems {
    public static final Item DIAMOND_REMNANT = registerItem("diamond_remnant", Item::new);
//    public static final Item DIAMOND_REMNANT = registerCustomItem("diamond_remnant", Item::new, new Item.Settings().armor(ArmorMaterials.DIAMOND, EquipmentType.CHESTPLATE));

    private static Item registerItem(String name, Function<Item.Settings, Item> function) {
        return Registry.register(Registries.ITEM, Perpetuity.id(name),
                function.apply(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Perpetuity.id(name)))));
    }

    private static Item registerCustomItem(String name, Function<Item.Settings, Item> factory, Item.Settings itemSettings) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Perpetuity.id(name));
        Item item = factory.apply(itemSettings.registryKey(key));
        return Registry.register(Registries.ITEM, key, item);
    }

    public static void registerModItems() {
        Perpetuity.LOGGER.info(Perpetuity.MOD_ID + " || Registering ModItems");
    }

}
