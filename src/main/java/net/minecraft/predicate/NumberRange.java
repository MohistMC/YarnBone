/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public abstract class NumberRange<T extends Number> {
    public static final SimpleCommandExceptionType EXCEPTION_EMPTY = new SimpleCommandExceptionType(Text.translatable("argument.range.empty"));
    public static final SimpleCommandExceptionType EXCEPTION_SWAPPED = new SimpleCommandExceptionType(Text.translatable("argument.range.swapped"));
    @Nullable
    protected final T min;
    @Nullable
    protected final T max;

    protected NumberRange(@Nullable T min, @Nullable T max) {
        this.min = min;
        this.max = max;
    }

    @Nullable
    public T getMin() {
        return this.min;
    }

    @Nullable
    public T getMax() {
        return this.max;
    }

    public boolean isDummy() {
        return this.min == null && this.max == null;
    }

    public JsonElement toJson() {
        if (this.isDummy()) {
            return JsonNull.INSTANCE;
        }
        if (this.min != null && this.min.equals(this.max)) {
            return new JsonPrimitive((Number)this.min);
        }
        JsonObject jsonObject = new JsonObject();
        if (this.min != null) {
            jsonObject.addProperty("min", (Number)this.min);
        }
        if (this.max != null) {
            jsonObject.addProperty("max", (Number)this.max);
        }
        return jsonObject;
    }

    protected static <T extends Number, R extends NumberRange<T>> R fromJson(@Nullable JsonElement json, R fallback, BiFunction<JsonElement, String, T> asNumber, Factory<T, R> factory) {
        if (json == null || json.isJsonNull()) {
            return fallback;
        }
        if (JsonHelper.isNumber(json)) {
            Number number = (Number)asNumber.apply(json, "value");
            return factory.create(number, number);
        }
        JsonObject jsonObject = JsonHelper.asObject(json, "value");
        Number number2 = jsonObject.has("min") ? (Number)((Number)asNumber.apply(jsonObject.get("min"), "min")) : (Number)null;
        Number number3 = jsonObject.has("max") ? (Number)((Number)asNumber.apply(jsonObject.get("max"), "max")) : (Number)null;
        return factory.create(number2, number3);
    }

    protected static <T extends Number, R extends NumberRange<T>> R parse(StringReader commandReader, CommandFactory<T, R> commandFactory, Function<String, T> converter, Supplier<DynamicCommandExceptionType> exceptionTypeSupplier, Function<T, T> mapper) throws CommandSyntaxException {
        if (!commandReader.canRead()) {
            throw EXCEPTION_EMPTY.createWithContext(commandReader);
        }
        int i = commandReader.getCursor();
        try {
            Number number2;
            Number number = (Number)NumberRange.map(NumberRange.fromStringReader(commandReader, converter, exceptionTypeSupplier), mapper);
            if (commandReader.canRead(2) && commandReader.peek() == '.' && commandReader.peek(1) == '.') {
                commandReader.skip();
                commandReader.skip();
                number2 = (Number)NumberRange.map(NumberRange.fromStringReader(commandReader, converter, exceptionTypeSupplier), mapper);
                if (number == null && number2 == null) {
                    throw EXCEPTION_EMPTY.createWithContext(commandReader);
                }
            } else {
                number2 = number;
            }
            if (number == null && number2 == null) {
                throw EXCEPTION_EMPTY.createWithContext(commandReader);
            }
            return commandFactory.create(commandReader, number, number2);
        }
        catch (CommandSyntaxException commandSyntaxException) {
            commandReader.setCursor(i);
            throw new CommandSyntaxException(commandSyntaxException.getType(), commandSyntaxException.getRawMessage(), commandSyntaxException.getInput(), i);
        }
    }

    @Nullable
    private static <T extends Number> T fromStringReader(StringReader reader, Function<String, T> converter, Supplier<DynamicCommandExceptionType> exceptionTypeSupplier) throws CommandSyntaxException {
        int i = reader.getCursor();
        while (reader.canRead() && NumberRange.isNextCharValid(reader)) {
            reader.skip();
        }
        String string = reader.getString().substring(i, reader.getCursor());
        if (string.isEmpty()) {
            return null;
        }
        try {
            return (T)((Number)converter.apply(string));
        }
        catch (NumberFormatException numberFormatException) {
            throw exceptionTypeSupplier.get().createWithContext(reader, string);
        }
    }

    private static boolean isNextCharValid(StringReader reader) {
        char c = reader.peek();
        if (c >= '0' && c <= '9' || c == '-') {
            return true;
        }
        if (c == '.') {
            return !reader.canRead(2) || reader.peek(1) != '.';
        }
        return false;
    }

    @Nullable
    private static <T> T map(@Nullable T object, Function<T, T> function) {
        return object == null ? null : (T)function.apply(object);
    }

    @FunctionalInterface
    protected static interface Factory<T extends Number, R extends NumberRange<T>> {
        public R create(@Nullable T var1, @Nullable T var2);
    }

    @FunctionalInterface
    protected static interface CommandFactory<T extends Number, R extends NumberRange<T>> {
        public R create(StringReader var1, @Nullable T var2, @Nullable T var3) throws CommandSyntaxException;
    }

    public static class FloatRange
    extends NumberRange<Double> {
        public static final FloatRange ANY = new FloatRange(null, null);
        @Nullable
        private final Double squaredMin;
        @Nullable
        private final Double squaredMax;

        private static FloatRange create(StringReader reader, @Nullable Double min, @Nullable Double max) throws CommandSyntaxException {
            if (min != null && max != null && min > max) {
                throw EXCEPTION_SWAPPED.createWithContext(reader);
            }
            return new FloatRange(min, max);
        }

        @Nullable
        private static Double square(@Nullable Double value) {
            return value == null ? null : Double.valueOf(value * value);
        }

        private FloatRange(@Nullable Double min, @Nullable Double max) {
            super(min, max);
            this.squaredMin = FloatRange.square(min);
            this.squaredMax = FloatRange.square(max);
        }

        public static FloatRange exactly(double value) {
            return new FloatRange(value, value);
        }

        public static FloatRange between(double min, double max) {
            return new FloatRange(min, max);
        }

        public static FloatRange atLeast(double value) {
            return new FloatRange(value, null);
        }

        public static FloatRange atMost(double value) {
            return new FloatRange(null, value);
        }

        public boolean test(double value) {
            if (this.min != null && (Double)this.min > value) {
                return false;
            }
            return this.max == null || !((Double)this.max < value);
        }

        public boolean testSqrt(double value) {
            if (this.squaredMin != null && this.squaredMin > value) {
                return false;
            }
            return this.squaredMax == null || !(this.squaredMax < value);
        }

        public static FloatRange fromJson(@Nullable JsonElement element) {
            return FloatRange.fromJson(element, ANY, JsonHelper::asDouble, FloatRange::new);
        }

        public static FloatRange parse(StringReader reader) throws CommandSyntaxException {
            return FloatRange.parse(reader, value -> value);
        }

        public static FloatRange parse(StringReader reader, Function<Double, Double> mapper) throws CommandSyntaxException {
            return FloatRange.parse(reader, FloatRange::create, Double::parseDouble, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidDouble, mapper);
        }
    }

    public static class IntRange
    extends NumberRange<Integer> {
        public static final IntRange ANY = new IntRange(null, null);
        @Nullable
        private final Long minSquared;
        @Nullable
        private final Long maxSquared;

        private static IntRange parse(StringReader reader, @Nullable Integer min, @Nullable Integer max) throws CommandSyntaxException {
            if (min != null && max != null && min > max) {
                throw EXCEPTION_SWAPPED.createWithContext(reader);
            }
            return new IntRange(min, max);
        }

        @Nullable
        private static Long squared(@Nullable Integer value) {
            return value == null ? null : Long.valueOf(value.longValue() * value.longValue());
        }

        private IntRange(@Nullable Integer min, @Nullable Integer max) {
            super(min, max);
            this.minSquared = IntRange.squared(min);
            this.maxSquared = IntRange.squared(max);
        }

        public static IntRange exactly(int value) {
            return new IntRange(value, value);
        }

        public static IntRange between(int min, int max) {
            return new IntRange(min, max);
        }

        public static IntRange atLeast(int value) {
            return new IntRange(value, null);
        }

        public static IntRange atMost(int value) {
            return new IntRange(null, value);
        }

        public boolean test(int value) {
            if (this.min != null && (Integer)this.min > value) {
                return false;
            }
            return this.max == null || (Integer)this.max >= value;
        }

        public boolean testSqrt(long value) {
            if (this.minSquared != null && this.minSquared > value) {
                return false;
            }
            return this.maxSquared == null || this.maxSquared >= value;
        }

        public static IntRange fromJson(@Nullable JsonElement element) {
            return IntRange.fromJson(element, ANY, JsonHelper::asInt, IntRange::new);
        }

        public static IntRange parse(StringReader reader) throws CommandSyntaxException {
            return IntRange.fromStringReader(reader, value -> value);
        }

        public static IntRange fromStringReader(StringReader reader, Function<Integer, Integer> converter) throws CommandSyntaxException {
            return IntRange.parse(reader, IntRange::parse, Integer::parseInt, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidInt, converter);
        }
    }
}

