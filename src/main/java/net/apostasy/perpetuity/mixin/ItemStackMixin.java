package net.apostasy.perpetuity.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.apostasy.perpetuity.Perpetuity;
import net.apostasy.perpetuity.item.RemnantItem;
import net.apostasy.perpetuity.util.AdvancementUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @WrapOperation(method = "onDurabilityChange", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V"))
    private void perpetuity$replaceStackWithRemnant(ItemStack instance, int amount, Operation<Void> original, @Local(argsOnly = true) ServerPlayerEntity player) {
        ItemStack newStack = RemnantItem.create(instance);
        if (newStack == null) {
            original.call(instance, amount); // Calls normal stack.decrement(1);
            return;
        }

        int newSlot = player.getInventory().getEmptySlot();

        if (newSlot == -1) {
            player.dropItem(newStack, true, false);
        } else {
            player.getInventory().setStack(newSlot, newStack);
        }

        instance.decrement(1);

        AdvancementUtil.grantAdvancement(player, Perpetuity.id("obtain_remnant"));
    }
}
