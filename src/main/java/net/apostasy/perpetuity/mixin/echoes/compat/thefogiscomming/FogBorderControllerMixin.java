//package net.apostasy.perpetuity.mixin.echoes.compat.thefogiscomming;
//
//import de.nexusrealms.thefogiscomming.client.fog.FogBorderController;
//import net.apostasy.perpetuity.util.EchoUtil;
//import net.minecraft.client.MinecraftClient;
//import net.minecraft.entity.player.PlayerEntity;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
///**
// * @author Chemthunder
// */
//@Mixin(value = FogBorderController.class)
//public abstract class FogBorderControllerMixin {
//    @Inject(
//            method = "tick",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lde/nexusrealms/thefogiscomming/client/fog/BorderFogEffect;<init>(Lnet/minecraft/util/math/Vec3d;DDDDDDLde/nexusrealms/nebulon/api/render/ColorRgba;Lde/nexusrealms/nebulon/api/render/ColorRgba;FFFIFFD)V"
//            )
//    )
//    private void perpetuity$attemptToTeleport(MinecraftClient client, CallbackInfo ci) {
//        PlayerEntity player = MinecraftClient.getInstance().player;
//        if (player == null) return;
//
//        if (player.getRandom().nextInt(3) == 0) {
//            EchoUtil.teleportIntoValidLabyrinthPos(player, 30);
//        }
//    }
//}
