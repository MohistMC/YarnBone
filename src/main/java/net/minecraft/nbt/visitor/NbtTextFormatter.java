/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.nbt.visitor;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.bytes.ByteCollection;
import it.unimi.dsi.fastutil.bytes.ByteOpenHashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Pattern;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtEnd;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.visitor.NbtElementVisitor;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;

public class NbtTextFormatter
implements NbtElementVisitor {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int field_33271 = 8;
    private static final ByteCollection SINGLE_LINE_ELEMENT_TYPES = new ByteOpenHashSet(Arrays.asList((byte)1, (byte)2, (byte)3, (byte)4, (byte)5, (byte)6));
    private static final Formatting NAME_COLOR = Formatting.AQUA;
    private static final Formatting STRING_COLOR = Formatting.GREEN;
    private static final Formatting NUMBER_COLOR = Formatting.GOLD;
    private static final Formatting TYPE_SUFFIX_COLOR = Formatting.RED;
    private static final Pattern SIMPLE_NAME = Pattern.compile("[A-Za-z0-9._+-]+");
    private static final String KEY_VALUE_SEPARATOR = String.valueOf(':');
    private static final String ENTRY_SEPARATOR = String.valueOf(',');
    private static final String SQUARE_OPEN_BRACKET = "[";
    private static final String SQUARE_CLOSE_BRACKET = "]";
    private static final String SEMICOLON = ";";
    private static final String SPACE = " ";
    private static final String CURLY_OPEN_BRACKET = "{";
    private static final String CURLY_CLOSE_BRACKET = "}";
    private static final String NEW_LINE = "\n";
    private final String prefix;
    private final int indentationLevel;
    private Text result = ScreenTexts.EMPTY;

    public NbtTextFormatter(String prefix, int indentationLevel) {
        this.prefix = prefix;
        this.indentationLevel = indentationLevel;
    }

    public Text apply(NbtElement element) {
        element.accept(this);
        return this.result;
    }

    @Override
    public void visitString(NbtString element) {
        String string = NbtString.escape(element.asString());
        String string2 = string.substring(0, 1);
        MutableText lv = Text.literal(string.substring(1, string.length() - 1)).formatted(STRING_COLOR);
        this.result = Text.literal(string2).append(lv).append(string2);
    }

    @Override
    public void visitByte(NbtByte element) {
        MutableText lv = Text.literal("b").formatted(TYPE_SUFFIX_COLOR);
        this.result = Text.literal(String.valueOf(element.numberValue())).append(lv).formatted(NUMBER_COLOR);
    }

    @Override
    public void visitShort(NbtShort element) {
        MutableText lv = Text.literal("s").formatted(TYPE_SUFFIX_COLOR);
        this.result = Text.literal(String.valueOf(element.numberValue())).append(lv).formatted(NUMBER_COLOR);
    }

    @Override
    public void visitInt(NbtInt element) {
        this.result = Text.literal(String.valueOf(element.numberValue())).formatted(NUMBER_COLOR);
    }

    @Override
    public void visitLong(NbtLong element) {
        MutableText lv = Text.literal("L").formatted(TYPE_SUFFIX_COLOR);
        this.result = Text.literal(String.valueOf(element.numberValue())).append(lv).formatted(NUMBER_COLOR);
    }

    @Override
    public void visitFloat(NbtFloat element) {
        MutableText lv = Text.literal("f").formatted(TYPE_SUFFIX_COLOR);
        this.result = Text.literal(String.valueOf(element.floatValue())).append(lv).formatted(NUMBER_COLOR);
    }

    @Override
    public void visitDouble(NbtDouble element) {
        MutableText lv = Text.literal("d").formatted(TYPE_SUFFIX_COLOR);
        this.result = Text.literal(String.valueOf(element.doubleValue())).append(lv).formatted(NUMBER_COLOR);
    }

    @Override
    public void visitByteArray(NbtByteArray element) {
        MutableText lv = Text.literal("B").formatted(TYPE_SUFFIX_COLOR);
        MutableText lv2 = Text.literal(SQUARE_OPEN_BRACKET).append(lv).append(SEMICOLON);
        byte[] bs = element.getByteArray();
        for (int i = 0; i < bs.length; ++i) {
            MutableText lv3 = Text.literal(String.valueOf(bs[i])).formatted(NUMBER_COLOR);
            lv2.append(SPACE).append(lv3).append(lv);
            if (i == bs.length - 1) continue;
            lv2.append(ENTRY_SEPARATOR);
        }
        lv2.append(SQUARE_CLOSE_BRACKET);
        this.result = lv2;
    }

    @Override
    public void visitIntArray(NbtIntArray element) {
        MutableText lv = Text.literal("I").formatted(TYPE_SUFFIX_COLOR);
        MutableText lv2 = Text.literal(SQUARE_OPEN_BRACKET).append(lv).append(SEMICOLON);
        int[] is = element.getIntArray();
        for (int i = 0; i < is.length; ++i) {
            lv2.append(SPACE).append(Text.literal(String.valueOf(is[i])).formatted(NUMBER_COLOR));
            if (i == is.length - 1) continue;
            lv2.append(ENTRY_SEPARATOR);
        }
        lv2.append(SQUARE_CLOSE_BRACKET);
        this.result = lv2;
    }

    @Override
    public void visitLongArray(NbtLongArray element) {
        MutableText lv = Text.literal("L").formatted(TYPE_SUFFIX_COLOR);
        MutableText lv2 = Text.literal(SQUARE_OPEN_BRACKET).append(lv).append(SEMICOLON);
        long[] ls = element.getLongArray();
        for (int i = 0; i < ls.length; ++i) {
            MutableText lv3 = Text.literal(String.valueOf(ls[i])).formatted(NUMBER_COLOR);
            lv2.append(SPACE).append(lv3).append(lv);
            if (i == ls.length - 1) continue;
            lv2.append(ENTRY_SEPARATOR);
        }
        lv2.append(SQUARE_CLOSE_BRACKET);
        this.result = lv2;
    }

    @Override
    public void visitList(NbtList element) {
        if (element.isEmpty()) {
            this.result = Text.literal("[]");
            return;
        }
        if (SINGLE_LINE_ELEMENT_TYPES.contains(element.getHeldType()) && element.size() <= 8) {
            String string = ENTRY_SEPARATOR + SPACE;
            MutableText lv = Text.literal(SQUARE_OPEN_BRACKET);
            for (int i = 0; i < element.size(); ++i) {
                if (i != 0) {
                    lv.append(string);
                }
                lv.append(new NbtTextFormatter(this.prefix, this.indentationLevel).apply(element.get(i)));
            }
            lv.append(SQUARE_CLOSE_BRACKET);
            this.result = lv;
            return;
        }
        MutableText lv2 = Text.literal(SQUARE_OPEN_BRACKET);
        if (!this.prefix.isEmpty()) {
            lv2.append(NEW_LINE);
        }
        for (int j = 0; j < element.size(); ++j) {
            MutableText lv3 = Text.literal(Strings.repeat(this.prefix, this.indentationLevel + 1));
            lv3.append(new NbtTextFormatter(this.prefix, this.indentationLevel + 1).apply(element.get(j)));
            if (j != element.size() - 1) {
                lv3.append(ENTRY_SEPARATOR).append(this.prefix.isEmpty() ? SPACE : NEW_LINE);
            }
            lv2.append(lv3);
        }
        if (!this.prefix.isEmpty()) {
            lv2.append(NEW_LINE).append(Strings.repeat(this.prefix, this.indentationLevel));
        }
        lv2.append(SQUARE_CLOSE_BRACKET);
        this.result = lv2;
    }

    @Override
    public void visitCompound(NbtCompound compound) {
        if (compound.isEmpty()) {
            this.result = Text.literal("{}");
            return;
        }
        MutableText lv = Text.literal(CURLY_OPEN_BRACKET);
        Collection<String> collection = compound.getKeys();
        if (LOGGER.isDebugEnabled()) {
            ArrayList<String> list = Lists.newArrayList(compound.getKeys());
            Collections.sort(list);
            collection = list;
        }
        if (!this.prefix.isEmpty()) {
            lv.append(NEW_LINE);
        }
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()) {
            String string = (String)iterator.next();
            MutableText lv2 = Text.literal(Strings.repeat(this.prefix, this.indentationLevel + 1)).append(NbtTextFormatter.escapeName(string)).append(KEY_VALUE_SEPARATOR).append(SPACE).append(new NbtTextFormatter(this.prefix, this.indentationLevel + 1).apply(compound.get(string)));
            if (iterator.hasNext()) {
                lv2.append(ENTRY_SEPARATOR).append(this.prefix.isEmpty() ? SPACE : NEW_LINE);
            }
            lv.append(lv2);
        }
        if (!this.prefix.isEmpty()) {
            lv.append(NEW_LINE).append(Strings.repeat(this.prefix, this.indentationLevel));
        }
        lv.append(CURLY_CLOSE_BRACKET);
        this.result = lv;
    }

    protected static Text escapeName(String name) {
        if (SIMPLE_NAME.matcher(name).matches()) {
            return Text.literal(name).formatted(NAME_COLOR);
        }
        String string2 = NbtString.escape(name);
        String string3 = string2.substring(0, 1);
        MutableText lv = Text.literal(string2.substring(1, string2.length() - 1)).formatted(NAME_COLOR);
        return Text.literal(string3).append(lv).append(string3);
    }

    @Override
    public void visitEnd(NbtEnd element) {
        this.result = ScreenTexts.EMPTY;
    }
}

