package net.apostasy.perpetuity.item;

import net.apostasy.perpetuity.Perpetuity;
import net.apostasy.perpetuity.component.ModDataComponents;
import net.apostasy.perpetuity.component.util.RemnantComponent;
import net.apostasy.perpetuity.component.util.ToolInfoComponent;
import net.apostasy.perpetuity.registry.ModItems;
import net.apostasy.perpetuity.remnant.RemnantData;
import net.apostasy.perpetuity.remnant.RemnantDataCollector;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class RemnantItem extends Item {
    public static Settings SETTINGS = new Item.Settings().maxCount(1);

    public RemnantItem(Settings settings) {
        super(settings);
    }

    /// @return `ItemStack` instance, or `null` if stack's item is not registered under a remnant type
    public static ItemStack create(ItemStack stack) {
        Identifier remnantId = RemnantDataCollector.remnantMappings.getOrDefault(stack.getItem(), null);
        if (remnantId == null) return null;
        RemnantData data = RemnantDataCollector.remnantTypes.getOrDefault(remnantId, null);
        if (data == null) return null;

        ItemStack storedStack = stack.copy();
        ToolInfoComponent toolInfo = storedStack.getOrDefault(ModDataComponents.TOOL_INFO, new ToolInfoComponent(0));
        storedStack.set(ModDataComponents.TOOL_INFO, new ToolInfoComponent(toolInfo.timesBroken() + 1));

        ItemStack returnStack = new ItemStack(ModItems.REMNANT);
        returnStack.set(ModDataComponents.REMNANT, new RemnantComponent(
                storedStack,
                data
        ));
        returnStack.set(DataComponentTypes.ITEM_MODEL, remnantId);
        return returnStack;
    }

    public static ItemStack repair(ItemStack stack, PlayerEntity player, float percentageRepaired) {
        percentageRepaired = Math.clamp(percentageRepaired, 0.0F, 1.0F);
        RemnantComponent component = stack.get(ModDataComponents.REMNANT);
        if (component == null) return null;
        ItemStack returnStack = component.item();
        returnStack.setDamage(Math.clamp((int) (returnStack.getMaxDamage() * (1 - percentageRepaired)), 0, returnStack.getMaxDamage()));

        int slot = player.getInventory().getSlotWithStack(stack);
        player.getInventory().setStack(slot, returnStack);

        return returnStack;
    }

    @Override
    public Text getName(ItemStack stack) {
        RemnantComponent component = stack.get(ModDataComponents.REMNANT);
        if (component == null) return super.getName(stack);
        return component.data().name();
    }
}
