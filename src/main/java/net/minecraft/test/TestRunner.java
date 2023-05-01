/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTestBatch;
import net.minecraft.test.GameTestState;
import net.minecraft.test.StructureTestUtil;
import net.minecraft.test.TestFunction;
import net.minecraft.test.TestListener;
import net.minecraft.test.TestManager;
import net.minecraft.test.TestSet;
import net.minecraft.test.TestUtil;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.slf4j.Logger;

public class TestRunner {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final BlockPos pos;
    final ServerWorld world;
    private final TestManager testManager;
    private final int sizeZ;
    private final List<GameTestState> tests;
    private final List<Pair<GameTestBatch, Collection<GameTestState>>> batches;
    private final BlockPos.Mutable reusablePos;

    public TestRunner(Collection<GameTestBatch> batches, BlockPos pos, BlockRotation rotation, ServerWorld world, TestManager testManager, int sizeZ) {
        this.reusablePos = pos.mutableCopy();
        this.pos = pos;
        this.world = world;
        this.testManager = testManager;
        this.sizeZ = sizeZ;
        this.batches = batches.stream().map(batch -> {
            Collection collection = batch.getTestFunctions().stream().map(testFunction -> new GameTestState((TestFunction)testFunction, rotation, world)).collect(ImmutableList.toImmutableList());
            return Pair.of(batch, collection);
        }).collect(ImmutableList.toImmutableList());
        this.tests = this.batches.stream().flatMap(batch -> ((Collection)batch.getSecond()).stream()).collect(ImmutableList.toImmutableList());
    }

    public List<GameTestState> getTests() {
        return this.tests;
    }

    public void run() {
        this.runBatch(0);
    }

    void runBatch(final int index) {
        if (index >= this.batches.size()) {
            return;
        }
        Pair<GameTestBatch, Collection<GameTestState>> pair = this.batches.get(index);
        final GameTestBatch lv = pair.getFirst();
        Collection<GameTestState> collection = pair.getSecond();
        Map<GameTestState, BlockPos> map = this.alignTestStructures(collection);
        String string = lv.getId();
        LOGGER.info("Running test batch '{}' ({} tests)...", (Object)string, (Object)collection.size());
        lv.startBatch(this.world);
        final TestSet lv2 = new TestSet();
        collection.forEach(lv2::add);
        lv2.addListener(new TestListener(){

            private void onFinished() {
                if (lv2.isDone()) {
                    lv.finishBatch(TestRunner.this.world);
                    TestRunner.this.runBatch(index + 1);
                }
            }

            @Override
            public void onStarted(GameTestState test) {
            }

            @Override
            public void onPassed(GameTestState test) {
                this.onFinished();
            }

            @Override
            public void onFailed(GameTestState test) {
                this.onFinished();
            }
        });
        collection.forEach(gameTest -> {
            BlockPos lv = (BlockPos)map.get(gameTest);
            TestUtil.startTest(gameTest, lv, this.testManager);
        });
    }

    private Map<GameTestState, BlockPos> alignTestStructures(Collection<GameTestState> gameTests) {
        HashMap<GameTestState, BlockPos> map = Maps.newHashMap();
        int i = 0;
        Box lv = new Box(this.reusablePos);
        for (GameTestState lv2 : gameTests) {
            BlockPos lv3 = new BlockPos(this.reusablePos);
            StructureBlockBlockEntity lv4 = StructureTestUtil.createStructureTemplate(lv2.getTemplateName(), lv3, lv2.getRotation(), 2, this.world, true);
            Box lv5 = StructureTestUtil.getStructureBoundingBox(lv4);
            lv2.setPos(lv4.getPos());
            map.put(lv2, new BlockPos(this.reusablePos));
            lv = lv.union(lv5);
            this.reusablePos.move((int)lv5.getXLength() + 5, 0, 0);
            if (i++ % this.sizeZ != this.sizeZ - 1) continue;
            this.reusablePos.move(0, 0, (int)lv.getZLength() + 6);
            this.reusablePos.setX(this.pos.getX());
            lv = new Box(this.reusablePos);
        }
        return map;
    }
}

