/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public class PlayerSkullBlock
extends SkullBlock {
    protected PlayerSkullBlock(AbstractBlock.Settings arg) {
        super(SkullBlock.Type.PLAYER, arg);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv instanceof SkullBlockEntity) {
            SkullBlockEntity lv2 = (SkullBlockEntity)lv;
            GameProfile gameProfile = null;
            if (itemStack.hasNbt()) {
                NbtCompound lv3 = itemStack.getNbt();
                if (lv3.contains("SkullOwner", NbtElement.COMPOUND_TYPE)) {
                    gameProfile = NbtHelper.toGameProfile(lv3.getCompound("SkullOwner"));
                } else if (lv3.contains("SkullOwner", NbtElement.STRING_TYPE) && !StringUtils.isBlank(lv3.getString("SkullOwner"))) {
                    gameProfile = new GameProfile(null, lv3.getString("SkullOwner"));
                }
            }
            lv2.setOwner(gameProfile);
        }
    }
}

