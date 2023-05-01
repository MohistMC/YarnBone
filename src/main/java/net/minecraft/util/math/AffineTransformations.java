/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.util.math;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.Util;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Direction;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;

public class AffineTransformations {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Map<Direction, AffineTransformation> DIRECTION_ROTATIONS = Util.make(Maps.newEnumMap(Direction.class), map -> {
        map.put(Direction.SOUTH, AffineTransformation.identity());
        map.put(Direction.EAST, new AffineTransformation(null, new Quaternionf().rotateY(1.5707964f), null, null));
        map.put(Direction.WEST, new AffineTransformation(null, new Quaternionf().rotateY(-1.5707964f), null, null));
        map.put(Direction.NORTH, new AffineTransformation(null, new Quaternionf().rotateY((float)Math.PI), null, null));
        map.put(Direction.UP, new AffineTransformation(null, new Quaternionf().rotateX(-1.5707964f), null, null));
        map.put(Direction.DOWN, new AffineTransformation(null, new Quaternionf().rotateX(1.5707964f), null, null));
    });
    public static final Map<Direction, AffineTransformation> INVERTED_DIRECTION_ROTATIONS = Util.make(Maps.newEnumMap(Direction.class), map -> {
        for (Direction lv : Direction.values()) {
            map.put(lv, DIRECTION_ROTATIONS.get(lv).invert());
        }
    });

    public static AffineTransformation setupUvLock(AffineTransformation arg) {
        Matrix4f matrix4f = new Matrix4f().translation(0.5f, 0.5f, 0.5f);
        matrix4f.mul(arg.getMatrix());
        matrix4f.translate(-0.5f, -0.5f, -0.5f);
        return new AffineTransformation(matrix4f);
    }

    public static AffineTransformation method_35829(AffineTransformation arg) {
        Matrix4f matrix4f = new Matrix4f().translation(-0.5f, -0.5f, -0.5f);
        matrix4f.mul(arg.getMatrix());
        matrix4f.translate(0.5f, 0.5f, 0.5f);
        return new AffineTransformation(matrix4f);
    }

    public static AffineTransformation uvLock(AffineTransformation arg, Direction arg2, Supplier<String> supplier) {
        Direction lv = Direction.transform(arg.getMatrix(), arg2);
        AffineTransformation lv2 = arg.invert();
        if (lv2 == null) {
            LOGGER.warn(supplier.get());
            return new AffineTransformation(null, null, new Vector3f(0.0f, 0.0f, 0.0f), null);
        }
        AffineTransformation lv3 = INVERTED_DIRECTION_ROTATIONS.get(arg2).multiply(lv2).multiply(DIRECTION_ROTATIONS.get(lv));
        return AffineTransformations.setupUvLock(lv3);
    }
}

