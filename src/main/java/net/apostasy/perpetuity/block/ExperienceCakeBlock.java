package net.apostasy.perpetuity.block;

import net.apostasy.perpetuity.Perpetuity;
import net.apostasy.perpetuity.item.RemnantItem;
import net.apostasy.perpetuity.registry.ModItems;
import net.apostasy.perpetuity.registry.ModStats;
import net.apostasy.perpetuity.util.AdvancementUtil;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;

public class ExperienceCakeBlock extends CakeBlock {
    public ExperienceCakeBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        Item item = stack.getItem();
        if (stack.isOf(ModItems.REMNANT) && tryRepair(world, pos, state, player).isAccepted()) {
            RemnantItem.repair(stack, player, 0.14285714285F); // Repair 1/7th of the item
            world.playSound(null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 0.75F, 1.15F);
            world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            player.incrementStat(Stats.USED.getOrCreateStat(item));
            if (player instanceof ServerPlayerEntity serverPlayer) AdvancementUtil.grantAdvancement(serverPlayer, Perpetuity.id("experience_cake_repair"));
            return ActionResult.SUCCESS;
        } else {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return ActionResult.PASS;
    }

    protected static ActionResult tryRepair(WorldAccess world, BlockPos pos, BlockState state, PlayerEntity player) {
        player.incrementStat(ModStats.REMNANTS_REPAIRED_WITH_CAKE);
        int i = state.get(BITES);
        world.emitGameEvent(player, GameEvent.EAT, pos);

        if (i < 6) {
            world.setBlockState(pos, state.with(BITES, i + 1), Block.NOTIFY_ALL);
        } else {
            world.removeBlock(pos, false);
            world.emitGameEvent(player, GameEvent.BLOCK_DESTROY, pos);
        }

        return ActionResult.SUCCESS;
    }
}
