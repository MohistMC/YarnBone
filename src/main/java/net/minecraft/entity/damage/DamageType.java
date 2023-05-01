/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.entity.damage;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.damage.DamageEffects;
import net.minecraft.entity.damage.DamageScaling;
import net.minecraft.entity.damage.DeathMessageType;

public record DamageType(String msgId, DamageScaling scaling, float exhaustion, DamageEffects effects, DeathMessageType deathMessageType) {
    public static final Codec<DamageType> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("message_id")).forGetter(DamageType::msgId), ((MapCodec)DamageScaling.CODEC.fieldOf("scaling")).forGetter(DamageType::scaling), ((MapCodec)Codec.FLOAT.fieldOf("exhaustion")).forGetter(DamageType::exhaustion), DamageEffects.CODEC.optionalFieldOf("effects", DamageEffects.HURT).forGetter(DamageType::effects), DeathMessageType.CODEC.optionalFieldOf("death_message_type", DeathMessageType.DEFAULT).forGetter(DamageType::deathMessageType)).apply((Applicative<DamageType, ?>)instance, DamageType::new));

    public DamageType(String msgId, DamageScaling scaling, float exhaustion) {
        this(msgId, scaling, exhaustion, DamageEffects.HURT, DeathMessageType.DEFAULT);
    }

    public DamageType(String msgId, DamageScaling scaling, float exhaustion, DamageEffects effects) {
        this(msgId, scaling, exhaustion, effects, DeathMessageType.DEFAULT);
    }

    public DamageType(String msgId, float exhaustion, DamageEffects effects) {
        this(msgId, DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, exhaustion, effects);
    }

    public DamageType(String msgId, float exhaustion) {
        this(msgId, DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, exhaustion);
    }
}

