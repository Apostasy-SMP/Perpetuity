package net.apostasy.perpetuity.item;

import net.apostasy.perpetuity.component.PerpetuityDataComponents;
import net.apostasy.perpetuity.component.util.RemnantComponent;
import net.apostasy.perpetuity.registry.ModItems;
import net.apostasy.perpetuity.remnant.RemnantData;
import net.apostasy.perpetuity.remnant.RemnantDataCollector;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class RemnantItem extends Item {
    public static Settings SETTINGS = new Item.Settings();

    public RemnantItem(Settings settings) {
        super(settings);
    }

    /// @return `ItemStack` instance, or `null` if stack's item is not registered under a remnant type
    public static ItemStack create(ItemStack stack) {
        Identifier remnantId = RemnantDataCollector.remnantMappings.getOrDefault(stack.getItem(), null);
        if (remnantId == null) return null;
        RemnantData data = RemnantDataCollector.remnantTypes.getOrDefault(remnantId, null);
        if (data == null) return null;

        ItemStack returnStack = new ItemStack(ModItems.REMNANT);
        returnStack.set(PerpetuityDataComponents.REMNANT, new RemnantComponent(
                stack.copy(),
                data.texture()
        ));
        return returnStack;
    }
}
