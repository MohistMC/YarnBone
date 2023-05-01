/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.text;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.Message;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.BlockNbtDataSource;
import net.minecraft.text.EntityNbtDataSource;
import net.minecraft.text.KeybindTextContent;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.NbtDataSource;
import net.minecraft.text.NbtTextContent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.ScoreTextContent;
import net.minecraft.text.SelectorTextContent;
import net.minecraft.text.StorageNbtDataSource;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.LowercaseEnumTypeAdapterFactory;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

public interface Text
extends Message,
StringVisitable {
    public Style getStyle();

    public TextContent getContent();

    @Override
    default public String getString() {
        return StringVisitable.super.getString();
    }

    default public String asTruncatedString(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        this.visit(string -> {
            int j = length - stringBuilder.length();
            if (j <= 0) {
                return TERMINATE_VISIT;
            }
            stringBuilder.append(string.length() <= j ? string : string.substring(0, j));
            return Optional.empty();
        });
        return stringBuilder.toString();
    }

    public List<Text> getSiblings();

    default public MutableText copyContentOnly() {
        return MutableText.of(this.getContent());
    }

    default public MutableText copy() {
        return new MutableText(this.getContent(), new ArrayList<Text>(this.getSiblings()), this.getStyle());
    }

    public OrderedText asOrderedText();

    @Override
    default public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> styledVisitor, Style style) {
        Style lv = this.getStyle().withParent(style);
        Optional<T> optional = this.getContent().visit(styledVisitor, lv);
        if (optional.isPresent()) {
            return optional;
        }
        for (Text lv2 : this.getSiblings()) {
            Optional<T> optional2 = lv2.visit(styledVisitor, lv);
            if (!optional2.isPresent()) continue;
            return optional2;
        }
        return Optional.empty();
    }

    @Override
    default public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
        Optional<T> optional = this.getContent().visit(visitor);
        if (optional.isPresent()) {
            return optional;
        }
        for (Text lv : this.getSiblings()) {
            Optional<T> optional2 = lv.visit(visitor);
            if (!optional2.isPresent()) continue;
            return optional2;
        }
        return Optional.empty();
    }

    default public List<Text> withoutStyle() {
        return this.getWithStyle(Style.EMPTY);
    }

    default public List<Text> getWithStyle(Style style) {
        ArrayList<Text> list = Lists.newArrayList();
        this.visit((styleOverride, text) -> {
            if (!text.isEmpty()) {
                list.add(Text.literal(text).fillStyle(styleOverride));
            }
            return Optional.empty();
        }, style);
        return list;
    }

    default public boolean contains(Text text) {
        List<Text> list2;
        if (this.equals(text)) {
            return true;
        }
        List<Text> list = this.withoutStyle();
        return Collections.indexOfSubList(list, list2 = text.getWithStyle(this.getStyle())) != -1;
    }

    public static Text of(@Nullable String string) {
        return string != null ? Text.literal(string) : ScreenTexts.EMPTY;
    }

    public static MutableText literal(String string) {
        return MutableText.of(new LiteralTextContent(string));
    }

    public static MutableText translatable(String key) {
        return MutableText.of(new TranslatableTextContent(key, null, TranslatableTextContent.EMPTY_ARGUMENTS));
    }

    public static MutableText translatable(String key, Object ... args) {
        return MutableText.of(new TranslatableTextContent(key, null, args));
    }

    public static MutableText translatableWithFallback(String key, @Nullable String fallback) {
        return MutableText.of(new TranslatableTextContent(key, fallback, TranslatableTextContent.EMPTY_ARGUMENTS));
    }

    public static MutableText translatableWithFallback(String key, @Nullable String fallback, Object ... args) {
        return MutableText.of(new TranslatableTextContent(key, fallback, args));
    }

    public static MutableText empty() {
        return MutableText.of(TextContent.EMPTY);
    }

    public static MutableText keybind(String string) {
        return MutableText.of(new KeybindTextContent(string));
    }

    public static MutableText nbt(String rawPath, boolean interpret, Optional<Text> separator, NbtDataSource dataSource) {
        return MutableText.of(new NbtTextContent(rawPath, interpret, separator, dataSource));
    }

    public static MutableText score(String name, String objective) {
        return MutableText.of(new ScoreTextContent(name, objective));
    }

    public static MutableText selector(String pattern, Optional<Text> separator) {
        return MutableText.of(new SelectorTextContent(pattern, separator));
    }

    public static class Serializer
    implements JsonDeserializer<MutableText>,
    JsonSerializer<Text> {
        private static final Gson GSON = Util.make(() -> {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.disableHtmlEscaping();
            gsonBuilder.registerTypeHierarchyAdapter(Text.class, new Serializer());
            gsonBuilder.registerTypeHierarchyAdapter(Style.class, new Style.Serializer());
            gsonBuilder.registerTypeAdapterFactory(new LowercaseEnumTypeAdapterFactory());
            return gsonBuilder.create();
        });
        private static final Field JSON_READER_POS = Util.make(() -> {
            try {
                new JsonReader(new StringReader(""));
                Field field = JsonReader.class.getDeclaredField("pos");
                field.setAccessible(true);
                return field;
            }
            catch (NoSuchFieldException noSuchFieldException) {
                throw new IllegalStateException("Couldn't get field 'pos' for JsonReader", noSuchFieldException);
            }
        });
        private static final Field JSON_READER_LINE_START = Util.make(() -> {
            try {
                new JsonReader(new StringReader(""));
                Field field = JsonReader.class.getDeclaredField("lineStart");
                field.setAccessible(true);
                return field;
            }
            catch (NoSuchFieldException noSuchFieldException) {
                throw new IllegalStateException("Couldn't get field 'lineStart' for JsonReader", noSuchFieldException);
            }
        });

        /*
         * WARNING - void declaration
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        public MutableText deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if (jsonElement.isJsonPrimitive()) {
                return Text.literal(jsonElement.getAsString());
            }
            if (jsonElement.isJsonObject()) {
                MutableText lv;
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (jsonObject.has("text")) {
                    string = JsonHelper.getString(jsonObject, "text");
                    lv = string.isEmpty() ? Text.empty() : Text.literal(string);
                } else if (jsonObject.has("translate")) {
                    string = JsonHelper.getString(jsonObject, "translate");
                    String string2 = JsonHelper.getString(jsonObject, "fallback", null);
                    if (jsonObject.has("with")) {
                        JsonArray jsonArray = JsonHelper.getArray(jsonObject, "with");
                        Object[] objectArray = new Object[jsonArray.size()];
                        for (int i = 0; i < objectArray.length; ++i) {
                            objectArray[i] = Serializer.optimizeArgument(this.deserialize(jsonArray.get(i), type, jsonDeserializationContext));
                        }
                        lv = Text.translatableWithFallback(string, string2, objectArray);
                    } else {
                        lv = Text.translatableWithFallback(string, string2);
                    }
                } else if (jsonObject.has("score")) {
                    JsonObject jsonObject2 = JsonHelper.getObject(jsonObject, "score");
                    if (!jsonObject2.has("name") || !jsonObject2.has("objective")) throw new JsonParseException("A score component needs a least a name and an objective");
                    lv = Text.score(JsonHelper.getString(jsonObject2, "name"), JsonHelper.getString(jsonObject2, "objective"));
                } else if (jsonObject.has("selector")) {
                    Optional<Text> optional = this.getSeparator(type, jsonDeserializationContext, jsonObject);
                    lv = Text.selector(JsonHelper.getString(jsonObject, "selector"), optional);
                } else if (jsonObject.has("keybind")) {
                    lv = Text.keybind(JsonHelper.getString(jsonObject, "keybind"));
                } else {
                    void var9_20;
                    if (!jsonObject.has("nbt")) throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
                    string = JsonHelper.getString(jsonObject, "nbt");
                    Optional<Text> optional2 = this.getSeparator(type, jsonDeserializationContext, jsonObject);
                    boolean bl = JsonHelper.getBoolean(jsonObject, "interpret", false);
                    if (jsonObject.has("block")) {
                        BlockNbtDataSource blockNbtDataSource = new BlockNbtDataSource(JsonHelper.getString(jsonObject, "block"));
                    } else if (jsonObject.has("entity")) {
                        EntityNbtDataSource entityNbtDataSource = new EntityNbtDataSource(JsonHelper.getString(jsonObject, "entity"));
                    } else {
                        if (!jsonObject.has("storage")) throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
                        StorageNbtDataSource storageNbtDataSource = new StorageNbtDataSource(new Identifier(JsonHelper.getString(jsonObject, "storage")));
                    }
                    lv = Text.nbt(string, bl, optional2, (NbtDataSource)var9_20);
                }
                if (jsonObject.has("extra")) {
                    JsonArray jsonArray2 = JsonHelper.getArray(jsonObject, "extra");
                    if (jsonArray2.size() <= 0) throw new JsonParseException("Unexpected empty array of components");
                    for (int j = 0; j < jsonArray2.size(); ++j) {
                        lv.append(this.deserialize(jsonArray2.get(j), type, jsonDeserializationContext));
                    }
                }
                lv.setStyle((Style)jsonDeserializationContext.deserialize(jsonElement, (Type)((Object)Style.class)));
                return lv;
            }
            if (!jsonElement.isJsonArray()) throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
            JsonArray jsonArray3 = jsonElement.getAsJsonArray();
            MutableText lv = null;
            for (JsonElement jsonElement2 : jsonArray3) {
                MutableText lv3 = this.deserialize(jsonElement2, jsonElement2.getClass(), jsonDeserializationContext);
                if (lv == null) {
                    lv = lv3;
                    continue;
                }
                lv.append(lv3);
            }
            return lv;
        }

        private static Object optimizeArgument(Object text) {
            TextContent lv2;
            Text lv;
            if (text instanceof Text && (lv = (Text)text).getStyle().isEmpty() && lv.getSiblings().isEmpty() && (lv2 = lv.getContent()) instanceof LiteralTextContent) {
                LiteralTextContent lv3 = (LiteralTextContent)lv2;
                return lv3.string();
            }
            return text;
        }

        private Optional<Text> getSeparator(Type type, JsonDeserializationContext context, JsonObject json) {
            if (json.has("separator")) {
                return Optional.of(this.deserialize(json.get("separator"), type, context));
            }
            return Optional.empty();
        }

        private void addStyle(Style style, JsonObject json, JsonSerializationContext context) {
            JsonElement jsonElement = context.serialize(style);
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject2 = (JsonObject)jsonElement;
                for (Map.Entry<String, JsonElement> entry : jsonObject2.entrySet()) {
                    json.add(entry.getKey(), entry.getValue());
                }
            }
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        public JsonElement serialize(Text arg, Type type, JsonSerializationContext jsonSerializationContext) {
            TextContent lv2;
            JsonObject jsonObject = new JsonObject();
            if (!arg.getStyle().isEmpty()) {
                this.addStyle(arg.getStyle(), jsonObject, jsonSerializationContext);
            }
            if (!arg.getSiblings().isEmpty()) {
                JsonArray jsonArray = new JsonArray();
                for (Text lv : arg.getSiblings()) {
                    jsonArray.add(this.serialize(lv, (Type)((Object)Text.class), jsonSerializationContext));
                }
                jsonObject.add("extra", jsonArray);
            }
            if ((lv2 = arg.getContent()) == TextContent.EMPTY) {
                jsonObject.addProperty("text", "");
                return jsonObject;
            } else if (lv2 instanceof LiteralTextContent) {
                LiteralTextContent lv3 = (LiteralTextContent)lv2;
                jsonObject.addProperty("text", lv3.string());
                return jsonObject;
            } else if (lv2 instanceof TranslatableTextContent) {
                TranslatableTextContent lv4 = (TranslatableTextContent)lv2;
                jsonObject.addProperty("translate", lv4.getKey());
                String string = lv4.getFallback();
                if (string != null) {
                    jsonObject.addProperty("fallback", string);
                }
                if (lv4.getArgs().length <= 0) return jsonObject;
                JsonArray jsonArray2 = new JsonArray();
                for (Object object : lv4.getArgs()) {
                    if (object instanceof Text) {
                        jsonArray2.add(this.serialize((Text)object, (Type)object.getClass(), jsonSerializationContext));
                        continue;
                    }
                    jsonArray2.add(new JsonPrimitive(String.valueOf(object)));
                }
                jsonObject.add("with", jsonArray2);
                return jsonObject;
            } else if (lv2 instanceof ScoreTextContent) {
                ScoreTextContent lv5 = (ScoreTextContent)lv2;
                JsonObject jsonObject2 = new JsonObject();
                jsonObject2.addProperty("name", lv5.getName());
                jsonObject2.addProperty("objective", lv5.getObjective());
                jsonObject.add("score", jsonObject2);
                return jsonObject;
            } else if (lv2 instanceof SelectorTextContent) {
                SelectorTextContent lv6 = (SelectorTextContent)lv2;
                jsonObject.addProperty("selector", lv6.getPattern());
                this.addSeparator(jsonSerializationContext, jsonObject, lv6.getSeparator());
                return jsonObject;
            } else if (lv2 instanceof KeybindTextContent) {
                KeybindTextContent lv7 = (KeybindTextContent)lv2;
                jsonObject.addProperty("keybind", lv7.getKey());
                return jsonObject;
            } else {
                if (!(lv2 instanceof NbtTextContent)) throw new IllegalArgumentException("Don't know how to serialize " + lv2 + " as a Component");
                NbtTextContent lv8 = (NbtTextContent)lv2;
                jsonObject.addProperty("nbt", lv8.getPath());
                jsonObject.addProperty("interpret", lv8.shouldInterpret());
                this.addSeparator(jsonSerializationContext, jsonObject, lv8.getSeparator());
                NbtDataSource lv9 = lv8.getDataSource();
                if (lv9 instanceof BlockNbtDataSource) {
                    BlockNbtDataSource lv10 = (BlockNbtDataSource)lv9;
                    jsonObject.addProperty("block", lv10.rawPos());
                    return jsonObject;
                } else if (lv9 instanceof EntityNbtDataSource) {
                    EntityNbtDataSource lv11 = (EntityNbtDataSource)lv9;
                    jsonObject.addProperty("entity", lv11.rawSelector());
                    return jsonObject;
                } else {
                    if (!(lv9 instanceof StorageNbtDataSource)) throw new IllegalArgumentException("Don't know how to serialize " + lv2 + " as a Component");
                    StorageNbtDataSource lv12 = (StorageNbtDataSource)lv9;
                    jsonObject.addProperty("storage", lv12.id().toString());
                }
            }
            return jsonObject;
        }

        private void addSeparator(JsonSerializationContext context, JsonObject json, Optional<Text> separator2) {
            separator2.ifPresent(separator -> json.add("separator", this.serialize((Text)separator, (Type)separator.getClass(), context)));
        }

        public static String toJson(Text text) {
            return GSON.toJson(text);
        }

        public static String toSortedJsonString(Text text) {
            return JsonHelper.toSortedString(Serializer.toJsonTree(text));
        }

        public static JsonElement toJsonTree(Text text) {
            return GSON.toJsonTree(text);
        }

        @Nullable
        public static MutableText fromJson(String json) {
            return JsonHelper.deserializeNullable(GSON, json, MutableText.class, false);
        }

        @Nullable
        public static MutableText fromJson(JsonElement json) {
            return GSON.fromJson(json, MutableText.class);
        }

        @Nullable
        public static MutableText fromLenientJson(String json) {
            return JsonHelper.deserializeNullable(GSON, json, MutableText.class, true);
        }

        public static MutableText fromJson(com.mojang.brigadier.StringReader reader) {
            try {
                JsonReader jsonReader = new JsonReader(new StringReader(reader.getRemaining()));
                jsonReader.setLenient(false);
                MutableText lv = GSON.getAdapter(MutableText.class).read(jsonReader);
                reader.setCursor(reader.getCursor() + Serializer.getPosition(jsonReader));
                return lv;
            }
            catch (IOException | StackOverflowError throwable) {
                throw new JsonParseException(throwable);
            }
        }

        private static int getPosition(JsonReader reader) {
            try {
                return JSON_READER_POS.getInt(reader) - JSON_READER_LINE_START.getInt(reader) + 1;
            }
            catch (IllegalAccessException illegalAccessException) {
                throw new IllegalStateException("Couldn't read position of JsonReader", illegalAccessException);
            }
        }

        @Override
        public /* synthetic */ JsonElement serialize(Object text, Type type, JsonSerializationContext context) {
            return this.serialize((Text)text, type, context);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            return this.deserialize(json, type, context);
        }
    }
}

