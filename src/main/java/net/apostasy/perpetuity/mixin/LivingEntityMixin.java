package net.apostasy.perpetuity.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.apostasy.perpetuity.item.RemnantItem;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    public abstract @Nullable LivingEntity getEntity();

    @Shadow
    public abstract @Nullable ItemEntity dropItem(ItemStack stack, boolean dropAtSelf, boolean retainOwnership);

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;damage" +
            "(ILnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;)V"), method = "damageEquipment", locals = LocalCapture.CAPTURE_FAILSOFT)
    public void bypassBreak(DamageSource source, float amount, EquipmentSlot[] slots, CallbackInfo ci, @Local(name = "itemStack") ItemStack itemStack) {
        if (this.getEntity() != null && this.getEntity().getEntityWorld() instanceof ServerWorld) {
            if (getEntity() instanceof ServerPlayerEntity player) {
                if (itemStack.hasEnchantments() && itemStack.getDamage() >= itemStack.getMaxDamage() - 1) {
                    Item remnant = RemnantItem.create(itemStack);
                    if (remnant == null) return;

                    ItemStack newStack = new ItemStack(remnant);
                    int newSlot = player.getInventory().getEmptySlot();

                    if (newSlot == -1) {
                        dropItem(newStack, true, false);
                    } else {
                        player.getInventory().setStack(newSlot, newStack);
                    }
                }
            }
        }
    }
}
