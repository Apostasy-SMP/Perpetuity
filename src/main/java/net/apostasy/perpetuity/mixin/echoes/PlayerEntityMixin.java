package net.apostasy.perpetuity.mixin.echoes;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.apostasy.perpetuity.util.EchoUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

/**
 * @author Chemthunder
 */
@Mixin(value = PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @WrapMethod(method = "canPlaceOn")
    private boolean perpetuity$denyPlacingBlocksAboveMaxLevel(BlockPos pos, Direction facing, ItemStack stack, Operation<Boolean> original) {
        if (EchoUtil.effectsApplicable((PlayerEntity) (Object) this)) {
            if (pos.getY() >= EchoUtil.MAX_Y_HEIGHT) {
                return false;
            }
        }
        return original.call(pos, facing, stack);
    }

    @WrapMethod(method = "isBlockBreakingRestricted")
    private boolean perpetuity$removeBlockBreaking(World world, BlockPos pos, GameMode gameMode, Operation<Boolean> original) {
        if (EchoUtil.effectsApplicable((PlayerEntity) (Object) this)) {
            if (pos.getY() >= EchoUtil.MAX_Y_HEIGHT) {
                return false;
            }
        }
        return original.call(world, pos, gameMode);
    }
}
