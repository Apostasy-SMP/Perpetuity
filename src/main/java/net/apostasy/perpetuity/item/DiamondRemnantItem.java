package net.apostasy.perpetuity.item;

import net.apostasy.perpetuity.Perpetuity;
import net.apostasy.perpetuity.data.ModDataComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;

public class DiamondRemnantItem extends Item {
    public DiamondRemnantItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {

        if (stack.getComponents().get(ModDataComponents.REMNANT_ITEM) != null) {
            ItemStack or = stack.getComponents().get(ModDataComponents.REMNANT_ITEM);
            Perpetuity.LOGGER.info(or.getName().toString());
            Perpetuity.LOGGER.info(or.getEnchantments().toString());
        }

        return super.onClicked(stack, otherStack, slot, clickType, player, cursorStackReference);
    }
}
