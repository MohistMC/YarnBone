/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.util.math;

import net.minecraft.util.math.GivensPair;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Math;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MatrixUtil {
    private static final float COT_PI_OVER_8 = 3.0f + 2.0f * Math.sqrt(2.0f);
    private static final GivensPair SIN_COS_PI_OVER_8 = GivensPair.fromAngle(0.7853982f);

    private MatrixUtil() {
    }

    public static Matrix4f scale(Matrix4f matrix, float scalar) {
        return matrix.set(matrix.m00() * scalar, matrix.m01() * scalar, matrix.m02() * scalar, matrix.m03() * scalar, matrix.m10() * scalar, matrix.m11() * scalar, matrix.m12() * scalar, matrix.m13() * scalar, matrix.m20() * scalar, matrix.m21() * scalar, matrix.m22() * scalar, matrix.m23() * scalar, matrix.m30() * scalar, matrix.m31() * scalar, matrix.m32() * scalar, matrix.m33() * scalar);
    }

    private static GivensPair approximateGivensQuaternion(float a11, float a12, float a22) {
        float j = a12;
        float i = 2.0f * (a11 - a22);
        if (COT_PI_OVER_8 * j * j < i * i) {
            return GivensPair.normalize(j, i);
        }
        return SIN_COS_PI_OVER_8;
    }

    private static GivensPair qrGivensQuaternion(float a1, float a2) {
        float h = (float)java.lang.Math.hypot(a1, a2);
        float i = h > 1.0E-6f ? a2 : 0.0f;
        float j = Math.abs(a1) + Math.max(h, 1.0E-6f);
        if (a1 < 0.0f) {
            float k = i;
            i = j;
            j = k;
        }
        return GivensPair.normalize(i, j);
    }

    private static void method_49742(Matrix3f matrix3f, Matrix3f matrix3f2) {
        matrix3f.mul(matrix3f2);
        matrix3f2.transpose();
        matrix3f2.mul(matrix3f);
        matrix3f.set(matrix3f2);
    }

    private static void applyJacobiIteration(Matrix3f matrix3f, Matrix3f matrix3f2, Quaternionf quaternionf, Quaternionf quaternionf2) {
        Quaternionf quaternionf3;
        GivensPair lv;
        if (matrix3f.m01 * matrix3f.m01 + matrix3f.m10 * matrix3f.m10 > 1.0E-6f) {
            lv = MatrixUtil.approximateGivensQuaternion(matrix3f.m00, 0.5f * (matrix3f.m01 + matrix3f.m10), matrix3f.m11);
            quaternionf3 = lv.method_49735(quaternionf);
            quaternionf2.mul(quaternionf3);
            lv.method_49734(matrix3f2);
            MatrixUtil.method_49742(matrix3f, matrix3f2);
        }
        if (matrix3f.m02 * matrix3f.m02 + matrix3f.m20 * matrix3f.m20 > 1.0E-6f) {
            lv = MatrixUtil.approximateGivensQuaternion(matrix3f.m00, 0.5f * (matrix3f.m02 + matrix3f.m20), matrix3f.m22).negateSin();
            quaternionf3 = lv.method_49732(quaternionf);
            quaternionf2.mul(quaternionf3);
            lv.method_49731(matrix3f2);
            MatrixUtil.method_49742(matrix3f, matrix3f2);
        }
        if (matrix3f.m12 * matrix3f.m12 + matrix3f.m21 * matrix3f.m21 > 1.0E-6f) {
            lv = MatrixUtil.approximateGivensQuaternion(matrix3f.m11, 0.5f * (matrix3f.m12 + matrix3f.m21), matrix3f.m22);
            quaternionf3 = lv.method_49729(quaternionf);
            quaternionf2.mul(quaternionf3);
            lv.method_49728(matrix3f2);
            MatrixUtil.method_49742(matrix3f, matrix3f2);
        }
    }

    public static Quaternionf method_49741(Matrix3f matrix3f, int i) {
        Quaternionf quaternionf = new Quaternionf();
        Matrix3f matrix3f2 = new Matrix3f();
        Quaternionf quaternionf2 = new Quaternionf();
        for (int j = 0; j < i; ++j) {
            MatrixUtil.applyJacobiIteration(matrix3f, matrix3f2, quaternionf2, quaternionf);
        }
        quaternionf.normalize();
        return quaternionf;
    }

    public static Triple<Quaternionf, Vector3f, Quaternionf> svdDecompose(Matrix3f A) {
        Matrix3f matrix3f2 = new Matrix3f(A);
        matrix3f2.transpose();
        matrix3f2.mul(A);
        Quaternionf quaternionf = MatrixUtil.method_49741(matrix3f2, 5);
        boolean bl = (double)matrix3f2.m00 < 1.0E-6;
        boolean bl2 = (double)matrix3f2.m11 < 1.0E-6;
        Matrix3f matrix3f3 = matrix3f2;
        Matrix3f matrix3f4 = A.rotate(quaternionf);
        float f = 1.0f;
        Quaternionf quaternionf2 = new Quaternionf();
        Quaternionf quaternionf3 = new Quaternionf();
        GivensPair lv = bl ? MatrixUtil.qrGivensQuaternion(matrix3f4.m11, -matrix3f4.m10) : MatrixUtil.qrGivensQuaternion(matrix3f4.m00, matrix3f4.m01);
        Quaternionf quaternionf4 = lv.method_49735(quaternionf3);
        Matrix3f matrix3f5 = lv.method_49734(matrix3f3);
        f *= matrix3f5.m22;
        quaternionf2.mul(quaternionf4);
        matrix3f5.transpose().mul(matrix3f4);
        matrix3f3 = matrix3f4;
        lv = bl ? MatrixUtil.qrGivensQuaternion(matrix3f5.m22, -matrix3f5.m20) : MatrixUtil.qrGivensQuaternion(matrix3f5.m00, matrix3f5.m02);
        lv = lv.negateSin();
        Quaternionf quaternionf5 = lv.method_49732(quaternionf3);
        Matrix3f matrix3f6 = lv.method_49731(matrix3f3);
        f *= matrix3f6.m11;
        quaternionf2.mul(quaternionf5);
        matrix3f6.transpose().mul(matrix3f5);
        matrix3f3 = matrix3f5;
        lv = bl2 ? MatrixUtil.qrGivensQuaternion(matrix3f6.m22, -matrix3f6.m21) : MatrixUtil.qrGivensQuaternion(matrix3f6.m11, matrix3f6.m12);
        Quaternionf quaternionf6 = lv.method_49729(quaternionf3);
        Matrix3f matrix3f7 = lv.method_49728(matrix3f3);
        f *= matrix3f7.m00;
        quaternionf2.mul(quaternionf6);
        matrix3f7.transpose().mul(matrix3f6);
        f = 1.0f / f;
        quaternionf2.mul(Math.sqrt(f));
        Vector3f vector3f = new Vector3f(matrix3f7.m00 * f, matrix3f7.m11 * f, matrix3f7.m22 * f);
        return Triple.of(quaternionf2, vector3f, quaternionf.conjugate());
    }
}

