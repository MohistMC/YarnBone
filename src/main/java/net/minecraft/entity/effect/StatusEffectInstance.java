/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.effect;

import com.google.common.collect.ComparisonChain;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class StatusEffectInstance
implements Comparable<StatusEffectInstance> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int INFINITE = -1;
    private final StatusEffect type;
    private int duration;
    private int amplifier;
    private boolean ambient;
    private boolean showParticles;
    private boolean showIcon;
    @Nullable
    private StatusEffectInstance hiddenEffect;
    private final Optional<FactorCalculationData> factorCalculationData;

    public StatusEffectInstance(StatusEffect type) {
        this(type, 0, 0);
    }

    public StatusEffectInstance(StatusEffect type, int duration) {
        this(type, duration, 0);
    }

    public StatusEffectInstance(StatusEffect type, int duration, int amplifier) {
        this(type, duration, amplifier, false, true);
    }

    public StatusEffectInstance(StatusEffect type, int duration, int amplifier, boolean ambient, boolean visible) {
        this(type, duration, amplifier, ambient, visible, visible);
    }

    public StatusEffectInstance(StatusEffect type, int duration, int amplifier, boolean ambient, boolean showParticles, boolean showIcon) {
        this(type, duration, amplifier, ambient, showParticles, showIcon, null, type.getFactorCalculationDataSupplier());
    }

    public StatusEffectInstance(StatusEffect type, int duration, int amplifier, boolean ambient, boolean showParticles, boolean showIcon, @Nullable StatusEffectInstance hiddenEffect, Optional<FactorCalculationData> factorCalculationData) {
        this.type = type;
        this.duration = duration;
        this.amplifier = amplifier;
        this.ambient = ambient;
        this.showParticles = showParticles;
        this.showIcon = showIcon;
        this.hiddenEffect = hiddenEffect;
        this.factorCalculationData = factorCalculationData;
    }

    public StatusEffectInstance(StatusEffectInstance instance) {
        this.type = instance.type;
        this.factorCalculationData = this.type.getFactorCalculationDataSupplier();
        this.copyFrom(instance);
    }

    public Optional<FactorCalculationData> getFactorCalculationData() {
        return this.factorCalculationData;
    }

    void copyFrom(StatusEffectInstance that) {
        this.duration = that.duration;
        this.amplifier = that.amplifier;
        this.ambient = that.ambient;
        this.showParticles = that.showParticles;
        this.showIcon = that.showIcon;
    }

    public boolean upgrade(StatusEffectInstance that) {
        if (this.type != that.type) {
            LOGGER.warn("This method should only be called for matching effects!");
        }
        int i = this.duration;
        boolean bl = false;
        if (that.amplifier > this.amplifier) {
            if (that.lastsShorterThan(this)) {
                StatusEffectInstance lv = this.hiddenEffect;
                this.hiddenEffect = new StatusEffectInstance(this);
                this.hiddenEffect.hiddenEffect = lv;
            }
            this.amplifier = that.amplifier;
            this.duration = that.duration;
            bl = true;
        } else if (this.lastsShorterThan(that)) {
            if (that.amplifier == this.amplifier) {
                this.duration = that.duration;
                bl = true;
            } else if (this.hiddenEffect == null) {
                this.hiddenEffect = new StatusEffectInstance(that);
            } else {
                this.hiddenEffect.upgrade(that);
            }
        }
        if (!that.ambient && this.ambient || bl) {
            this.ambient = that.ambient;
            bl = true;
        }
        if (that.showParticles != this.showParticles) {
            this.showParticles = that.showParticles;
            bl = true;
        }
        if (that.showIcon != this.showIcon) {
            this.showIcon = that.showIcon;
            bl = true;
        }
        return bl;
    }

    private boolean lastsShorterThan(StatusEffectInstance effect) {
        return !this.isInfinite() && (this.duration < effect.duration || effect.isInfinite());
    }

    public boolean isInfinite() {
        return this.duration == -1;
    }

    public boolean isDurationBelow(int duration) {
        return !this.isInfinite() && this.duration <= duration;
    }

    public int mapDuration(Int2IntFunction mapper) {
        if (this.isInfinite() || this.duration == 0) {
            return this.duration;
        }
        return mapper.applyAsInt(this.duration);
    }

    public StatusEffect getEffectType() {
        return this.type;
    }

    public int getDuration() {
        return this.duration;
    }

    public int getAmplifier() {
        return this.amplifier;
    }

    public boolean isAmbient() {
        return this.ambient;
    }

    public boolean shouldShowParticles() {
        return this.showParticles;
    }

    public boolean shouldShowIcon() {
        return this.showIcon;
    }

    public boolean update(LivingEntity entity, Runnable overwriteCallback) {
        if (this.isActive()) {
            int i;
            int n = i = this.isInfinite() ? entity.age : this.duration;
            if (this.type.canApplyUpdateEffect(i, this.amplifier)) {
                this.applyUpdateEffect(entity);
            }
            this.updateDuration();
            if (this.duration == 0 && this.hiddenEffect != null) {
                this.copyFrom(this.hiddenEffect);
                this.hiddenEffect = this.hiddenEffect.hiddenEffect;
                overwriteCallback.run();
            }
        }
        this.factorCalculationData.ifPresent(factorCalculationData -> factorCalculationData.update(this));
        return this.isActive();
    }

    private boolean isActive() {
        return this.isInfinite() || this.duration > 0;
    }

    private int updateDuration() {
        if (this.hiddenEffect != null) {
            this.hiddenEffect.updateDuration();
        }
        this.duration = this.mapDuration(duration -> duration - 1);
        return this.duration;
    }

    public void applyUpdateEffect(LivingEntity entity) {
        if (this.isActive()) {
            this.type.applyUpdateEffect(entity, this.amplifier);
        }
    }

    public String getTranslationKey() {
        return this.type.getTranslationKey();
    }

    public String toString() {
        String string = this.amplifier > 0 ? this.getTranslationKey() + " x " + (this.amplifier + 1) + ", Duration: " + this.getDurationString() : this.getTranslationKey() + ", Duration: " + this.getDurationString();
        if (!this.showParticles) {
            string = string + ", Particles: false";
        }
        if (!this.showIcon) {
            string = string + ", Show Icon: false";
        }
        return string;
    }

    private String getDurationString() {
        if (this.isInfinite()) {
            return "infinite";
        }
        return Integer.toString(this.duration);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof StatusEffectInstance) {
            StatusEffectInstance lv = (StatusEffectInstance)o;
            return this.duration == lv.duration && this.amplifier == lv.amplifier && this.ambient == lv.ambient && this.type.equals(lv.type);
        }
        return false;
    }

    public int hashCode() {
        int i = this.type.hashCode();
        i = 31 * i + this.duration;
        i = 31 * i + this.amplifier;
        i = 31 * i + (this.ambient ? 1 : 0);
        return i;
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("Id", StatusEffect.getRawId(this.getEffectType()));
        this.writeTypelessNbt(nbt);
        return nbt;
    }

    private void writeTypelessNbt(NbtCompound nbt) {
        nbt.putByte("Amplifier", (byte)this.getAmplifier());
        nbt.putInt("Duration", this.getDuration());
        nbt.putBoolean("Ambient", this.isAmbient());
        nbt.putBoolean("ShowParticles", this.shouldShowParticles());
        nbt.putBoolean("ShowIcon", this.shouldShowIcon());
        if (this.hiddenEffect != null) {
            NbtCompound lv = new NbtCompound();
            this.hiddenEffect.writeNbt(lv);
            nbt.put("HiddenEffect", lv);
        }
        this.factorCalculationData.ifPresent(factorCalculationData -> FactorCalculationData.CODEC.encodeStart(NbtOps.INSTANCE, (FactorCalculationData)factorCalculationData).resultOrPartial(LOGGER::error).ifPresent(factorCalculationDataNbt -> nbt.put("FactorCalculationData", (NbtElement)factorCalculationDataNbt)));
    }

    @Nullable
    public static StatusEffectInstance fromNbt(NbtCompound nbt) {
        int i = nbt.getInt("Id");
        StatusEffect lv = StatusEffect.byRawId(i);
        if (lv == null) {
            return null;
        }
        return StatusEffectInstance.fromNbt(lv, nbt);
    }

    private static StatusEffectInstance fromNbt(StatusEffect type, NbtCompound nbt) {
        byte i = nbt.getByte("Amplifier");
        int j = nbt.getInt("Duration");
        boolean bl = nbt.getBoolean("Ambient");
        boolean bl2 = true;
        if (nbt.contains("ShowParticles", NbtElement.BYTE_TYPE)) {
            bl2 = nbt.getBoolean("ShowParticles");
        }
        boolean bl3 = bl2;
        if (nbt.contains("ShowIcon", NbtElement.BYTE_TYPE)) {
            bl3 = nbt.getBoolean("ShowIcon");
        }
        StatusEffectInstance lv = null;
        if (nbt.contains("HiddenEffect", NbtElement.COMPOUND_TYPE)) {
            lv = StatusEffectInstance.fromNbt(type, nbt.getCompound("HiddenEffect"));
        }
        Optional<FactorCalculationData> optional = nbt.contains("FactorCalculationData", NbtElement.COMPOUND_TYPE) ? FactorCalculationData.CODEC.parse(new Dynamic<NbtCompound>(NbtOps.INSTANCE, nbt.getCompound("FactorCalculationData"))).resultOrPartial(LOGGER::error) : Optional.empty();
        return new StatusEffectInstance(type, j, Math.max(i, 0), bl, bl2, bl3, lv, optional);
    }

    @Override
    public int compareTo(StatusEffectInstance arg) {
        int i = 32147;
        if (this.getDuration() > 32147 && arg.getDuration() > 32147 || this.isAmbient() && arg.isAmbient()) {
            return ComparisonChain.start().compare(this.isAmbient(), arg.isAmbient()).compare(this.getEffectType().getColor(), arg.getEffectType().getColor()).result();
        }
        return ComparisonChain.start().compareFalseFirst(this.isAmbient(), arg.isAmbient()).compareFalseFirst(this.isInfinite(), arg.isInfinite()).compare(this.getDuration(), arg.getDuration()).compare(this.getEffectType().getColor(), arg.getEffectType().getColor()).result();
    }

    @Override
    public /* synthetic */ int compareTo(Object that) {
        return this.compareTo((StatusEffectInstance)that);
    }

    public static class FactorCalculationData {
        public static final Codec<FactorCalculationData> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codecs.NONNEGATIVE_INT.fieldOf("padding_duration")).forGetter(data -> data.paddingDuration), ((MapCodec)Codec.FLOAT.fieldOf("factor_start")).orElse(Float.valueOf(0.0f)).forGetter(data -> Float.valueOf(data.factorStart)), ((MapCodec)Codec.FLOAT.fieldOf("factor_target")).orElse(Float.valueOf(1.0f)).forGetter(data -> Float.valueOf(data.factorTarget)), ((MapCodec)Codec.FLOAT.fieldOf("factor_current")).orElse(Float.valueOf(0.0f)).forGetter(data -> Float.valueOf(data.factorCurrent)), ((MapCodec)Codecs.NONNEGATIVE_INT.fieldOf("ticks_active")).orElse(0).forGetter(data -> data.effectChangedTimestamp), ((MapCodec)Codec.FLOAT.fieldOf("factor_previous_frame")).orElse(Float.valueOf(0.0f)).forGetter(data -> Float.valueOf(data.factorPreviousFrame)), ((MapCodec)Codec.BOOL.fieldOf("had_effect_last_tick")).orElse(false).forGetter(data -> data.hadEffectLastTick)).apply((Applicative<FactorCalculationData, ?>)instance, FactorCalculationData::new));
        private final int paddingDuration;
        private float factorStart;
        private float factorTarget;
        private float factorCurrent;
        private int effectChangedTimestamp;
        private float factorPreviousFrame;
        private boolean hadEffectLastTick;

        public FactorCalculationData(int paddingDuration, float factorStart, float factorTarget, float factorCurrent, int effectChangedTimestamp, float factorPreviousFrame, boolean hadEffectLastTick) {
            this.paddingDuration = paddingDuration;
            this.factorStart = factorStart;
            this.factorTarget = factorTarget;
            this.factorCurrent = factorCurrent;
            this.effectChangedTimestamp = effectChangedTimestamp;
            this.factorPreviousFrame = factorPreviousFrame;
            this.hadEffectLastTick = hadEffectLastTick;
        }

        public FactorCalculationData(int paddingDuration) {
            this(paddingDuration, 0.0f, 1.0f, 0.0f, 0, 0.0f, false);
        }

        public void update(StatusEffectInstance effect) {
            this.factorPreviousFrame = this.factorCurrent;
            boolean bl = !effect.isDurationBelow(this.paddingDuration);
            ++this.effectChangedTimestamp;
            if (this.hadEffectLastTick != bl) {
                this.hadEffectLastTick = bl;
                this.effectChangedTimestamp = 0;
                this.factorStart = this.factorCurrent;
                this.factorTarget = bl ? 1.0f : 0.0f;
            }
            float f = MathHelper.clamp((float)this.effectChangedTimestamp / (float)this.paddingDuration, 0.0f, 1.0f);
            this.factorCurrent = MathHelper.lerp(f, this.factorStart, this.factorTarget);
        }

        public float lerp(LivingEntity entity, float tickDelta) {
            if (entity.isRemoved()) {
                this.factorPreviousFrame = this.factorCurrent;
            }
            return MathHelper.lerp(tickDelta, this.factorPreviousFrame, this.factorCurrent);
        }
    }
}

