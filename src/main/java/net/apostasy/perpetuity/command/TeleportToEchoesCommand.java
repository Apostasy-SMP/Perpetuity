package net.apostasy.perpetuity.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.apostasy.perpetuity.network.s2c.RiftBangPayload;
import net.apostasy.perpetuity.util.EchoUtil;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * @author Chemthunder
 */
public class TeleportToEchoesCommand implements CommandRegistrationCallback {
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("tte").then(argument("target", EntityArgumentType.player()).then(argument("timeTillRelease", IntegerArgumentType.integer())
                .executes(context -> {
                    PlayerEntity player = EntityArgumentType.getPlayer(context, "target");

                    if (player != null) {
                        EchoUtil.teleportIntoValidLabyrinthPos(player, IntegerArgumentType.getInteger(context, "timeTillRelease"));
                    }
                    return 1;
                })))
        );

        dispatcher.register(literal("riftbang").executes(context -> {
            ServerWorld echoes = EchoUtil.fetchEchoes(context.getSource().getServer());

            if (echoes != null) {
                for (ServerPlayerEntity serverPlayer : echoes.getPlayers()) {
                    ServerPlayNetworking.send(serverPlayer, new RiftBangPayload());
                }
            }
            return 1;
        }));
    }
}
