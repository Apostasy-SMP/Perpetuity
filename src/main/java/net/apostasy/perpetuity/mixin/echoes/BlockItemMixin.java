package net.apostasy.perpetuity.mixin.echoes;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.apostasy.perpetuity.util.EchoUtil;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;

/**
 * @author Chemthunder
 */
@Mixin(value = BlockItem.class)
public abstract class BlockItemMixin {

    @WrapMethod(method = "canPlace")
    private boolean perpetuity$denyPlace(ItemPlacementContext context, BlockState state, Operation<Boolean> original) {
        BlockPos pos = context.getBlockPos();
        ItemStack stack = context.getStack();

        if (EchoUtil.effectsApplicable(stack.getHolder()) && pos.getY() >= EchoUtil.MAX_Y_HEIGHT) {
            return false;
        }
        return original.call(context, state);
    }

    @WrapMethod(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;")
    private ActionResult perpetuity$denyPlace(ItemPlacementContext context, Operation<ActionResult> original) {
        BlockPos pos = context.getBlockPos();
        ItemStack stack = context.getStack();

        if (EchoUtil.effectsApplicable(stack.getHolder()) && pos.getY() >= EchoUtil.MAX_Y_HEIGHT) {
            return ActionResult.FAIL;
        }
        return original.call(context);
    }
}
