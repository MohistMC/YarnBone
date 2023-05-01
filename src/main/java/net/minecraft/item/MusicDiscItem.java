/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class MusicDiscItem
extends Item {
    private static final Map<SoundEvent, MusicDiscItem> MUSIC_DISCS = Maps.newHashMap();
    private final int comparatorOutput;
    private final SoundEvent sound;
    private final int lengthInTicks;

    protected MusicDiscItem(int comparatorOutput, SoundEvent sound, Item.Settings settings, int lengthInSeconds) {
        super(settings);
        this.comparatorOutput = comparatorOutput;
        this.sound = sound;
        this.lengthInTicks = lengthInSeconds * 20;
        MUSIC_DISCS.put(this.sound, this);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos lv2;
        World lv = context.getWorld();
        BlockState lv3 = lv.getBlockState(lv2 = context.getBlockPos());
        if (!lv3.isOf(Blocks.JUKEBOX) || lv3.get(JukeboxBlock.HAS_RECORD).booleanValue()) {
            return ActionResult.PASS;
        }
        ItemStack lv4 = context.getStack();
        if (!lv.isClient) {
            PlayerEntity lv5 = context.getPlayer();
            BlockEntity blockEntity = lv.getBlockEntity(lv2);
            if (blockEntity instanceof JukeboxBlockEntity) {
                JukeboxBlockEntity lv6 = (JukeboxBlockEntity)blockEntity;
                lv6.setStack(lv4.copy());
                lv.emitGameEvent(GameEvent.BLOCK_CHANGE, lv2, GameEvent.Emitter.of(lv5, lv3));
            }
            lv4.decrement(1);
            if (lv5 != null) {
                lv5.incrementStat(Stats.PLAY_RECORD);
            }
        }
        return ActionResult.success(lv.isClient);
    }

    public int getComparatorOutput() {
        return this.comparatorOutput;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(this.getDescription().formatted(Formatting.GRAY));
    }

    public MutableText getDescription() {
        return Text.translatable(this.getTranslationKey() + ".desc");
    }

    @Nullable
    public static MusicDiscItem bySound(SoundEvent sound) {
        return MUSIC_DISCS.get(sound);
    }

    public SoundEvent getSound() {
        return this.sound;
    }

    public int getSongLengthInTicks() {
        return this.lengthInTicks;
    }
}

