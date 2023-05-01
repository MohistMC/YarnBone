/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AttachedStemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.block.GourdBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class PumpkinBlock
extends GourdBlock {
    protected PumpkinBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player2, Hand hand, BlockHitResult hit) {
        ItemStack lv = player2.getStackInHand(hand);
        if (lv.isOf(Items.SHEARS)) {
            if (!world.isClient) {
                Direction lv2 = hit.getSide();
                Direction lv3 = lv2.getAxis() == Direction.Axis.Y ? player2.getHorizontalFacing().getOpposite() : lv2;
                world.playSound(null, pos, SoundEvents.BLOCK_PUMPKIN_CARVE, SoundCategory.BLOCKS, 1.0f, 1.0f);
                world.setBlockState(pos, (BlockState)Blocks.CARVED_PUMPKIN.getDefaultState().with(CarvedPumpkinBlock.FACING, lv3), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                ItemEntity lv4 = new ItemEntity(world, (double)pos.getX() + 0.5 + (double)lv3.getOffsetX() * 0.65, (double)pos.getY() + 0.1, (double)pos.getZ() + 0.5 + (double)lv3.getOffsetZ() * 0.65, new ItemStack(Items.PUMPKIN_SEEDS, 4));
                lv4.setVelocity(0.05 * (double)lv3.getOffsetX() + world.random.nextDouble() * 0.02, 0.05, 0.05 * (double)lv3.getOffsetZ() + world.random.nextDouble() * 0.02);
                world.spawnEntity(lv4);
                lv.damage(1, player2, player -> player.sendToolBreakStatus(hand));
                world.emitGameEvent((Entity)player2, GameEvent.SHEAR, pos);
                player2.incrementStat(Stats.USED.getOrCreateStat(Items.SHEARS));
            }
            return ActionResult.success(world.isClient);
        }
        return super.onUse(state, world, pos, player2, hand, hit);
    }

    @Override
    public StemBlock getStem() {
        return (StemBlock)Blocks.PUMPKIN_STEM;
    }

    @Override
    public AttachedStemBlock getAttachedStem() {
        return (AttachedStemBlock)Blocks.ATTACHED_PUMPKIN_STEM;
    }
}

