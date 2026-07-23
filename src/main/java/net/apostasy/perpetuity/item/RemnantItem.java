package net.apostasy.perpetuity.item;

import net.apostasy.perpetuity.component.PerpetuityDataComponents;
import net.apostasy.perpetuity.component.util.RemnantComponent;
import net.apostasy.perpetuity.remnant.RemnantDataCollector;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class RemnantItem extends Item {
    public static Settings SETTINGS = new Item.Settings();

    public RemnantItem(Settings settings) {
        super(SETTINGS);
    }

    /// @return `RemnantItem` instance, or `null` if stack's item is not registered under a remnant type
    public static RemnantItem create(ItemStack stack) {
        Identifier texture = RemnantDataCollector.remnantMappings.getOrDefault(stack.getItem(), null);
        if (texture == null) return null;

        Settings settings = RemnantItem.SETTINGS;
        settings.component(PerpetuityDataComponents.REMNANT, new RemnantComponent(
                stack.copy(),
                texture
        ));
        return new RemnantItem(settings);
    }
}
