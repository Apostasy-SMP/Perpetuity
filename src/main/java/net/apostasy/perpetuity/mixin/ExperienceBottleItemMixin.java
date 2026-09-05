package net.apostasy.perpetuity.mixin;

import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ExperienceBottleItem.class)
public abstract class ExperienceBottleItemMixin extends Item {
    public ExperienceBottleItemMixin(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack getRecipeRemainder(ItemStack stack) {
        return new ItemStack(Items.GLASS_BOTTLE).copyWithCount(stack.getCount());
    }
}
