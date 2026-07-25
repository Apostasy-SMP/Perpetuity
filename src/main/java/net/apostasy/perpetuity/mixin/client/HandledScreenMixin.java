package net.apostasy.perpetuity.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.apostasy.perpetuity.component.ModDataComponents;
import net.apostasy.perpetuity.component.util.RemnantComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Optional;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @Shadow
    protected abstract List<Text> getTooltipFromItem(ItemStack stack);

    @WrapOperation(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/util/Identifier;)V"))
    private void perpetuity$drawRemnantPreviewTooltip(DrawContext context, TextRenderer textRenderer, List<Text> text, Optional<TooltipData> data, int x, int y, @Nullable Identifier texture, Operation<Void> original, @Local ItemStack stack) {
        if (!(MinecraftClient.getInstance().options.sneakKey instanceof KeyBindingAccessor accessor)) return;

        Window window = MinecraftClient.getInstance().getWindow();
        int boundKeyCode = accessor.perpetuity$getBoundKey().getCode();

        RemnantComponent component = stack.get(ModDataComponents.REMNANT);
        if (component != null && InputUtil.isKeyPressed(window, boundKeyCode)) {
            context.drawTooltip(
                    textRenderer, getTooltipFromItem(component.item()), component.item().getTooltipData(), x, y, component.item().get(DataComponentTypes.TOOLTIP_STYLE)
            );
        } else original.call(context, textRenderer, text, data, x, y, texture);
    }

    @Mixin(KeyBinding.class)
    public interface KeyBindingAccessor {
        @Accessor("boundKey")
        InputUtil.Key perpetuity$getBoundKey();
    }
}
