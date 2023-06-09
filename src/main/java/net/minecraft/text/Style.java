/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.text;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class Style {
    public static final Style EMPTY = new Style(null, null, null, null, null, null, null, null, null, null);
    public static final Codec<Style> CODEC = RecordCodecBuilder.create(instance -> instance.group(TextColor.CODEC.optionalFieldOf("color").forGetter(style -> Optional.ofNullable(style.color)), Codec.BOOL.optionalFieldOf("bold").forGetter(style -> Optional.ofNullable(style.bold)), Codec.BOOL.optionalFieldOf("italic").forGetter(style -> Optional.ofNullable(style.italic)), Codec.BOOL.optionalFieldOf("underlined").forGetter(style -> Optional.ofNullable(style.underlined)), Codec.BOOL.optionalFieldOf("strikethrough").forGetter(style -> Optional.ofNullable(style.strikethrough)), Codec.BOOL.optionalFieldOf("obfuscated").forGetter(style -> Optional.ofNullable(style.obfuscated)), Codec.STRING.optionalFieldOf("insertion").forGetter(style -> Optional.ofNullable(style.insertion)), Identifier.CODEC.optionalFieldOf("font").forGetter(style -> Optional.ofNullable(style.font))).apply((Applicative<Style, ?>)instance, Style::of));
    public static final Identifier DEFAULT_FONT_ID = new Identifier("minecraft", "default");
    @Nullable
    final TextColor color;
    @Nullable
    final Boolean bold;
    @Nullable
    final Boolean italic;
    @Nullable
    final Boolean underlined;
    @Nullable
    final Boolean strikethrough;
    @Nullable
    final Boolean obfuscated;
    @Nullable
    final ClickEvent clickEvent;
    @Nullable
    final HoverEvent hoverEvent;
    @Nullable
    final String insertion;
    @Nullable
    final Identifier font;

    private static Style of(Optional<TextColor> color, Optional<Boolean> bold, Optional<Boolean> italic, Optional<Boolean> underlined, Optional<Boolean> strikethrough, Optional<Boolean> obfuscated, Optional<String> insertion, Optional<Identifier> font) {
        return new Style(color.orElse(null), bold.orElse(null), italic.orElse(null), underlined.orElse(null), strikethrough.orElse(null), obfuscated.orElse(null), null, null, insertion.orElse(null), font.orElse(null));
    }

    Style(@Nullable TextColor color, @Nullable Boolean bold, @Nullable Boolean italic, @Nullable Boolean underlined, @Nullable Boolean strikethrough, @Nullable Boolean obfuscated, @Nullable ClickEvent clickEvent, @Nullable HoverEvent hoverEvent, @Nullable String insertion, @Nullable Identifier font) {
        this.color = color;
        this.bold = bold;
        this.italic = italic;
        this.underlined = underlined;
        this.strikethrough = strikethrough;
        this.obfuscated = obfuscated;
        this.clickEvent = clickEvent;
        this.hoverEvent = hoverEvent;
        this.insertion = insertion;
        this.font = font;
    }

    @Nullable
    public TextColor getColor() {
        return this.color;
    }

    public boolean isBold() {
        return this.bold == Boolean.TRUE;
    }

    public boolean isItalic() {
        return this.italic == Boolean.TRUE;
    }

    public boolean isStrikethrough() {
        return this.strikethrough == Boolean.TRUE;
    }

    public boolean isUnderlined() {
        return this.underlined == Boolean.TRUE;
    }

    public boolean isObfuscated() {
        return this.obfuscated == Boolean.TRUE;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    @Nullable
    public ClickEvent getClickEvent() {
        return this.clickEvent;
    }

    @Nullable
    public HoverEvent getHoverEvent() {
        return this.hoverEvent;
    }

    @Nullable
    public String getInsertion() {
        return this.insertion;
    }

    public Identifier getFont() {
        return this.font != null ? this.font : DEFAULT_FONT_ID;
    }

    public Style withColor(@Nullable TextColor color) {
        return new Style(color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style withColor(@Nullable Formatting color) {
        return this.withColor(color != null ? TextColor.fromFormatting(color) : null);
    }

    public Style withColor(int rgbColor) {
        return this.withColor(TextColor.fromRgb(rgbColor));
    }

    public Style withBold(@Nullable Boolean bold) {
        return new Style(this.color, bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style withItalic(@Nullable Boolean italic) {
        return new Style(this.color, this.bold, italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style withUnderline(@Nullable Boolean underline) {
        return new Style(this.color, this.bold, this.italic, underline, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style withStrikethrough(@Nullable Boolean strikethrough) {
        return new Style(this.color, this.bold, this.italic, this.underlined, strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style withObfuscated(@Nullable Boolean obfuscated) {
        return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style withClickEvent(@Nullable ClickEvent clickEvent) {
        return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style withHoverEvent(@Nullable HoverEvent hoverEvent) {
        return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, hoverEvent, this.insertion, this.font);
    }

    public Style withInsertion(@Nullable String insertion) {
        return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, insertion, this.font);
    }

    public Style withFont(@Nullable Identifier font) {
        return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, font);
    }

    public Style withFormatting(Formatting formatting) {
        TextColor lv = this.color;
        Boolean boolean_ = this.bold;
        Boolean boolean2 = this.italic;
        Boolean boolean3 = this.strikethrough;
        Boolean boolean4 = this.underlined;
        Boolean boolean5 = this.obfuscated;
        switch (formatting) {
            case OBFUSCATED: {
                boolean5 = true;
                break;
            }
            case BOLD: {
                boolean_ = true;
                break;
            }
            case STRIKETHROUGH: {
                boolean3 = true;
                break;
            }
            case UNDERLINE: {
                boolean4 = true;
                break;
            }
            case ITALIC: {
                boolean2 = true;
                break;
            }
            case RESET: {
                return EMPTY;
            }
            default: {
                lv = TextColor.fromFormatting(formatting);
            }
        }
        return new Style(lv, boolean_, boolean2, boolean4, boolean3, boolean5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style withExclusiveFormatting(Formatting formatting) {
        TextColor lv = this.color;
        Boolean boolean_ = this.bold;
        Boolean boolean2 = this.italic;
        Boolean boolean3 = this.strikethrough;
        Boolean boolean4 = this.underlined;
        Boolean boolean5 = this.obfuscated;
        switch (formatting) {
            case OBFUSCATED: {
                boolean5 = true;
                break;
            }
            case BOLD: {
                boolean_ = true;
                break;
            }
            case STRIKETHROUGH: {
                boolean3 = true;
                break;
            }
            case UNDERLINE: {
                boolean4 = true;
                break;
            }
            case ITALIC: {
                boolean2 = true;
                break;
            }
            case RESET: {
                return EMPTY;
            }
            default: {
                boolean5 = false;
                boolean_ = false;
                boolean3 = false;
                boolean4 = false;
                boolean2 = false;
                lv = TextColor.fromFormatting(formatting);
            }
        }
        return new Style(lv, boolean_, boolean2, boolean4, boolean3, boolean5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style withFormatting(Formatting ... formattings) {
        TextColor lv = this.color;
        Boolean boolean_ = this.bold;
        Boolean boolean2 = this.italic;
        Boolean boolean3 = this.strikethrough;
        Boolean boolean4 = this.underlined;
        Boolean boolean5 = this.obfuscated;
        block8: for (Formatting lv2 : formattings) {
            switch (lv2) {
                case OBFUSCATED: {
                    boolean5 = true;
                    continue block8;
                }
                case BOLD: {
                    boolean_ = true;
                    continue block8;
                }
                case STRIKETHROUGH: {
                    boolean3 = true;
                    continue block8;
                }
                case UNDERLINE: {
                    boolean4 = true;
                    continue block8;
                }
                case ITALIC: {
                    boolean2 = true;
                    continue block8;
                }
                case RESET: {
                    return EMPTY;
                }
                default: {
                    lv = TextColor.fromFormatting(lv2);
                }
            }
        }
        return new Style(lv, boolean_, boolean2, boolean4, boolean3, boolean5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style withParent(Style parent) {
        if (this == EMPTY) {
            return parent;
        }
        if (parent == EMPTY) {
            return this;
        }
        return new Style(this.color != null ? this.color : parent.color, this.bold != null ? this.bold : parent.bold, this.italic != null ? this.italic : parent.italic, this.underlined != null ? this.underlined : parent.underlined, this.strikethrough != null ? this.strikethrough : parent.strikethrough, this.obfuscated != null ? this.obfuscated : parent.obfuscated, this.clickEvent != null ? this.clickEvent : parent.clickEvent, this.hoverEvent != null ? this.hoverEvent : parent.hoverEvent, this.insertion != null ? this.insertion : parent.insertion, this.font != null ? this.font : parent.font);
    }

    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder("{");
        class Writer {
            private boolean shouldAppendComma;

            Writer() {
            }

            private void appendComma() {
                if (this.shouldAppendComma) {
                    stringBuilder.append(',');
                }
                this.shouldAppendComma = true;
            }

            void append(String key, @Nullable Boolean value) {
                if (value != null) {
                    this.appendComma();
                    if (!value.booleanValue()) {
                        stringBuilder.append('!');
                    }
                    stringBuilder.append(key);
                }
            }

            void append(String key, @Nullable Object value) {
                if (value != null) {
                    this.appendComma();
                    stringBuilder.append(key);
                    stringBuilder.append('=');
                    stringBuilder.append(value);
                }
            }
        }
        Writer lv = new Writer();
        lv.append("color", this.color);
        lv.append("bold", this.bold);
        lv.append("italic", this.italic);
        lv.append("underlined", this.underlined);
        lv.append("strikethrough", this.strikethrough);
        lv.append("obfuscated", this.obfuscated);
        lv.append("clickEvent", this.clickEvent);
        lv.append("hoverEvent", this.hoverEvent);
        lv.append("insertion", this.insertion);
        lv.append("font", this.font);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Style) {
            Style lv = (Style)o;
            return this.isBold() == lv.isBold() && Objects.equals(this.getColor(), lv.getColor()) && this.isItalic() == lv.isItalic() && this.isObfuscated() == lv.isObfuscated() && this.isStrikethrough() == lv.isStrikethrough() && this.isUnderlined() == lv.isUnderlined() && Objects.equals(this.getClickEvent(), lv.getClickEvent()) && Objects.equals(this.getHoverEvent(), lv.getHoverEvent()) && Objects.equals(this.getInsertion(), lv.getInsertion()) && Objects.equals(this.getFont(), lv.getFont());
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion);
    }

    public static class Serializer
    implements JsonDeserializer<Style>,
    JsonSerializer<Style> {
        @Override
        @Nullable
        public Style deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (jsonObject == null) {
                    return null;
                }
                Boolean boolean_ = Serializer.parseNullableBoolean(jsonObject, "bold");
                Boolean boolean2 = Serializer.parseNullableBoolean(jsonObject, "italic");
                Boolean boolean3 = Serializer.parseNullableBoolean(jsonObject, "underlined");
                Boolean boolean4 = Serializer.parseNullableBoolean(jsonObject, "strikethrough");
                Boolean boolean5 = Serializer.parseNullableBoolean(jsonObject, "obfuscated");
                TextColor lv = Serializer.parseColor(jsonObject);
                String string = Serializer.parseInsertion(jsonObject);
                ClickEvent lv2 = Serializer.getClickEvent(jsonObject);
                HoverEvent lv3 = Serializer.getHoverEvent(jsonObject);
                Identifier lv4 = Serializer.getFont(jsonObject);
                return new Style(lv, boolean_, boolean2, boolean3, boolean4, boolean5, lv2, lv3, string, lv4);
            }
            return null;
        }

        @Nullable
        private static Identifier getFont(JsonObject root) {
            if (root.has("font")) {
                String string = JsonHelper.getString(root, "font");
                try {
                    return new Identifier(string);
                }
                catch (InvalidIdentifierException lv) {
                    throw new JsonSyntaxException("Invalid font name: " + string);
                }
            }
            return null;
        }

        @Nullable
        private static HoverEvent getHoverEvent(JsonObject root) {
            JsonObject jsonObject2;
            HoverEvent lv;
            if (root.has("hoverEvent") && (lv = HoverEvent.fromJson(jsonObject2 = JsonHelper.getObject(root, "hoverEvent"))) != null && lv.getAction().isParsable()) {
                return lv;
            }
            return null;
        }

        @Nullable
        private static ClickEvent getClickEvent(JsonObject root) {
            if (root.has("clickEvent")) {
                JsonObject jsonObject2 = JsonHelper.getObject(root, "clickEvent");
                String string = JsonHelper.getString(jsonObject2, "action", null);
                ClickEvent.Action lv = string == null ? null : ClickEvent.Action.byName(string);
                String string2 = JsonHelper.getString(jsonObject2, "value", null);
                if (lv != null && string2 != null && lv.isUserDefinable()) {
                    return new ClickEvent(lv, string2);
                }
            }
            return null;
        }

        @Nullable
        private static String parseInsertion(JsonObject root) {
            return JsonHelper.getString(root, "insertion", null);
        }

        @Nullable
        private static TextColor parseColor(JsonObject root) {
            if (root.has("color")) {
                String string = JsonHelper.getString(root, "color");
                return TextColor.parse(string);
            }
            return null;
        }

        @Nullable
        private static Boolean parseNullableBoolean(JsonObject root, String key) {
            if (root.has(key)) {
                return root.get(key).getAsBoolean();
            }
            return null;
        }

        @Override
        @Nullable
        public JsonElement serialize(Style arg, Type type, JsonSerializationContext jsonSerializationContext) {
            if (arg.isEmpty()) {
                return null;
            }
            JsonObject jsonObject = new JsonObject();
            if (arg.bold != null) {
                jsonObject.addProperty("bold", arg.bold);
            }
            if (arg.italic != null) {
                jsonObject.addProperty("italic", arg.italic);
            }
            if (arg.underlined != null) {
                jsonObject.addProperty("underlined", arg.underlined);
            }
            if (arg.strikethrough != null) {
                jsonObject.addProperty("strikethrough", arg.strikethrough);
            }
            if (arg.obfuscated != null) {
                jsonObject.addProperty("obfuscated", arg.obfuscated);
            }
            if (arg.color != null) {
                jsonObject.addProperty("color", arg.color.getName());
            }
            if (arg.insertion != null) {
                jsonObject.add("insertion", jsonSerializationContext.serialize(arg.insertion));
            }
            if (arg.clickEvent != null) {
                JsonObject jsonObject2 = new JsonObject();
                jsonObject2.addProperty("action", arg.clickEvent.getAction().getName());
                jsonObject2.addProperty("value", arg.clickEvent.getValue());
                jsonObject.add("clickEvent", jsonObject2);
            }
            if (arg.hoverEvent != null) {
                jsonObject.add("hoverEvent", arg.hoverEvent.toJson());
            }
            if (arg.font != null) {
                jsonObject.addProperty("font", arg.font.toString());
            }
            return jsonObject;
        }

        @Override
        @Nullable
        public /* synthetic */ JsonElement serialize(Object style, Type type, JsonSerializationContext context) {
            return this.serialize((Style)style, type, context);
        }

        @Override
        @Nullable
        public /* synthetic */ Object deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            return this.deserialize(json, type, context);
        }
    }
}

