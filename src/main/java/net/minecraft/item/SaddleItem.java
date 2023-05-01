/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.event.GameEvent;

public class SaddleItem
extends Item {
    public SaddleItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        Saddleable lv;
        if (entity instanceof Saddleable && entity.isAlive() && !(lv = (Saddleable)((Object)entity)).isSaddled() && lv.canBeSaddled()) {
            if (!user.world.isClient) {
                lv.saddle(SoundCategory.NEUTRAL);
                entity.world.emitGameEvent((Entity)entity, GameEvent.EQUIP, entity.getPos());
                stack.decrement(1);
            }
            return ActionResult.success(user.world.isClient);
        }
        return ActionResult.PASS;
    }
}

