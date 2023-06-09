/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class SignBlockEntity
extends BlockEntity {
    public static final int TEXT_COUNT = 4;
    private static final int MAX_TEXT_WIDTH = 90;
    private static final int TEXT_LINE_HEIGHT = 10;
    private static final String[] TEXT_KEYS = new String[]{"Text1", "Text2", "Text3", "Text4"};
    private static final String[] FILTERED_TEXT_KEYS = new String[]{"FilteredText1", "FilteredText2", "FilteredText3", "FilteredText4"};
    private final Text[] texts = new Text[]{ScreenTexts.EMPTY, ScreenTexts.EMPTY, ScreenTexts.EMPTY, ScreenTexts.EMPTY};
    private final Text[] filteredTexts = new Text[]{ScreenTexts.EMPTY, ScreenTexts.EMPTY, ScreenTexts.EMPTY, ScreenTexts.EMPTY};
    private boolean editable = true;
    @Nullable
    private UUID editor;
    @Nullable
    private OrderedText[] textsBeingEdited;
    private boolean filterText;
    private DyeColor textColor = DyeColor.BLACK;
    private boolean glowingText;

    public SignBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.SIGN, pos, state);
    }

    public SignBlockEntity(BlockEntityType arg, BlockPos arg2, BlockState arg3) {
        super(arg, arg2, arg3);
    }

    public int getTextLineHeight() {
        return 10;
    }

    public int getMaxTextWidth() {
        return 90;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        for (int i = 0; i < 4; ++i) {
            Text lv = this.texts[i];
            String string = Text.Serializer.toJson(lv);
            nbt.putString(TEXT_KEYS[i], string);
            Text lv2 = this.filteredTexts[i];
            if (lv2.equals(lv)) continue;
            nbt.putString(FILTERED_TEXT_KEYS[i], Text.Serializer.toJson(lv2));
        }
        nbt.putString("Color", this.textColor.getName());
        nbt.putBoolean("GlowingText", this.glowingText);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.editable = false;
        super.readNbt(nbt);
        this.textColor = DyeColor.byName(nbt.getString("Color"), DyeColor.BLACK);
        for (int i = 0; i < 4; ++i) {
            Text lv;
            String string = nbt.getString(TEXT_KEYS[i]);
            this.texts[i] = lv = this.parseTextFromJson(string);
            String string2 = FILTERED_TEXT_KEYS[i];
            this.filteredTexts[i] = nbt.contains(string2, NbtElement.STRING_TYPE) ? this.parseTextFromJson(nbt.getString(string2)) : lv;
        }
        this.textsBeingEdited = null;
        this.glowingText = nbt.getBoolean("GlowingText");
    }

    private Text parseTextFromJson(String json) {
        Text lv = this.unparsedTextFromJson(json);
        if (this.world instanceof ServerWorld) {
            try {
                return Texts.parse(this.getCommandSource(null), lv, null, 0);
            }
            catch (CommandSyntaxException commandSyntaxException) {
                // empty catch block
            }
        }
        return lv;
    }

    private Text unparsedTextFromJson(String json) {
        try {
            MutableText lv = Text.Serializer.fromJson(json);
            if (lv != null) {
                return lv;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return ScreenTexts.EMPTY;
    }

    public Text getTextOnRow(int row, boolean filtered) {
        return this.getTexts(filtered)[row];
    }

    public void setTextOnRow(int row, Text text) {
        this.setTextOnRow(row, text, text);
    }

    public void setTextOnRow(int row, Text text, Text filteredText) {
        this.texts[row] = text;
        this.filteredTexts[row] = filteredText;
        this.textsBeingEdited = null;
    }

    public OrderedText[] updateSign(boolean filterText, Function<Text, OrderedText> textOrderingFunction) {
        if (this.textsBeingEdited == null || this.filterText != filterText) {
            this.filterText = filterText;
            this.textsBeingEdited = new OrderedText[4];
            for (int i = 0; i < 4; ++i) {
                this.textsBeingEdited[i] = textOrderingFunction.apply(this.getTextOnRow(i, filterText));
            }
        }
        return this.textsBeingEdited;
    }

    private Text[] getTexts(boolean filtered) {
        return filtered ? this.filteredTexts : this.texts;
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return this.createNbt();
    }

    @Override
    public boolean copyItemDataRequiresOperator() {
        return true;
    }

    public boolean isEditable() {
        return this.editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        if (!editable) {
            this.editor = null;
        }
    }

    public void setEditor(UUID editor) {
        this.editor = editor;
    }

    @Nullable
    public UUID getEditor() {
        return this.editor;
    }

    public boolean shouldRunCommand(PlayerEntity player) {
        for (Text lv : this.getTexts(player.shouldFilterText())) {
            Style lv2 = lv.getStyle();
            ClickEvent lv3 = lv2.getClickEvent();
            if (lv3 == null || lv3.getAction() != ClickEvent.Action.RUN_COMMAND) continue;
            return true;
        }
        return false;
    }

    public boolean onActivate(ServerPlayerEntity player) {
        for (Text lv : this.getTexts(player.shouldFilterText())) {
            Style lv2 = lv.getStyle();
            ClickEvent lv3 = lv2.getClickEvent();
            if (lv3 == null || lv3.getAction() != ClickEvent.Action.RUN_COMMAND) continue;
            player.getServer().getCommandManager().executeWithPrefix(this.getCommandSource(player), lv3.getValue());
        }
        return true;
    }

    public ServerCommandSource getCommandSource(@Nullable ServerPlayerEntity player) {
        String string = player == null ? "Sign" : player.getName().getString();
        Text lv = player == null ? Text.literal("Sign") : player.getDisplayName();
        return new ServerCommandSource(CommandOutput.DUMMY, Vec3d.ofCenter(this.pos), Vec2f.ZERO, (ServerWorld)this.world, 2, string, lv, this.world.getServer(), player);
    }

    public DyeColor getTextColor() {
        return this.textColor;
    }

    public boolean setTextColor(DyeColor value) {
        if (value != this.getTextColor()) {
            this.textColor = value;
            this.updateListeners();
            return true;
        }
        return false;
    }

    public boolean isGlowingText() {
        return this.glowingText;
    }

    public boolean setGlowingText(boolean glowingText) {
        if (this.glowingText != glowingText) {
            this.glowingText = glowingText;
            this.updateListeners();
            return true;
        }
        return false;
    }

    private void updateListeners() {
        this.markDirty();
        this.world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
    }

    public /* synthetic */ Packet toUpdatePacket() {
        return this.toUpdatePacket();
    }
}

