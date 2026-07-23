package net.apostasy.perpetuity.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.apostasy.perpetuity.Perpetuity;
import net.apostasy.perpetuity.data.ModDataComponents;
import net.apostasy.perpetuity.registry.ModItems;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    public abstract @Nullable LivingEntity getEntity();

    @Shadow
    public abstract @Nullable ItemEntity dropItem(ItemStack stack, boolean dropAtSelf, boolean retainOwnership);

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;damage" +
            "(ILnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;)V"), method = "damageEquipment", locals = LocalCapture.CAPTURE_FAILSOFT)
    public void bypassBreak(DamageSource source, float amount, EquipmentSlot[] slots, CallbackInfo ci, @Local(name = "itemStack") ItemStack itemStack) {
        if (this.getEntity().getEntityWorld() instanceof ServerWorld) {
            if (getEntity() instanceof ServerPlayerEntity player) {
                if (itemStack.hasEnchantments() && itemStack.getDamage() >= itemStack.getMaxDamage() - 1) {
                    Item remnant = getRemnant(itemStack);
                    if (remnant == null) return;

                    ItemStack newStack = new ItemStack(remnant);
                    newStack.set(ModDataComponents.REMNANT_ITEM, itemStack);
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

    @Unique
    public Item getRemnant(ItemStack stack) {
        List<Item> DIAMOND_ITEMS = List.of(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS,
                Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE, Items.DIAMOND_SPEAR);

        if (DIAMOND_ITEMS.contains(stack.getItem())) return ModItems.DIAMOND_REMNANT;

        return null;
    }
}
