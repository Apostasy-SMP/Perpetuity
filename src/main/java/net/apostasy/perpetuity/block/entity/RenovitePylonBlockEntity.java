package net.apostasy.perpetuity.block.entity;

import net.apostasy.perpetuity.registry.ModBlockEntities;
import net.apostasy.perpetuity.registry.ModTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jspecify.annotations.NonNull;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class RenovitePylonBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int lastTick = 0;

    public RenovitePylonBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RENOVITE_PYLON, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, RenovitePylonBlockEntity entity) {
        if (world.isClient()) return;
        if (world.getTime() % 200 != 0 || entity.lastTick == Math.toIntExact(world.getTime())) return;
        entity.lastTick = Math.toIntExact(world.getTime());

        world.getEntitiesByClass(PlayerEntity.class, new Box(pos).expand(5), LivingEntity::isAlive).forEach(player -> {
            player.getInventory().getMainStacks().stream()
                    .filter(ItemStack::isDamaged)
                    .forEach(stack -> {
                        if (!stack.isIn(ModTags.IGNORED_BY_PYLON)) stack.setDamage(stack.getDamage() - 5);
                    });
        });
    }

    public void registerControllers(AnimatableManager.@NonNull ControllerRegistrar controllers) {}

    public @NonNull AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
