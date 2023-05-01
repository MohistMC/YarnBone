/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ChorusFruitItem
extends Item {
    public ChorusFruitItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack lv = super.finishUsing(stack, world, user);
        if (!world.isClient) {
            double d = user.getX();
            double e = user.getY();
            double f = user.getZ();
            for (int i = 0; i < 16; ++i) {
                double g = user.getX() + (user.getRandom().nextDouble() - 0.5) * 16.0;
                double h = MathHelper.clamp(user.getY() + (double)(user.getRandom().nextInt(16) - 8), (double)world.getBottomY(), (double)(world.getBottomY() + ((ServerWorld)world).getLogicalHeight() - 1));
                double j = user.getZ() + (user.getRandom().nextDouble() - 0.5) * 16.0;
                if (user.hasVehicle()) {
                    user.stopRiding();
                }
                Vec3d lv2 = user.getPos();
                if (!user.teleport(g, h, j, true)) continue;
                world.emitGameEvent(GameEvent.TELEPORT, lv2, GameEvent.Emitter.of(user));
                SoundEvent lv3 = user instanceof FoxEntity ? SoundEvents.ENTITY_FOX_TELEPORT : SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT;
                world.playSound(null, d, e, f, lv3, SoundCategory.PLAYERS, 1.0f, 1.0f);
                user.playSound(lv3, 1.0f, 1.0f);
                break;
            }
            if (user instanceof PlayerEntity) {
                ((PlayerEntity)user).getItemCooldownManager().set(this, 20);
            }
        }
        return lv;
    }
}

