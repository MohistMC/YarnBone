/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.BlockEntitySignTextStrictJsonFix;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;
import org.apache.commons.lang3.StringUtils;

public class ItemWrittenBookPagesStrictJsonFix
extends DataFix {
    public ItemWrittenBookPagesStrictJsonFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    public Dynamic<?> fixBookPages(Dynamic<?> dynamic) {
        return dynamic.update("pages", dynamic2 -> DataFixUtils.orElse(dynamic2.asStreamOpt().map(stream -> stream.map(dynamic -> {
            if (!dynamic.asString().result().isPresent()) {
                return dynamic;
            }
            String string = dynamic.asString("");
            Text lv = null;
            if ("null".equals(string) || StringUtils.isEmpty(string)) {
                lv = ScreenTexts.EMPTY;
            } else if (string.charAt(0) == '\"' && string.charAt(string.length() - 1) == '\"' || string.charAt(0) == '{' && string.charAt(string.length() - 1) == '}') {
                try {
                    lv = JsonHelper.deserializeNullable(BlockEntitySignTextStrictJsonFix.GSON, string, Text.class, true);
                    if (lv == null) {
                        lv = ScreenTexts.EMPTY;
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
                if (lv == null) {
                    try {
                        lv = Text.Serializer.fromJson(string);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                if (lv == null) {
                    try {
                        lv = Text.Serializer.fromLenientJson(string);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                if (lv == null) {
                    lv = Text.literal(string);
                }
            } else {
                lv = Text.literal(string);
            }
            return dynamic.createString(Text.Serializer.toJson(lv));
        })).map(dynamic::createList).result(), dynamic.emptyList()));
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.ITEM_STACK);
        OpticFinder<?> opticFinder = type.findField("tag");
        return this.fixTypeEverywhereTyped("ItemWrittenBookPagesStrictJsonFix", type, typed2 -> typed2.updateTyped(opticFinder, typed -> typed.update(DSL.remainderFinder(), this::fixBookPages)));
    }
}

