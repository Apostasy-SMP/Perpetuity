package net.apostasy.perpetuity.mixin.echoes.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.apostasy.perpetuity.client.PerpetuityClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.RenderCommandQueue;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * @author Chemthunder
 */
@Mixin(value = EquipmentRenderer.class)
public abstract class EquipmentRendererMixin {

    @WrapOperation(
            method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/util/Identifier;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/command/RenderCommandQueue;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/RenderLayer;IIILnet/minecraft/client/texture/Sprite;ILnet/minecraft/client/render/command/ModelCommandRenderer$CrumblingOverlayCommand;)V",
                    ordinal = 1
            )
    )
    private <S> void perpetuity$hideArmorGlint(RenderCommandQueue instance, Model<? super S> model, S s, MatrixStack matrixStack, RenderLayer renderLayer, int light, int overlay, int color, @Nullable Sprite sprite, int i, ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlayCommand, Operation<Void> original) {
        if (s instanceof BipedEntityRenderState state) {
            if (Boolean.TRUE.equals(state.getData(PerpetuityClient.IN_ECHOES))) {
                return;
            }
        }
        original.call(instance, model, s, matrixStack, renderLayer, light, overlay, color, sprite, i, crumblingOverlayCommand);
    }
}
