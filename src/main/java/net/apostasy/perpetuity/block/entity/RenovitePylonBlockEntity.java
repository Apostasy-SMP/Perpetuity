package net.apostasy.perpetuity.block.entity;

import net.apostasy.perpetuity.block.RenovitePylonBlock;
import net.apostasy.perpetuity.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
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
        if (world.getTime() % 200 != 0 || entity.lastTick == Math.toIntExact(world.getTime())) return;
        entity.lastTick = Math.toIntExact(world.getTime());

        world.getEntitiesByClass(PlayerEntity.class, new Box(pos).expand(10), LivingEntity::isAlive).forEach(player -> {
            player.getInventory().getMainStacks().stream()
                    .filter(ItemStack::isDamageable)
                    .forEach(stack -> stack.setDamage(stack.getDamage()-1));
        });
    }

    @Override
    public void registerControllers(AnimatableManager.@NonNull ControllerRegistrar controllers) {

    }

    @Override
    public @NonNull AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
