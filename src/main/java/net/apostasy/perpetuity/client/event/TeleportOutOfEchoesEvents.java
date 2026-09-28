package net.apostasy.perpetuity.client.event;

import net.apostasy.perpetuity.Perpetuity;
import net.apostasy.perpetuity.network.c2s.TeleportOutOfEchoesPayload;
import net.apostasy.perpetuity.util.EchoUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.ColorHelper;

/**
 * @author Chemthunder
 */
@Environment(EnvType.CLIENT)
public class TeleportOutOfEchoesEvents {
    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(new Tick());
        HudElementRegistry.addFirst(Perpetuity.id("render_black_box"), new Render());
    }

    public static void execute(int timeTillRelease) {
        timeTillTP = timeTillRelease * 20;
        opacity = 0.0F;
    }

    public static int timeTillTP = 0;

    public static float opacity = 0.0F;

    public static class Tick implements ClientTickEvents.EndTick {
        public void onEndTick(MinecraftClient client) {
            PlayerEntity player = client.player;
            if (player == null) return;

            opacity = Math.clamp(opacity, 0.0F, 0.9F);

            if (timeTillTP > 0) {
                if (!EchoUtil.isInLabyrinth(player)) {
                    timeTillTP = 0;
                    opacity = 0.0F;
                    return;
                }
            }

            if (timeTillTP > 0) {
                timeTillTP--;
                if (timeTillTP <= 0) {
                    TeleportOutOfEchoesPayload.send();
                }
            }

            if (EchoUtil.isInLabyrinth(player)) {
                if (timeTillTP > 0 && timeTillTP <= 80) {
                    opacity = Math.min(0.9F, opacity + 0.01F);
                } else if (opacity > 0.0F) {
                    opacity = Math.max(0.0F, opacity - 0.025F);
                }
            } else {
                if (opacity > 0.0F) {
                    opacity -= 0.025F;
                }

                timeTillTP = 0;
            }
        }
    }

    public static class Render implements HudElement {
        public void render(DrawContext context, RenderTickCounter tickCounter) {
            if (opacity > 0.01F) {
                context.fill(
                        0,
                        0,
                        context.getScaledWindowWidth(),
                        context.getScaledWindowHeight(),
                        ColorHelper.withAlpha(opacity, 0xFF000000)
                );
            }
        }
    }
}
