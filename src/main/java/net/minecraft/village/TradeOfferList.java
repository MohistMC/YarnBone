/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.village;

import java.util.ArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.village.TradeOffer;
import org.jetbrains.annotations.Nullable;

public class TradeOfferList
extends ArrayList<TradeOffer> {
    public TradeOfferList() {
    }

    private TradeOfferList(int size) {
        super(size);
    }

    public TradeOfferList(NbtCompound nbt) {
        NbtList lv = nbt.getList("Recipes", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < lv.size(); ++i) {
            this.add(new TradeOffer(lv.getCompound(i)));
        }
    }

    @Nullable
    public TradeOffer getValidOffer(ItemStack firstBuyItem, ItemStack secondBuyItem, int index) {
        if (index > 0 && index < this.size()) {
            TradeOffer lv = (TradeOffer)this.get(index);
            if (lv.matchesBuyItems(firstBuyItem, secondBuyItem)) {
                return lv;
            }
            return null;
        }
        for (int j = 0; j < this.size(); ++j) {
            TradeOffer lv2 = (TradeOffer)this.get(j);
            if (!lv2.matchesBuyItems(firstBuyItem, secondBuyItem)) continue;
            return lv2;
        }
        return null;
    }

    public void toPacket(PacketByteBuf buf) {
        buf.writeCollection(this, (buf2, offer) -> {
            buf2.writeItemStack(offer.getOriginalFirstBuyItem());
            buf2.writeItemStack(offer.getSellItem());
            buf2.writeItemStack(offer.getSecondBuyItem());
            buf2.writeBoolean(offer.isDisabled());
            buf2.writeInt(offer.getUses());
            buf2.writeInt(offer.getMaxUses());
            buf2.writeInt(offer.getMerchantExperience());
            buf2.writeInt(offer.getSpecialPrice());
            buf2.writeFloat(offer.getPriceMultiplier());
            buf2.writeInt(offer.getDemandBonus());
        });
    }

    public static TradeOfferList fromPacket(PacketByteBuf buf) {
        return buf.readCollection(TradeOfferList::new, buf2 -> {
            ItemStack lv = buf2.readItemStack();
            ItemStack lv2 = buf2.readItemStack();
            ItemStack lv3 = buf2.readItemStack();
            boolean bl = buf2.readBoolean();
            int i = buf2.readInt();
            int j = buf2.readInt();
            int k = buf2.readInt();
            int l = buf2.readInt();
            float f = buf2.readFloat();
            int m = buf2.readInt();
            TradeOffer lv4 = new TradeOffer(lv, lv3, lv2, i, j, k, f, m);
            if (bl) {
                lv4.disable();
            }
            lv4.setSpecialPrice(l);
            return lv4;
        });
    }

    public NbtCompound toNbt() {
        NbtCompound lv = new NbtCompound();
        NbtList lv2 = new NbtList();
        for (int i = 0; i < this.size(); ++i) {
            TradeOffer lv3 = (TradeOffer)this.get(i);
            lv2.add(lv3.toNbt());
        }
        lv.put("Recipes", lv2);
        return lv;
    }
}

