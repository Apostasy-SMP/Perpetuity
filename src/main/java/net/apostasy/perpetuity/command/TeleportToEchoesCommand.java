package net.apostasy.perpetuity.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.apostasy.perpetuity.util.EchoUtil;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * @author Chemthunder
 */
public class TeleportToEchoesCommand implements CommandRegistrationCallback {
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("tte").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                .then(argument("targets", EntityArgumentType.players())
                        .then(argument("time", IntegerArgumentType.integer())
                                .executes(context -> {
                                    for (ServerPlayerEntity serverPlayer : EntityArgumentType.getPlayers(context, "targets")) {
                                        run(
                                                serverPlayer,
                                                IntegerArgumentType.getInteger(context, "time")
                                        );
                                    }
                                    return 1;
                                })
                        )
                )
                .then(argument("target", EntityArgumentType.player())
                        .then(argument("time", IntegerArgumentType.integer())
                                .executes(context -> {
                                    run(
                                            EntityArgumentType.getPlayer(context, "target"),
                                            IntegerArgumentType.getInteger(context, "time")
                                    );
                                    return 1;
                                })
                        )
                )
        );
    }

    private static void run(LivingEntity living, int time) {
        EchoUtil.teleportIntoValidLabyrinthPos(living, time);
    }
}
