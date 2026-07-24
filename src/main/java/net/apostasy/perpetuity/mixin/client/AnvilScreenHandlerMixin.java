package net.apostasy.perpetuity.mixin.client;

import net.apostasy.perpetuity.Perpetuity;
import net.apostasy.perpetuity.component.ModDataComponents;
import net.apostasy.perpetuity.component.util.RemnantComponent;
import net.apostasy.perpetuity.registry.ModItems;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.ForgingSlotsManager;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {
    @Shadow
    @Final
    private Property levelCost;

    public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, ForgingSlotsManager forgingSlotsManager) {
        super(type, syncId, playerInventory, context, forgingSlotsManager);
    }

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void perpetuity$addRemnantAnvilRecipes(CallbackInfo ci) {
        levelCost.set(1);
        ItemStack stack1 = input.getStack(0);
        ItemStack stack2 = input.getStack(1);
        RemnantComponent component = stack1.get(ModDataComponents.REMNANT);
        if (component == null) return;

        if (component.data().resources().contains(stack2.getItem())) {
            ItemStack stack = component.item().copy();
            stack.setDamage(0);

            output.setStack(0, stack);
            sendContentUpdates();

            ci.cancel();
        } else levelCost.set(0);
    }
}
