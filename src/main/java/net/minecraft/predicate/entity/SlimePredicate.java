/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.predicate.entity;

import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.TypeSpecificPredicate;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class SlimePredicate
implements TypeSpecificPredicate {
    private final NumberRange.IntRange size;

    private SlimePredicate(NumberRange.IntRange size) {
        this.size = size;
    }

    public static SlimePredicate of(NumberRange.IntRange size) {
        return new SlimePredicate(size);
    }

    public static SlimePredicate fromJson(JsonObject json) {
        NumberRange.IntRange lv = NumberRange.IntRange.fromJson(json.get("size"));
        return new SlimePredicate(lv);
    }

    @Override
    public JsonObject typeSpecificToJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("size", this.size.toJson());
        return jsonObject;
    }

    @Override
    public boolean test(Entity entity, ServerWorld world, @Nullable Vec3d pos) {
        if (entity instanceof SlimeEntity) {
            SlimeEntity lv = (SlimeEntity)entity;
            return this.size.test(lv.getSize());
        }
        return false;
    }

    @Override
    public TypeSpecificPredicate.Deserializer getDeserializer() {
        return TypeSpecificPredicate.Deserializers.SLIME;
    }
}

