/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.predicate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.State;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Nullable;

public class StatePredicate {
    public static final StatePredicate ANY = new StatePredicate(ImmutableList.of());
    private final List<Condition> conditions;

    private static Condition createPredicate(String key, JsonElement json) {
        if (json.isJsonPrimitive()) {
            String string2 = json.getAsString();
            return new ExactValueCondition(key, string2);
        }
        JsonObject jsonObject = JsonHelper.asObject(json, "value");
        String string3 = jsonObject.has("min") ? StatePredicate.asNullableString(jsonObject.get("min")) : null;
        String string4 = jsonObject.has("max") ? StatePredicate.asNullableString(jsonObject.get("max")) : null;
        return string3 != null && string3.equals(string4) ? new ExactValueCondition(key, string3) : new RangedValueCondition(key, string3, string4);
    }

    @Nullable
    private static String asNullableString(JsonElement json) {
        if (json.isJsonNull()) {
            return null;
        }
        return json.getAsString();
    }

    StatePredicate(List<Condition> conditions) {
        this.conditions = ImmutableList.copyOf(conditions);
    }

    public <S extends State<?, S>> boolean test(StateManager<?, S> stateManager, S container) {
        for (Condition lv : this.conditions) {
            if (lv.test(stateManager, container)) continue;
            return false;
        }
        return true;
    }

    public boolean test(BlockState state) {
        return this.test(state.getBlock().getStateManager(), state);
    }

    public boolean test(FluidState state) {
        return this.test(state.getFluid().getStateManager(), state);
    }

    public void check(StateManager<?, ?> factory, Consumer<String> reporter) {
        this.conditions.forEach(condition -> condition.reportMissing(factory, reporter));
    }

    public static StatePredicate fromJson(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = JsonHelper.asObject(json, "properties");
        ArrayList<Condition> list = Lists.newArrayList();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            list.add(StatePredicate.createPredicate(entry.getKey(), entry.getValue()));
        }
        return new StatePredicate(list);
    }

    public JsonElement toJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        if (!this.conditions.isEmpty()) {
            this.conditions.forEach(condition -> jsonObject.add(condition.getKey(), condition.toJson()));
        }
        return jsonObject;
    }

    static class ExactValueCondition
    extends Condition {
        private final String value;

        public ExactValueCondition(String key, String value) {
            super(key);
            this.value = value;
        }

        @Override
        protected <T extends Comparable<T>> boolean test(State<?, ?> state, Property<T> property) {
            Comparable comparable = state.get(property);
            Optional<T> optional = property.parse(this.value);
            return optional.isPresent() && comparable.compareTo((Comparable)((Comparable)optional.get())) == 0;
        }

        @Override
        public JsonElement toJson() {
            return new JsonPrimitive(this.value);
        }
    }

    static class RangedValueCondition
    extends Condition {
        @Nullable
        private final String min;
        @Nullable
        private final String max;

        public RangedValueCondition(String key, @Nullable String min, @Nullable String max) {
            super(key);
            this.min = min;
            this.max = max;
        }

        @Override
        protected <T extends Comparable<T>> boolean test(State<?, ?> state, Property<T> property) {
            Optional<T> optional;
            Comparable comparable = state.get(property);
            if (!(this.min == null || (optional = property.parse(this.min)).isPresent() && comparable.compareTo((Comparable)((Comparable)optional.get())) >= 0)) {
                return false;
            }
            return this.max == null || (optional = property.parse(this.max)).isPresent() && comparable.compareTo((Comparable)((Comparable)optional.get())) <= 0;
        }

        @Override
        public JsonElement toJson() {
            JsonObject jsonObject = new JsonObject();
            if (this.min != null) {
                jsonObject.addProperty("min", this.min);
            }
            if (this.max != null) {
                jsonObject.addProperty("max", this.max);
            }
            return jsonObject;
        }
    }

    static abstract class Condition {
        private final String key;

        public Condition(String key) {
            this.key = key;
        }

        public <S extends State<?, S>> boolean test(StateManager<?, S> stateManager, S state) {
            Property<?> lv = stateManager.getProperty(this.key);
            if (lv == null) {
                return false;
            }
            return this.test(state, lv);
        }

        protected abstract <T extends Comparable<T>> boolean test(State<?, ?> var1, Property<T> var2);

        public abstract JsonElement toJson();

        public String getKey() {
            return this.key;
        }

        public void reportMissing(StateManager<?, ?> factory, Consumer<String> reporter) {
            Property<?> lv = factory.getProperty(this.key);
            if (lv == null) {
                reporter.accept(this.key);
            }
        }
    }

    public static class Builder {
        private final List<Condition> conditions = Lists.newArrayList();

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder exactMatch(Property<?> property, String valueName) {
            this.conditions.add(new ExactValueCondition(property.getName(), valueName));
            return this;
        }

        public Builder exactMatch(Property<Integer> property, int value) {
            return this.exactMatch((Property)property, (Comparable<T> & StringIdentifiable)Integer.toString(value));
        }

        public Builder exactMatch(Property<Boolean> property, boolean value) {
            return this.exactMatch((Property)property, (Comparable<T> & StringIdentifiable)Boolean.toString(value));
        }

        public <T extends Comparable<T> & StringIdentifiable> Builder exactMatch(Property<T> property, T value) {
            return this.exactMatch(property, (T)((StringIdentifiable)value).asString());
        }

        public StatePredicate build() {
            return new StatePredicate(this.conditions);
        }
    }
}

