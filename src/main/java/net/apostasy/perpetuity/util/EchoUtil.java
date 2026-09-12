package net.apostasy.perpetuity.util;

import net.apostasy.perpetuity.Perpetuity;
import net.apostasy.perpetuity.network.s2c.BeginTimerPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Chemthunder
 */
// Labyrinth Utilities
public class EchoUtil {
    public static final int MAX_Y_HEIGHT = 17;
    public static final int MIN_Y_HEIGHT = -2;

    public static final RegistryKey<World> ECHOES_KEY = RegistryKey.of(RegistryKeys.WORLD, Perpetuity.id("the_echoes"));

    public static boolean isInLabyrinth(Entity entity) {
        return entity != null && entity.getEntityWorld().getRegistryKey() == ECHOES_KEY;
    }

    public static boolean effectsApplicable(Entity entity) {
        return entity instanceof PlayerEntity player && isInLabyrinth(player) && !player.isCreative();
    }

    @Nullable
    public static ServerWorld fetchEchoes(MinecraftServer server) {
        return server.getWorld(ECHOES_KEY);
    }

    @Nullable
    public static ServerWorld fetchEchoes(World world) {
        return world.getServer() != null ? world.getServer().getWorld(ECHOES_KEY) : null;
    }

    public static void teleportIntoValidLabyrinthPos(LivingEntity living, int timeTillRelease) {
        ServerWorld echoes = fetchEchoes(living.getEntityWorld());

        if (echoes != null) {
            BlockPos target = findValidBlockPos(echoes);

            if (target != null) {
                if (living instanceof ServerPlayerEntity serverPlayer) {
                    serverPlayer.teleportTo(
                            new TeleportTarget(
                                    echoes,
                                    new Vec3d(target.toCenterPos().x, target.toCenterPos().y + 2, target.toCenterPos().z),
                                    new Vec3d(0, 0, 0),
                                    serverPlayer.getYaw(),
                                    serverPlayer.getPitch(),
                                    TeleportTarget.NO_OP
                            )
                    );

                    ServerPlayNetworking.send(serverPlayer, new BeginTimerPayload(timeTillRelease));
                }
            }
        }
    }

    @Nullable
    public static BlockPos findValidBlockPos(ServerWorld echoes) {
        int spawnRadius = 50;

        List<BlockPos> possiblePoints = new ArrayList<>();

        for (int x = -spawnRadius; x < spawnRadius; x++) {
            for (int z = -spawnRadius; z < spawnRadius; z++) {
                BlockPos pos = new BlockPos(x, 4, z);
                BlockState atPos = echoes.getBlockState(pos);

                if (atPos.isOf(Blocks.PACKED_MUD)) {
                    if (echoes.getBlockState(pos.up()).isAir() && echoes.getBlockState(pos.up(2)).isAir()) {
                        possiblePoints.add(pos);
                    }
                }
            }
        }

        for (int x = -spawnRadius; x < spawnRadius; x++) {
            for (int z = -spawnRadius; z < spawnRadius; z++) {
                BlockPos pos = new BlockPos(x, 5, z);
                BlockState atPos = echoes.getBlockState(pos);

                if (atPos.isOf(Blocks.PACKED_MUD)) {
                    if (echoes.getBlockState(pos.up()).isAir() && echoes.getBlockState(pos.up(2)).isAir()) {
                        possiblePoints.add(pos);
                    }
                }
            }
        }

        if (!possiblePoints.isEmpty()) {
            return possiblePoints.get(new Random().nextInt(possiblePoints.size()));
        }
        return null;
    }
}
