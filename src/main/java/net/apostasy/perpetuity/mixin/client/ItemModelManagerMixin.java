package net.apostasy.perpetuity.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.apostasy.perpetuity.PerpetuityConstants;
import net.apostasy.perpetuity.component.ModDataComponents;
import net.apostasy.perpetuity.registry.ModItems;
import net.minecraft.client.item.ItemAssetsLoader;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HeldItemContext;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Function;

@Mixin(ItemModelManager.class)
public class ItemModelManagerMixin {
    @Shadow
    @Final
    private Function<Identifier, ItemModel> modelGetter;

    @WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/model/ItemModel;update(Lnet/minecraft/client/render/item/ItemRenderState;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/item/ItemModelManager;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/util/HeldItemContext;I)V"))
    private void perpetuity$updateItemModel(ItemModel instance, ItemRenderState itemRenderState, ItemStack stack, ItemModelManager itemModelManager, ItemDisplayContext itemDisplayContext, ClientWorld clientWorld, HeldItemContext heldItemContext, int i, Operation<Void> original) {
        if (isGenericRemnant(stack)) {
            Identifier model = PerpetuityConstants.GENERIC_REMNANT_PREVIEWS.getFirst();
            modelGetter.apply(model).update(itemRenderState, stack, itemModelManager, itemDisplayContext, clientWorld, heldItemContext, i);
        } else original.call(instance, itemRenderState, stack, itemModelManager, itemDisplayContext, clientWorld, heldItemContext, i);
    }

    @Unique
    private boolean isGenericRemnant(ItemStack stack) {
        return stack.isOf(ModItems.REMNANT) && !stack.contains(ModDataComponents.REMNANT);
    }
}
