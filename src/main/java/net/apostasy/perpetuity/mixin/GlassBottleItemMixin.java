package net.apostasy.perpetuity.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.GlassBottleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlassBottleItem.class)
public abstract class GlassBottleItemMixin {

    @Shadow
    public abstract ActionResult use(World world, PlayerEntity user, Hand hand);

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void perpetuity$fillBottleWithXp(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = user.getStackInHand(hand);

        if (!user.isSneaking()) return;
        if (user.totalExperience < 11) return; // i made it 11 because that's the most an xp bottle can drop

        user.getInventory().offerOrDrop(Items.EXPERIENCE_BOTTLE.getDefaultStack());
        user.playSound(SoundEvents.ITEM_BOTTLE_FILL);
        user.addExperience(-11);
        
        stack.decrementUnlessCreative(1, user);

        cir.setReturnValue(ActionResult.SUCCESS);
        cir.cancel();
    }
}
