package net.apostasy.perpetuity.mixin.client;

import net.apostasy.perpetuity.Perpetuity;
import net.apostasy.perpetuity.component.ModDataComponents;
import net.apostasy.perpetuity.component.util.RemnantComponent;
import net.apostasy.perpetuity.network.GrantAdvancementPayload;
import net.apostasy.perpetuity.registry.ModItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
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

@Environment(EnvType.CLIENT)
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
            stack.setDamage(stack.getMaxDamage()/10*9); // Repair with 10% of durability

            output.setStack(0, stack);
            sendContentUpdates();

            ci.cancel();
        } else if (stack2.isOf(ModItems.RENOVITE)) {
            ItemStack stack = component.item().copy();
            stack.setDamage(stack.getMaxDamage()/2); // Repair with 50% of durability

            output.setStack(0, stack);
            sendContentUpdates();

            ci.cancel();
        } else levelCost.set(0);
    }

    @Inject(method = "onTakeOutput", at = @At("HEAD"))
    private void perpetuity$grantRepairAdvancement(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (input.getStack(0).isOf(ModItems.REMNANT)) ClientPlayNetworking.send(new GrantAdvancementPayload(Perpetuity.id("remnant_anvil_repair"), player.getUuid()));
    }
}
