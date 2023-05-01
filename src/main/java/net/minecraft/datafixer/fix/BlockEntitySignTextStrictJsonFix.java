/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.datafixer.fix;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.lang.reflect.Type;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;
import org.apache.commons.lang3.StringUtils;

public class BlockEntitySignTextStrictJsonFix
extends ChoiceFix {
    public static final Gson GSON = new GsonBuilder().registerTypeAdapter((Type)((Object)Text.class), new JsonDeserializer<Text>(){

        @Override
        public MutableText deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if (jsonElement.isJsonPrimitive()) {
                return Text.literal(jsonElement.getAsString());
            }
            if (jsonElement.isJsonArray()) {
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                MutableText lv = null;
                for (JsonElement jsonElement2 : jsonArray) {
                    MutableText lv2 = this.deserialize(jsonElement2, jsonElement2.getClass(), jsonDeserializationContext);
                    if (lv == null) {
                        lv = lv2;
                        continue;
                    }
                    lv.append(lv2);
                }
                return lv;
            }
            throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement functionJson, Type unused, JsonDeserializationContext context) throws JsonParseException {
            return this.deserialize(functionJson, unused, context);
        }
    }).create();

    public BlockEntitySignTextStrictJsonFix(Schema schema, boolean bl) {
        super(schema, bl, "BlockEntitySignTextStrictJsonFix", TypeReferences.BLOCK_ENTITY, "Sign");
    }

    private Dynamic<?> fix(Dynamic<?> dynamic, String lineName) {
        String string2 = dynamic.get(lineName).asString("");
        Text lv = null;
        if ("null".equals(string2) || StringUtils.isEmpty(string2)) {
            lv = ScreenTexts.EMPTY;
        } else if (string2.charAt(0) == '\"' && string2.charAt(string2.length() - 1) == '\"' || string2.charAt(0) == '{' && string2.charAt(string2.length() - 1) == '}') {
            try {
                lv = JsonHelper.deserializeNullable(GSON, string2, Text.class, true);
                if (lv == null) {
                    lv = ScreenTexts.EMPTY;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (lv == null) {
                try {
                    lv = Text.Serializer.fromJson(string2);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            if (lv == null) {
                try {
                    lv = Text.Serializer.fromLenientJson(string2);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            if (lv == null) {
                lv = Text.literal(string2);
            }
        } else {
            lv = Text.literal(string2);
        }
        return dynamic.set(lineName, dynamic.createString(Text.Serializer.toJson(lv)));
    }

    @Override
    protected Typed<?> transform(Typed<?> inputType) {
        return inputType.update(DSL.remainderFinder(), dynamic -> {
            dynamic = this.fix((Dynamic<?>)dynamic, "Text1");
            dynamic = this.fix((Dynamic<?>)dynamic, "Text2");
            dynamic = this.fix((Dynamic<?>)dynamic, "Text3");
            dynamic = this.fix((Dynamic<?>)dynamic, "Text4");
            return dynamic;
        });
    }
}

