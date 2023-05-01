/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.text;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslationException;
import net.minecraft.util.Language;
import org.jetbrains.annotations.Nullable;

public class TranslatableTextContent
implements TextContent {
    public static final Object[] EMPTY_ARGUMENTS = new Object[0];
    private static final StringVisitable LITERAL_PERCENT_SIGN = StringVisitable.plain("%");
    private static final StringVisitable NULL_ARGUMENT = StringVisitable.plain("null");
    private final String key;
    @Nullable
    private final String fallback;
    private final Object[] args;
    @Nullable
    private Language languageCache;
    private List<StringVisitable> translations = ImmutableList.of();
    private static final Pattern ARG_FORMAT = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

    public TranslatableTextContent(String key, @Nullable String fallback, Object[] args) {
        this.key = key;
        this.fallback = fallback;
        this.args = args;
    }

    private void updateTranslations() {
        Language lv = Language.getInstance();
        if (lv == this.languageCache) {
            return;
        }
        this.languageCache = lv;
        String string = this.fallback != null ? lv.get(this.key, this.fallback) : lv.get(this.key);
        try {
            ImmutableList.Builder builder = ImmutableList.builder();
            this.forEachPart(string, builder::add);
            this.translations = builder.build();
        }
        catch (TranslationException lv2) {
            this.translations = ImmutableList.of(StringVisitable.plain(string));
        }
    }

    private void forEachPart(String translation, Consumer<StringVisitable> partsConsumer) {
        Matcher matcher = ARG_FORMAT.matcher(translation);
        try {
            int i = 0;
            int j = 0;
            while (matcher.find(j)) {
                String string2;
                int k = matcher.start();
                int l = matcher.end();
                if (k > j) {
                    string2 = translation.substring(j, k);
                    if (string2.indexOf(37) != -1) {
                        throw new IllegalArgumentException();
                    }
                    partsConsumer.accept(StringVisitable.plain(string2));
                }
                string2 = matcher.group(2);
                String string3 = translation.substring(k, l);
                if ("%".equals(string2) && "%%".equals(string3)) {
                    partsConsumer.accept(LITERAL_PERCENT_SIGN);
                } else if ("s".equals(string2)) {
                    String string4 = matcher.group(1);
                    int m = string4 != null ? Integer.parseInt(string4) - 1 : i++;
                    partsConsumer.accept(this.getArg(m));
                } else {
                    throw new TranslationException(this, "Unsupported format: '" + string3 + "'");
                }
                j = l;
            }
            if (j < translation.length()) {
                String string5 = translation.substring(j);
                if (string5.indexOf(37) != -1) {
                    throw new IllegalArgumentException();
                }
                partsConsumer.accept(StringVisitable.plain(string5));
            }
        }
        catch (IllegalArgumentException illegalArgumentException) {
            throw new TranslationException(this, (Throwable)illegalArgumentException);
        }
    }

    private StringVisitable getArg(int index) {
        if (index < 0 || index >= this.args.length) {
            throw new TranslationException(this, index);
        }
        Object object = this.args[index];
        if (object instanceof Text) {
            return (Text)object;
        }
        return object == null ? NULL_ARGUMENT : StringVisitable.plain(object.toString());
    }

    @Override
    public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> visitor, Style style) {
        this.updateTranslations();
        for (StringVisitable lv : this.translations) {
            Optional<T> optional = lv.visit(visitor, style);
            if (!optional.isPresent()) continue;
            return optional;
        }
        return Optional.empty();
    }

    @Override
    public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
        this.updateTranslations();
        for (StringVisitable lv : this.translations) {
            Optional<T> optional = lv.visit(visitor);
            if (!optional.isPresent()) continue;
            return optional;
        }
        return Optional.empty();
    }

    @Override
    public MutableText parse(@Nullable ServerCommandSource source, @Nullable Entity sender, int depth) throws CommandSyntaxException {
        Object[] objects = new Object[this.args.length];
        for (int j = 0; j < objects.length; ++j) {
            Object object = this.args[j];
            objects[j] = object instanceof Text ? Texts.parse(source, (Text)object, sender, depth) : object;
        }
        return MutableText.of(new TranslatableTextContent(this.key, this.fallback, objects));
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TranslatableTextContent)) return false;
        TranslatableTextContent lv = (TranslatableTextContent)o;
        if (!Objects.equals(this.key, lv.key)) return false;
        if (!Objects.equals(this.fallback, lv.fallback)) return false;
        if (!Arrays.equals(this.args, lv.args)) return false;
        return true;
    }

    public int hashCode() {
        int i = Objects.hashCode(this.key);
        i = 31 * i + Objects.hashCode(this.fallback);
        i = 31 * i + Arrays.hashCode(this.args);
        return i;
    }

    public String toString() {
        return "translation{key='" + this.key + "'" + (String)(this.fallback != null ? ", fallback='" + this.fallback + "'" : "") + ", args=" + Arrays.toString(this.args) + "}";
    }

    public String getKey() {
        return this.key;
    }

    @Nullable
    public String getFallback() {
        return this.fallback;
    }

    public Object[] getArgs() {
        return this.args;
    }
}

