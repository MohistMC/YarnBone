/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network.message;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Decoration;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public record MessageType(Decoration chat, Decoration narration) {
    public static final Codec<MessageType> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Decoration.CODEC.fieldOf("chat")).forGetter(MessageType::chat), ((MapCodec)Decoration.CODEC.fieldOf("narration")).forGetter(MessageType::narration)).apply((Applicative<MessageType, ?>)instance, MessageType::new));
    public static final Decoration CHAT_TEXT_DECORATION = Decoration.ofChat("chat.type.text");
    public static final RegistryKey<MessageType> CHAT = MessageType.register("chat");
    public static final RegistryKey<MessageType> SAY_COMMAND = MessageType.register("say_command");
    public static final RegistryKey<MessageType> MSG_COMMAND_INCOMING = MessageType.register("msg_command_incoming");
    public static final RegistryKey<MessageType> MSG_COMMAND_OUTGOING = MessageType.register("msg_command_outgoing");
    public static final RegistryKey<MessageType> TEAM_MSG_COMMAND_INCOMING = MessageType.register("team_msg_command_incoming");
    public static final RegistryKey<MessageType> TEAM_MSG_COMMAND_OUTGOING = MessageType.register("team_msg_command_outgoing");
    public static final RegistryKey<MessageType> EMOTE_COMMAND = MessageType.register("emote_command");

    private static RegistryKey<MessageType> register(String id) {
        return RegistryKey.of(RegistryKeys.MESSAGE_TYPE, new Identifier(id));
    }

    public static void bootstrap(Registerable<MessageType> messageTypeRegisterable) {
        messageTypeRegisterable.register(CHAT, new MessageType(CHAT_TEXT_DECORATION, Decoration.ofChat("chat.type.text.narrate")));
        messageTypeRegisterable.register(SAY_COMMAND, new MessageType(Decoration.ofChat("chat.type.announcement"), Decoration.ofChat("chat.type.text.narrate")));
        messageTypeRegisterable.register(MSG_COMMAND_INCOMING, new MessageType(Decoration.ofIncomingMessage("commands.message.display.incoming"), Decoration.ofChat("chat.type.text.narrate")));
        messageTypeRegisterable.register(MSG_COMMAND_OUTGOING, new MessageType(Decoration.ofOutgoingMessage("commands.message.display.outgoing"), Decoration.ofChat("chat.type.text.narrate")));
        messageTypeRegisterable.register(TEAM_MSG_COMMAND_INCOMING, new MessageType(Decoration.ofTeamMessage("chat.type.team.text"), Decoration.ofChat("chat.type.text.narrate")));
        messageTypeRegisterable.register(TEAM_MSG_COMMAND_OUTGOING, new MessageType(Decoration.ofTeamMessage("chat.type.team.sent"), Decoration.ofChat("chat.type.text.narrate")));
        messageTypeRegisterable.register(EMOTE_COMMAND, new MessageType(Decoration.ofChat("chat.type.emote"), Decoration.ofChat("chat.type.emote")));
    }

    public static Parameters params(RegistryKey<MessageType> typeKey, Entity entity) {
        return MessageType.params(typeKey, entity.world.getRegistryManager(), entity.getDisplayName());
    }

    public static Parameters params(RegistryKey<MessageType> typeKey, ServerCommandSource source) {
        return MessageType.params(typeKey, source.getRegistryManager(), source.getDisplayName());
    }

    public static Parameters params(RegistryKey<MessageType> typeKey, DynamicRegistryManager registryManager, Text name) {
        Registry<MessageType> lv = registryManager.get(RegistryKeys.MESSAGE_TYPE);
        return lv.getOrThrow(typeKey).params(name);
    }

    public Parameters params(Text name) {
        return new Parameters(this, name);
    }

    public record Parameters(MessageType type, Text name, @Nullable Text targetName) {
        Parameters(MessageType type, Text name) {
            this(type, name, null);
        }

        public Text applyChatDecoration(Text content) {
            return this.type.chat().apply(content, this);
        }

        public Text applyNarrationDecoration(Text content) {
            return this.type.narration().apply(content, this);
        }

        public Parameters withTargetName(Text targetName) {
            return new Parameters(this.type, this.name, targetName);
        }

        public Serialized toSerialized(DynamicRegistryManager registryManager) {
            Registry<MessageType> lv = registryManager.get(RegistryKeys.MESSAGE_TYPE);
            return new Serialized(lv.getRawId(this.type), this.name, this.targetName);
        }
    }

    public record Serialized(int typeId, Text name, @Nullable Text targetName) {
        public Serialized(PacketByteBuf buf) {
            this(buf.readVarInt(), buf.readText(), (Text)buf.readNullable(PacketByteBuf::readText));
        }

        public void write(PacketByteBuf buf) {
            buf.writeVarInt(this.typeId);
            buf.writeText(this.name);
            buf.writeNullable(this.targetName, PacketByteBuf::writeText);
        }

        public Optional<Parameters> toParameters(DynamicRegistryManager registryManager) {
            Registry<MessageType> lv = registryManager.get(RegistryKeys.MESSAGE_TYPE);
            MessageType lv2 = (MessageType)lv.get(this.typeId);
            return Optional.ofNullable(lv2).map(type -> new Parameters((MessageType)type, this.name, this.targetName));
        }
    }
}

