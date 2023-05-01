/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class EntityBucketItem
extends BucketItem {
    private final EntityType<?> entityType;
    private final SoundEvent emptyingSound;

    public EntityBucketItem(EntityType<?> type, Fluid fluid, SoundEvent emptyingSound, Item.Settings settings) {
        super(fluid, settings);
        this.entityType = type;
        this.emptyingSound = emptyingSound;
    }

    @Override
    public void onEmptied(@Nullable PlayerEntity player, World world, ItemStack stack, BlockPos pos) {
        if (world instanceof ServerWorld) {
            this.spawnEntity((ServerWorld)world, stack, pos);
            world.emitGameEvent((Entity)player, GameEvent.ENTITY_PLACE, pos);
        }
    }

    @Override
    protected void playEmptyingSound(@Nullable PlayerEntity player, WorldAccess world, BlockPos pos) {
        world.playSound(player, pos, this.emptyingSound, SoundCategory.NEUTRAL, 1.0f, 1.0f);
    }

    private void spawnEntity(ServerWorld world, ItemStack stack, BlockPos pos) {
        Object lv = this.entityType.spawnFromItemStack(world, stack, null, pos, SpawnReason.BUCKET, true, false);
        if (lv instanceof Bucketable) {
            Bucketable lv2 = (Bucketable)lv;
            lv2.copyDataFromNbt(stack.getOrCreateNbt());
            lv2.setFromBucket(true);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound lv;
        if (this.entityType == EntityType.TROPICAL_FISH && (lv = stack.getNbt()) != null && lv.contains("BucketVariantTag", NbtElement.INT_TYPE)) {
            int i = lv.getInt("BucketVariantTag");
            Formatting[] lvs = new Formatting[]{Formatting.ITALIC, Formatting.GRAY};
            String string = "color.minecraft." + TropicalFishEntity.getBaseDyeColor(i);
            String string2 = "color.minecraft." + TropicalFishEntity.getPatternDyeColor(i);
            for (int j = 0; j < TropicalFishEntity.COMMON_VARIANTS.size(); ++j) {
                if (i != TropicalFishEntity.COMMON_VARIANTS.get(j).getId()) continue;
                tooltip.add(Text.translatable(TropicalFishEntity.getToolTipForVariant(j)).formatted(lvs));
                return;
            }
            tooltip.add(TropicalFishEntity.getVariety(i).getText().copyContentOnly().formatted(lvs));
            MutableText lv2 = Text.translatable(string);
            if (!string.equals(string2)) {
                lv2.append(", ").append(Text.translatable(string2));
            }
            lv2.formatted(lvs);
            tooltip.add(lv2);
        }
    }
}

