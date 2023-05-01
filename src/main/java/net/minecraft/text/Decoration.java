/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.text;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.message.MessageType;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Nullable;

public record Decoration(String translationKey, List<Parameter> parameters, Style style) {
    public static final Codec<Decoration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("translation_key")).forGetter(Decoration::translationKey), ((MapCodec)Parameter.CODEC.listOf().fieldOf("parameters")).forGetter(Decoration::parameters), Style.CODEC.optionalFieldOf("style", Style.EMPTY).forGetter(Decoration::style)).apply((Applicative<Decoration, ?>)instance, Decoration::new));

    public static Decoration ofChat(String translationKey) {
        return new Decoration(translationKey, List.of(Parameter.SENDER, Parameter.CONTENT), Style.EMPTY);
    }

    public static Decoration ofIncomingMessage(String translationKey) {
        Style lv = Style.EMPTY.withColor(Formatting.GRAY).withItalic(true);
        return new Decoration(translationKey, List.of(Parameter.SENDER, Parameter.CONTENT), lv);
    }

    public static Decoration ofOutgoingMessage(String translationKey) {
        Style lv = Style.EMPTY.withColor(Formatting.GRAY).withItalic(true);
        return new Decoration(translationKey, List.of(Parameter.TARGET, Parameter.CONTENT), lv);
    }

    public static Decoration ofTeamMessage(String translationKey) {
        return new Decoration(translationKey, List.of(Parameter.TARGET, Parameter.SENDER, Parameter.CONTENT), Style.EMPTY);
    }

    public Text apply(Text content, MessageType.Parameters params) {
        Object[] objects = this.collectArguments(content, params);
        return Text.translatable(this.translationKey, objects).fillStyle(this.style);
    }

    private Text[] collectArguments(Text content, MessageType.Parameters params) {
        Text[] lvs = new Text[this.parameters.size()];
        for (int i = 0; i < lvs.length; ++i) {
            Parameter lv = this.parameters.get(i);
            lvs[i] = lv.apply(content, params);
        }
        return lvs;
    }

    public static enum Parameter implements StringIdentifiable
    {
        SENDER("sender", (content, params) -> params.name()),
        TARGET("target", (content, params) -> params.targetName()),
        CONTENT("content", (content, params) -> content);

        public static final Codec<Parameter> CODEC;
        private final String name;
        private final Selector selector;

        private Parameter(String name, Selector selector) {
            this.name = name;
            this.selector = selector;
        }

        public Text apply(Text content, MessageType.Parameters params) {
            Text lv = this.selector.select(content, params);
            return Objects.requireNonNullElse(lv, ScreenTexts.EMPTY);
        }

        @Override
        public String asString() {
            return this.name;
        }

        static {
            CODEC = StringIdentifiable.createCodec(Parameter::values);
        }

        public static interface Selector {
            @Nullable
            public Text select(Text var1, MessageType.Parameters var2);
        }
    }
}

