/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStringReader;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;

public class ItemStackParticleEffect
implements ParticleEffect {
    public static final ParticleEffect.Factory<ItemStackParticleEffect> PARAMETERS_FACTORY = new ParticleEffect.Factory<ItemStackParticleEffect>(){

        @Override
        public ItemStackParticleEffect read(ParticleType<ItemStackParticleEffect> arg, StringReader stringReader) throws CommandSyntaxException {
            stringReader.expect(' ');
            ItemStringReader.ItemResult lv = ItemStringReader.item(Registries.ITEM.getReadOnlyWrapper(), stringReader);
            ItemStack lv2 = new ItemStackArgument(lv.item(), lv.nbt()).createStack(1, false);
            return new ItemStackParticleEffect(arg, lv2);
        }

        @Override
        public ItemStackParticleEffect read(ParticleType<ItemStackParticleEffect> arg, PacketByteBuf arg2) {
            return new ItemStackParticleEffect(arg, arg2.readItemStack());
        }

        @Override
        public /* synthetic */ ParticleEffect read(ParticleType type, PacketByteBuf buf) {
            return this.read(type, buf);
        }

        @Override
        public /* synthetic */ ParticleEffect read(ParticleType type, StringReader reader) throws CommandSyntaxException {
            return this.read(type, reader);
        }
    };
    private final ParticleType<ItemStackParticleEffect> type;
    private final ItemStack stack;

    public static Codec<ItemStackParticleEffect> createCodec(ParticleType<ItemStackParticleEffect> type) {
        return ItemStack.CODEC.xmap(stack -> new ItemStackParticleEffect(type, (ItemStack)stack), effect -> effect.stack);
    }

    public ItemStackParticleEffect(ParticleType<ItemStackParticleEffect> type, ItemStack stack) {
        this.type = type;
        this.stack = stack;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeItemStack(this.stack);
    }

    @Override
    public String asString() {
        return Registries.PARTICLE_TYPE.getId(this.getType()) + " " + new ItemStackArgument(this.stack.getRegistryEntry(), this.stack.getNbt()).asString();
    }

    public ParticleType<ItemStackParticleEffect> getType() {
        return this.type;
    }

    public ItemStack getItemStack() {
        return this.stack;
    }
}

