/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.test;

import com.google.common.base.MoreObjects;
import java.util.Arrays;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.test.GameTestState;
import net.minecraft.test.NotEnoughSuccessesError;
import net.minecraft.test.PositionedException;
import net.minecraft.test.TestFailureLogger;
import net.minecraft.test.TestFunction;
import net.minecraft.test.TestListener;
import net.minecraft.test.TestManager;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.exception.ExceptionUtils;

class StructureTestListener
implements TestListener {
    private final GameTestState test;
    private final TestManager testManager;
    private final BlockPos pos;
    int attempt;
    int successes;

    public StructureTestListener(GameTestState test, TestManager testManager, BlockPos pos) {
        this.test = test;
        this.testManager = testManager;
        this.pos = pos;
        this.attempt = 0;
        this.successes = 0;
    }

    @Override
    public void onStarted(GameTestState test) {
        StructureTestListener.visualizeTest(this.test, Blocks.LIGHT_GRAY_STAINED_GLASS);
        ++this.attempt;
    }

    @Override
    public void onPassed(GameTestState test) {
        ++this.successes;
        if (!test.isFlaky()) {
            StructureTestListener.passTest(test, test.getTemplatePath() + " passed! (" + test.getElapsedMilliseconds() + "ms)");
            return;
        }
        if (this.successes >= test.getRequiredSuccesses()) {
            StructureTestListener.passTest(test, test + " passed " + this.successes + " times of " + this.attempt + " attempts.");
        } else {
            StructureTestListener.sendMessageToAllPlayers(this.test.getWorld(), Formatting.GREEN, "Flaky test " + this.test + " succeeded, attempt: " + this.attempt + " successes: " + this.successes);
            this.init();
        }
    }

    @Override
    public void onFailed(GameTestState test) {
        if (!test.isFlaky()) {
            StructureTestListener.failTest(test, test.getThrowable());
            return;
        }
        TestFunction lv = this.test.getTestFunction();
        String string = "Flaky test " + this.test + " failed, attempt: " + this.attempt + "/" + lv.getMaxAttempts();
        if (lv.getRequiredSuccesses() > 1) {
            string = string + ", successes: " + this.successes + " (" + lv.getRequiredSuccesses() + " required)";
        }
        StructureTestListener.sendMessageToAllPlayers(this.test.getWorld(), Formatting.YELLOW, string);
        if (test.getMaxAttempts() - this.attempt + this.successes >= test.getRequiredSuccesses()) {
            this.init();
        } else {
            StructureTestListener.failTest(test, new NotEnoughSuccessesError(this.attempt, this.successes, test));
        }
    }

    public static void passTest(GameTestState test, String output) {
        StructureTestListener.visualizeTest(test, Blocks.LIME_STAINED_GLASS);
        StructureTestListener.finishPassedTest(test, output);
    }

    private static void finishPassedTest(GameTestState test, String output) {
        StructureTestListener.sendMessageToAllPlayers(test.getWorld(), Formatting.GREEN, output);
        TestFailureLogger.passTest(test);
    }

    protected static void failTest(GameTestState test, Throwable output) {
        StructureTestListener.visualizeTest(test, test.isRequired() ? Blocks.RED_STAINED_GLASS : Blocks.ORANGE_STAINED_GLASS);
        StructureTestListener.createTestOutputLectern(test, Util.getInnermostMessage(output));
        StructureTestListener.finishFailedTest(test, output);
    }

    protected static void finishFailedTest(GameTestState test, Throwable output) {
        String string = output.getMessage() + (String)(output.getCause() == null ? "" : " cause: " + Util.getInnermostMessage(output.getCause()));
        String string2 = (test.isRequired() ? "" : "(optional) ") + test.getTemplatePath() + " failed! " + string;
        StructureTestListener.sendMessageToAllPlayers(test.getWorld(), test.isRequired() ? Formatting.RED : Formatting.YELLOW, string2);
        Throwable throwable2 = MoreObjects.firstNonNull(ExceptionUtils.getRootCause(output), output);
        if (throwable2 instanceof PositionedException) {
            PositionedException lv = (PositionedException)throwable2;
            StructureTestListener.addGameTestMarker(test.getWorld(), lv.getPos(), lv.getDebugMessage());
        }
        TestFailureLogger.failTest(test);
    }

    private void init() {
        this.test.clearArea();
        GameTestState lv = new GameTestState(this.test.getTestFunction(), this.test.getRotation(), this.test.getWorld());
        lv.startCountdown();
        this.testManager.start(lv);
        lv.addListener(this);
        lv.init(this.pos, 2);
    }

    protected static void visualizeTest(GameTestState test, Block block) {
        ServerWorld lv = test.getWorld();
        BlockPos lv2 = test.getPos();
        BlockPos lv3 = new BlockPos(-1, -1, -1);
        BlockPos lv4 = StructureTemplate.transformAround(lv2.add(lv3), BlockMirror.NONE, test.getRotation(), lv2);
        lv.setBlockState(lv4, Blocks.BEACON.getDefaultState().rotate(test.getRotation()));
        BlockPos lv5 = lv4.add(0, 1, 0);
        lv.setBlockState(lv5, block.getDefaultState());
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                BlockPos lv6 = lv4.add(i, -1, j);
                lv.setBlockState(lv6, Blocks.IRON_BLOCK.getDefaultState());
            }
        }
    }

    private static void createTestOutputLectern(GameTestState test, String output) {
        ServerWorld lv = test.getWorld();
        BlockPos lv2 = test.getPos();
        BlockPos lv3 = new BlockPos(-1, 1, -1);
        BlockPos lv4 = StructureTemplate.transformAround(lv2.add(lv3), BlockMirror.NONE, test.getRotation(), lv2);
        lv.setBlockState(lv4, Blocks.LECTERN.getDefaultState().rotate(test.getRotation()));
        BlockState lv5 = lv.getBlockState(lv4);
        ItemStack lv6 = StructureTestListener.createBookWithText(test.getTemplatePath(), test.isRequired(), output);
        LecternBlock.putBookIfAbsent(null, lv, lv4, lv5, lv6);
    }

    private static ItemStack createBookWithText(String text, boolean required, String output) {
        ItemStack lv = new ItemStack(Items.WRITABLE_BOOK);
        NbtList lv2 = new NbtList();
        StringBuffer stringBuffer = new StringBuffer();
        Arrays.stream(text.split("\\.")).forEach(line -> stringBuffer.append((String)line).append('\n'));
        if (!required) {
            stringBuffer.append("(optional)\n");
        }
        stringBuffer.append("-------------------\n");
        lv2.add(NbtString.of(stringBuffer + output));
        lv.setSubNbt("pages", lv2);
        return lv;
    }

    protected static void sendMessageToAllPlayers(ServerWorld world, Formatting formatting, String message) {
        world.getPlayers(player -> true).forEach(player -> player.sendMessage(Text.literal(message).formatted(formatting)));
    }

    private static void addGameTestMarker(ServerWorld world, BlockPos pos, String message) {
        DebugInfoSender.addGameTestMarker(world, pos, message, -2130771968, Integer.MAX_VALUE);
    }
}

