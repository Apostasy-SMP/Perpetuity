package net.apostasy.perpetuity.mixin.echoes.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.apostasy.perpetuity.util.LUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * @author Chemthunder
 */
@Mixin(value = DebugHud.class)
public abstract class DebugHudMixin {
    @Shadow @Final private MinecraftClient client;

    @Shadow
    public abstract boolean shouldShowDebugHud();

    @WrapMethod(method = "render")
    private void perpetuity$removeF3(DrawContext context, Operation<Void> original) {
        PlayerEntity player = this.client.player;

        if (player != null) {
            if (LUtil.effectsApplicable(player)) {
                if (this.shouldShowDebugHud()) {
                    context.drawText(
                            this.client.textRenderer,
                            Text.literal("Your efforts are futile.").formatted(Formatting.YELLOW),
                            10,
                            10,
                            0xFFffffff,
                            false
                    );
                    return;
                }
            }
        }

        original.call(context);
    }
}
