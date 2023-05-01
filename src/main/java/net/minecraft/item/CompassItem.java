/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import com.mojang.logging.LogUtils;
import java.util.Optional;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.Vanishable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class CompassItem
extends Item
implements Vanishable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String LODESTONE_POS_KEY = "LodestonePos";
    public static final String LODESTONE_DIMENSION_KEY = "LodestoneDimension";
    public static final String LODESTONE_TRACKED_KEY = "LodestoneTracked";

    public CompassItem(Item.Settings arg) {
        super(arg);
    }

    public static boolean hasLodestone(ItemStack stack) {
        NbtCompound lv = stack.getNbt();
        return lv != null && (lv.contains(LODESTONE_DIMENSION_KEY) || lv.contains(LODESTONE_POS_KEY));
    }

    private static Optional<RegistryKey<World>> getLodestoneDimension(NbtCompound nbt) {
        return World.CODEC.parse(NbtOps.INSTANCE, nbt.get(LODESTONE_DIMENSION_KEY)).result();
    }

    @Nullable
    public static GlobalPos createLodestonePos(NbtCompound nbt) {
        Optional<RegistryKey<World>> optional;
        boolean bl = nbt.contains(LODESTONE_POS_KEY);
        boolean bl2 = nbt.contains(LODESTONE_DIMENSION_KEY);
        if (bl && bl2 && (optional = CompassItem.getLodestoneDimension(nbt)).isPresent()) {
            BlockPos lv = NbtHelper.toBlockPos(nbt.getCompound(LODESTONE_POS_KEY));
            return GlobalPos.create(optional.get(), lv);
        }
        return null;
    }

    @Nullable
    public static GlobalPos createSpawnPos(World world) {
        return world.getDimension().natural() ? GlobalPos.create(world.getRegistryKey(), world.getSpawnPos()) : null;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return CompassItem.hasLodestone(stack) || super.hasGlint(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient) {
            return;
        }
        if (CompassItem.hasLodestone(stack)) {
            BlockPos lv2;
            NbtCompound lv = stack.getOrCreateNbt();
            if (lv.contains(LODESTONE_TRACKED_KEY) && !lv.getBoolean(LODESTONE_TRACKED_KEY)) {
                return;
            }
            Optional<RegistryKey<World>> optional = CompassItem.getLodestoneDimension(lv);
            if (optional.isPresent() && optional.get() == world.getRegistryKey() && lv.contains(LODESTONE_POS_KEY) && (!world.isInBuildLimit(lv2 = NbtHelper.toBlockPos(lv.getCompound(LODESTONE_POS_KEY))) || !((ServerWorld)world).getPointOfInterestStorage().hasTypeAt(PointOfInterestTypes.LODESTONE, lv2))) {
                lv.remove(LODESTONE_POS_KEY);
            }
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos lv = context.getBlockPos();
        World lv2 = context.getWorld();
        if (lv2.getBlockState(lv).isOf(Blocks.LODESTONE)) {
            boolean bl;
            lv2.playSound(null, lv, SoundEvents.ITEM_LODESTONE_COMPASS_LOCK, SoundCategory.PLAYERS, 1.0f, 1.0f);
            PlayerEntity lv3 = context.getPlayer();
            ItemStack lv4 = context.getStack();
            boolean bl2 = bl = !lv3.getAbilities().creativeMode && lv4.getCount() == 1;
            if (bl) {
                this.writeNbt(lv2.getRegistryKey(), lv, lv4.getOrCreateNbt());
            } else {
                ItemStack lv5 = new ItemStack(Items.COMPASS, 1);
                NbtCompound lv6 = lv4.hasNbt() ? lv4.getNbt().copy() : new NbtCompound();
                lv5.setNbt(lv6);
                if (!lv3.getAbilities().creativeMode) {
                    lv4.decrement(1);
                }
                this.writeNbt(lv2.getRegistryKey(), lv, lv6);
                if (!lv3.getInventory().insertStack(lv5)) {
                    lv3.dropItem(lv5, false);
                }
            }
            return ActionResult.success(lv2.isClient);
        }
        return super.useOnBlock(context);
    }

    private void writeNbt(RegistryKey<World> worldKey, BlockPos pos, NbtCompound nbt) {
        nbt.put(LODESTONE_POS_KEY, NbtHelper.fromBlockPos(pos));
        World.CODEC.encodeStart(NbtOps.INSTANCE, worldKey).resultOrPartial(LOGGER::error).ifPresent(arg2 -> nbt.put(LODESTONE_DIMENSION_KEY, (NbtElement)arg2));
        nbt.putBoolean(LODESTONE_TRACKED_KEY, true);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return CompassItem.hasLodestone(stack) ? "item.minecraft.lodestone_compass" : super.getTranslationKey(stack);
    }
}

