/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTestBatch;
import net.minecraft.test.GameTestState;
import net.minecraft.test.StructureTestListener;
import net.minecraft.test.StructureTestUtil;
import net.minecraft.test.TestFunction;
import net.minecraft.test.TestFunctions;
import net.minecraft.test.TestManager;
import net.minecraft.test.TestRunner;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.mutable.MutableInt;

public class TestUtil {
    private static final int MAX_BATCH_SIZE = 100;
    public static final int field_33148 = 2;
    public static final int field_33149 = 5;
    public static final int field_33150 = 6;
    public static final int field_33151 = 8;

    public static void startTest(GameTestState test, BlockPos pos, TestManager testManager) {
        test.startCountdown();
        testManager.start(test);
        test.addListener(new StructureTestListener(test, testManager, pos));
        test.init(pos, 2);
    }

    public static Collection<GameTestState> runTestBatches(Collection<GameTestBatch> batches, BlockPos pos, BlockRotation rotation, ServerWorld world, TestManager testManager, int sizeZ) {
        TestRunner lv = new TestRunner(batches, pos, rotation, world, testManager, sizeZ);
        lv.run();
        return lv.getTests();
    }

    public static Collection<GameTestState> runTestFunctions(Collection<TestFunction> testFunctions, BlockPos pos, BlockRotation rotation, ServerWorld world, TestManager testManager, int sizeZ) {
        return TestUtil.runTestBatches(TestUtil.createBatches(testFunctions), pos, rotation, world, testManager, sizeZ);
    }

    public static Collection<GameTestBatch> createBatches(Collection<TestFunction> testFunctions) {
        Map<String, List<TestFunction>> map = testFunctions.stream().collect(Collectors.groupingBy(TestFunction::getBatchId));
        return map.entrySet().stream().flatMap(entry -> {
            String string = (String)entry.getKey();
            Consumer<ServerWorld> consumer = TestFunctions.getBeforeBatchConsumer(string);
            Consumer<ServerWorld> consumer2 = TestFunctions.getAfterBatchConsumer(string);
            MutableInt mutableInt = new MutableInt();
            Collection collection = (Collection)entry.getValue();
            return Streams.stream(Iterables.partition(collection, 100)).map(testFunctions -> new GameTestBatch(string + ":" + mutableInt.incrementAndGet(), ImmutableList.copyOf(testFunctions), consumer, consumer2));
        }).collect(ImmutableList.toImmutableList());
    }

    public static void clearTests(ServerWorld world, BlockPos pos2, TestManager testManager, int radius) {
        testManager.clear();
        BlockPos lv = pos2.add(-radius, 0, -radius);
        BlockPos lv2 = pos2.add(radius, 0, radius);
        BlockPos.stream(lv, lv2).filter(pos -> world.getBlockState((BlockPos)pos).isOf(Blocks.STRUCTURE_BLOCK)).forEach(pos -> {
            StructureBlockBlockEntity lv = (StructureBlockBlockEntity)world.getBlockEntity((BlockPos)pos);
            BlockPos lv2 = lv.getPos();
            BlockBox lv3 = StructureTestUtil.getStructureBlockBox(lv);
            StructureTestUtil.clearArea(lv3, lv2.getY(), world);
        });
    }

    public static void clearDebugMarkers(ServerWorld world) {
        DebugInfoSender.clearGameTestMarkers(world);
    }
}

