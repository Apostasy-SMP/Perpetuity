package net.apostasy.perpetuity.mixin.echoes;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.apostasy.perpetuity.util.LUtil;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Chemthunder
 */
@Mixin(value = ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract void setHolder(@Nullable Entity holder);

    @Inject(method = "inventoryTick", at = @At(value = "HEAD"))
    private void perpetuity$fixItemStackHolder(World world, Entity entity, EquipmentSlot slot, CallbackInfo ci) {
        this.setHolder(entity);
    }

    @WrapMethod(method = "canPlaceOn")
    private boolean perpetuity$removePlacingBlocksAboveY(CachedBlockPosition pos, Operation<Boolean> original) {
        ItemStack self = (ItemStack) (Object) this;

        if (LUtil.effectsApplicable(self.getHolder())) {
            if (pos.getBlockPos().getY() >= LUtil.MAX_Y_HEIGHT) {
                return false;
            }
        }
        return original.call(pos);
    }

    @WrapMethod(method = "canBreak")
    private boolean perpetuity$removeBreakingBlocksAboveY(CachedBlockPosition pos, Operation<Boolean> original) {
        ItemStack self = (ItemStack) (Object) this;

        if (LUtil.effectsApplicable(self.getHolder())) {
            if (pos.getBlockPos().getY() >= LUtil.MAX_Y_HEIGHT) {
                return false;
            }
        }
        return original.call(pos);
    }

    @WrapMethod(method = "hasGlint")
    private boolean perpetuity$removeGlint(Operation<Boolean> original) {
        ItemStack self = (ItemStack) (Object) this;

        if (LUtil.effectsApplicable(self.getHolder())) {
            return false;
        }
        return original.call();
    }
}
