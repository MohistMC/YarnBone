/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.TestClassArgumentType;
import net.minecraft.command.argument.TestFunctionArgumentType;
import net.minecraft.data.DataWriter;
import net.minecraft.data.dev.NbtProvider;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTestState;
import net.minecraft.test.StructureTestUtil;
import net.minecraft.test.TestFunction;
import net.minecraft.test.TestFunctions;
import net.minecraft.test.TestListener;
import net.minecraft.test.TestManager;
import net.minecraft.test.TestSet;
import net.minecraft.test.TestUtil;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

public class TestCommand {
    private static final int field_33178 = 200;
    private static final int field_33179 = 1024;
    private static final int field_33180 = 15;
    private static final int field_33181 = 200;
    private static final int field_33182 = 3;
    private static final int field_33183 = 10000;
    private static final int field_33184 = 5;
    private static final int field_33185 = 5;
    private static final int field_33186 = 5;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("test").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("runthis").executes(context -> TestCommand.executeRunThis((ServerCommandSource)context.getSource())))).then(CommandManager.literal("runthese").executes(context -> TestCommand.executeRunThese((ServerCommandSource)context.getSource())))).then(((LiteralArgumentBuilder)CommandManager.literal("runfailed").executes(context -> TestCommand.executeRerunFailed((ServerCommandSource)context.getSource(), false, 0, 8))).then(((RequiredArgumentBuilder)CommandManager.argument("onlyRequiredTests", BoolArgumentType.bool()).executes(context -> TestCommand.executeRerunFailed((ServerCommandSource)context.getSource(), BoolArgumentType.getBool(context, "onlyRequiredTests"), 0, 8))).then(((RequiredArgumentBuilder)CommandManager.argument("rotationSteps", IntegerArgumentType.integer()).executes(context -> TestCommand.executeRerunFailed((ServerCommandSource)context.getSource(), BoolArgumentType.getBool(context, "onlyRequiredTests"), IntegerArgumentType.getInteger(context, "rotationSteps"), 8))).then(CommandManager.argument("testsPerRow", IntegerArgumentType.integer()).executes(context -> TestCommand.executeRerunFailed((ServerCommandSource)context.getSource(), BoolArgumentType.getBool(context, "onlyRequiredTests"), IntegerArgumentType.getInteger(context, "rotationSteps"), IntegerArgumentType.getInteger(context, "testsPerRow")))))))).then(CommandManager.literal("run").then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("testName", TestFunctionArgumentType.testFunction()).executes(context -> TestCommand.executeRun((ServerCommandSource)context.getSource(), TestFunctionArgumentType.getFunction(context, "testName"), 0))).then(CommandManager.argument("rotationSteps", IntegerArgumentType.integer()).executes(context -> TestCommand.executeRun((ServerCommandSource)context.getSource(), TestFunctionArgumentType.getFunction(context, "testName"), IntegerArgumentType.getInteger(context, "rotationSteps"))))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("runall").executes(context -> TestCommand.executeRunAll((ServerCommandSource)context.getSource(), 0, 8))).then(((RequiredArgumentBuilder)CommandManager.argument("testClassName", TestClassArgumentType.testClass()).executes(context -> TestCommand.executeRunAll((ServerCommandSource)context.getSource(), TestClassArgumentType.getTestClass(context, "testClassName"), 0, 8))).then(((RequiredArgumentBuilder)CommandManager.argument("rotationSteps", IntegerArgumentType.integer()).executes(context -> TestCommand.executeRunAll((ServerCommandSource)context.getSource(), TestClassArgumentType.getTestClass(context, "testClassName"), IntegerArgumentType.getInteger(context, "rotationSteps"), 8))).then(CommandManager.argument("testsPerRow", IntegerArgumentType.integer()).executes(context -> TestCommand.executeRunAll((ServerCommandSource)context.getSource(), TestClassArgumentType.getTestClass(context, "testClassName"), IntegerArgumentType.getInteger(context, "rotationSteps"), IntegerArgumentType.getInteger(context, "testsPerRow"))))))).then(((RequiredArgumentBuilder)CommandManager.argument("rotationSteps", IntegerArgumentType.integer()).executes(context -> TestCommand.executeRunAll((ServerCommandSource)context.getSource(), IntegerArgumentType.getInteger(context, "rotationSteps"), 8))).then(CommandManager.argument("testsPerRow", IntegerArgumentType.integer()).executes(context -> TestCommand.executeRunAll((ServerCommandSource)context.getSource(), IntegerArgumentType.getInteger(context, "rotationSteps"), IntegerArgumentType.getInteger(context, "testsPerRow"))))))).then(CommandManager.literal("export").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("testName", StringArgumentType.word()).executes(context -> TestCommand.executeExport((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "testName")))))).then(CommandManager.literal("exportthis").executes(context -> TestCommand.executeExport((ServerCommandSource)context.getSource())))).then(CommandManager.literal("import").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("testName", StringArgumentType.word()).executes(context -> TestCommand.executeImport((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "testName")))))).then(((LiteralArgumentBuilder)CommandManager.literal("pos").executes(context -> TestCommand.executePos((ServerCommandSource)context.getSource(), "pos"))).then(CommandManager.argument("var", StringArgumentType.word()).executes(context -> TestCommand.executePos((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "var")))))).then(CommandManager.literal("create").then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("testName", StringArgumentType.word()).executes(context -> TestCommand.executeCreate((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "testName"), 5, 5, 5))).then(((RequiredArgumentBuilder)CommandManager.argument("width", IntegerArgumentType.integer()).executes(context -> TestCommand.executeCreate((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "testName"), IntegerArgumentType.getInteger(context, "width"), IntegerArgumentType.getInteger(context, "width"), IntegerArgumentType.getInteger(context, "width")))).then(CommandManager.argument("height", IntegerArgumentType.integer()).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("depth", IntegerArgumentType.integer()).executes(context -> TestCommand.executeCreate((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "testName"), IntegerArgumentType.getInteger(context, "width"), IntegerArgumentType.getInteger(context, "height"), IntegerArgumentType.getInteger(context, "depth"))))))))).then(((LiteralArgumentBuilder)CommandManager.literal("clearall").executes(context -> TestCommand.executeClearAll((ServerCommandSource)context.getSource(), 200))).then(CommandManager.argument("radius", IntegerArgumentType.integer()).executes(context -> TestCommand.executeClearAll((ServerCommandSource)context.getSource(), IntegerArgumentType.getInteger(context, "radius"))))));
    }

    private static int executeCreate(ServerCommandSource source, String testName, int x, int y, int z) {
        if (x > 48 || y > 48 || z > 48) {
            throw new IllegalArgumentException("The structure must be less than 48 blocks big in each axis");
        }
        ServerWorld lv = source.getWorld();
        BlockPos lv2 = BlockPos.ofFloored(source.getPosition());
        BlockPos lv3 = new BlockPos(lv2.getX(), source.getWorld().getTopPosition(Heightmap.Type.WORLD_SURFACE, lv2).getY(), lv2.getZ() + 3);
        StructureTestUtil.createTestArea(testName.toLowerCase(), lv3, new Vec3i(x, y, z), BlockRotation.NONE, lv);
        for (int l = 0; l < x; ++l) {
            for (int m = 0; m < z; ++m) {
                BlockPos lv4 = new BlockPos(lv3.getX() + l, lv3.getY() + 1, lv3.getZ() + m);
                Block lv5 = Blocks.POLISHED_ANDESITE;
                BlockStateArgument lv6 = new BlockStateArgument(lv5.getDefaultState(), Collections.emptySet(), null);
                lv6.setBlockState(lv, lv4, Block.NOTIFY_LISTENERS);
            }
        }
        StructureTestUtil.placeStartButton(lv3, new BlockPos(1, 0, -1), BlockRotation.NONE, lv);
        return 0;
    }

    private static int executePos(ServerCommandSource source, String variableName) throws CommandSyntaxException {
        ServerWorld lv3;
        BlockHitResult lv = (BlockHitResult)source.getPlayerOrThrow().raycast(10.0, 1.0f, false);
        BlockPos lv2 = lv.getBlockPos();
        Optional<BlockPos> optional = StructureTestUtil.findContainingStructureBlock(lv2, 15, lv3 = source.getWorld());
        if (!optional.isPresent()) {
            optional = StructureTestUtil.findContainingStructureBlock(lv2, 200, lv3);
        }
        if (!optional.isPresent()) {
            source.sendError(Text.literal("Can't find a structure block that contains the targeted pos " + lv2));
            return 0;
        }
        StructureBlockBlockEntity lv4 = (StructureBlockBlockEntity)lv3.getBlockEntity(optional.get());
        BlockPos lv5 = lv2.subtract(optional.get());
        String string2 = lv5.getX() + ", " + lv5.getY() + ", " + lv5.getZ();
        String string3 = lv4.getStructurePath();
        MutableText lv6 = Text.literal(string2).setStyle(Style.EMPTY.withBold(true).withColor(Formatting.GREEN).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to copy to clipboard"))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "final BlockPos " + variableName + " = new BlockPos(" + string2 + ");")));
        source.sendFeedback(Text.literal("Position relative to " + string3 + ": ").append(lv6), false);
        DebugInfoSender.addGameTestMarker(lv3, new BlockPos(lv2), string2, -2147418368, 10000);
        return 1;
    }

    private static int executeRunThis(ServerCommandSource source) {
        ServerWorld lv2;
        BlockPos lv = BlockPos.ofFloored(source.getPosition());
        BlockPos lv3 = StructureTestUtil.findNearestStructureBlock(lv, 15, lv2 = source.getWorld());
        if (lv3 == null) {
            TestCommand.sendMessage(lv2, "Couldn't find any structure block within 15 radius", Formatting.RED);
            return 0;
        }
        TestUtil.clearDebugMarkers(lv2);
        TestCommand.run(lv2, lv3, null);
        return 1;
    }

    private static int executeRunThese(ServerCommandSource source) {
        ServerWorld lv2;
        BlockPos lv = BlockPos.ofFloored(source.getPosition());
        Collection<BlockPos> collection = StructureTestUtil.findStructureBlocks(lv, 200, lv2 = source.getWorld());
        if (collection.isEmpty()) {
            TestCommand.sendMessage(lv2, "Couldn't find any structure blocks within 200 block radius", Formatting.RED);
            return 1;
        }
        TestUtil.clearDebugMarkers(lv2);
        TestCommand.sendMessage(source, "Running " + collection.size() + " tests...");
        TestSet lv3 = new TestSet();
        collection.forEach(pos -> TestCommand.run(lv2, pos, lv3));
        return 1;
    }

    private static void run(ServerWorld world, BlockPos pos, @Nullable TestSet tests) {
        StructureBlockBlockEntity lv = (StructureBlockBlockEntity)world.getBlockEntity(pos);
        String string = lv.getStructurePath();
        TestFunction lv2 = TestFunctions.getTestFunctionOrThrow(string);
        GameTestState lv3 = new GameTestState(lv2, lv.getRotation(), world);
        if (tests != null) {
            tests.add(lv3);
            lv3.addListener(new Listener(world, tests));
        }
        TestCommand.beforeBatch(lv2, world);
        Box lv4 = StructureTestUtil.getStructureBoundingBox(lv);
        BlockPos lv5 = BlockPos.ofFloored(lv4.minX, lv4.minY, lv4.minZ);
        TestUtil.startTest(lv3, lv5, TestManager.INSTANCE);
    }

    static void onCompletion(ServerWorld world, TestSet tests) {
        if (tests.isDone()) {
            TestCommand.sendMessage(world, "GameTest done! " + tests.getTestCount() + " tests were run", Formatting.WHITE);
            if (tests.failed()) {
                TestCommand.sendMessage(world, tests.getFailedRequiredTestCount() + " required tests failed :(", Formatting.RED);
            } else {
                TestCommand.sendMessage(world, "All required tests passed :)", Formatting.GREEN);
            }
            if (tests.hasFailedOptionalTests()) {
                TestCommand.sendMessage(world, tests.getFailedOptionalTestCount() + " optional tests failed", Formatting.GRAY);
            }
        }
    }

    private static int executeClearAll(ServerCommandSource source, int radius) {
        ServerWorld lv = source.getWorld();
        TestUtil.clearDebugMarkers(lv);
        BlockPos lv2 = BlockPos.ofFloored(source.getPosition().x, source.getWorld().getTopPosition(Heightmap.Type.WORLD_SURFACE, BlockPos.ofFloored(source.getPosition())).getY(), source.getPosition().z);
        TestUtil.clearTests(lv, lv2, TestManager.INSTANCE, MathHelper.clamp(radius, 0, 1024));
        return 1;
    }

    private static int executeRun(ServerCommandSource source, TestFunction testFunction, int rotationSteps) {
        ServerWorld lv = source.getWorld();
        BlockPos lv2 = BlockPos.ofFloored(source.getPosition());
        int j = source.getWorld().getTopPosition(Heightmap.Type.WORLD_SURFACE, lv2).getY();
        BlockPos lv3 = new BlockPos(lv2.getX(), j, lv2.getZ() + 3);
        TestUtil.clearDebugMarkers(lv);
        TestCommand.beforeBatch(testFunction, lv);
        BlockRotation lv4 = StructureTestUtil.getRotation(rotationSteps);
        GameTestState lv5 = new GameTestState(testFunction, lv4, lv);
        TestUtil.startTest(lv5, lv3, TestManager.INSTANCE);
        return 1;
    }

    private static void beforeBatch(TestFunction testFunction, ServerWorld world) {
        Consumer<ServerWorld> consumer = TestFunctions.getBeforeBatchConsumer(testFunction.getBatchId());
        if (consumer != null) {
            consumer.accept(world);
        }
    }

    private static int executeRunAll(ServerCommandSource source, int rotationSteps, int sizeZ) {
        TestUtil.clearDebugMarkers(source.getWorld());
        Collection<TestFunction> collection = TestFunctions.getTestFunctions();
        TestCommand.sendMessage(source, "Running all " + collection.size() + " tests...");
        TestFunctions.clearFailedTestFunctions();
        TestCommand.run(source, collection, rotationSteps, sizeZ);
        return 1;
    }

    private static int executeRunAll(ServerCommandSource source, String testClass, int rotationSteps, int sizeZ) {
        Collection<TestFunction> collection = TestFunctions.getTestFunctions(testClass);
        TestUtil.clearDebugMarkers(source.getWorld());
        TestCommand.sendMessage(source, "Running " + collection.size() + " tests from " + testClass + "...");
        TestFunctions.clearFailedTestFunctions();
        TestCommand.run(source, collection, rotationSteps, sizeZ);
        return 1;
    }

    private static int executeRerunFailed(ServerCommandSource source, boolean requiredOnly, int rotationSteps, int sizeZ) {
        Collection collection = requiredOnly ? (Collection)TestFunctions.getFailedTestFunctions().stream().filter(TestFunction::isRequired).collect(Collectors.toList()) : TestFunctions.getFailedTestFunctions();
        if (collection.isEmpty()) {
            TestCommand.sendMessage(source, "No failed tests to rerun");
            return 0;
        }
        TestUtil.clearDebugMarkers(source.getWorld());
        TestCommand.sendMessage(source, "Rerunning " + collection.size() + " failed tests (" + (requiredOnly ? "only required tests" : "including optional tests") + ")");
        TestCommand.run(source, collection, rotationSteps, sizeZ);
        return 1;
    }

    private static void run(ServerCommandSource source, Collection<TestFunction> testFunctions, int rotationSteps, int j) {
        BlockPos lv = BlockPos.ofFloored(source.getPosition());
        BlockPos lv2 = new BlockPos(lv.getX(), source.getWorld().getTopPosition(Heightmap.Type.WORLD_SURFACE, lv).getY(), lv.getZ() + 3);
        ServerWorld lv3 = source.getWorld();
        BlockRotation lv4 = StructureTestUtil.getRotation(rotationSteps);
        Collection<GameTestState> collection2 = TestUtil.runTestFunctions(testFunctions, lv2, lv4, lv3, TestManager.INSTANCE, j);
        TestSet lv5 = new TestSet(collection2);
        lv5.addListener(new Listener(lv3, lv5));
        lv5.addListener(test -> TestFunctions.addFailedTestFunction(test.getTestFunction()));
    }

    private static void sendMessage(ServerCommandSource source, String message) {
        source.sendFeedback(Text.literal(message), false);
    }

    private static int executeExport(ServerCommandSource source) {
        ServerWorld lv2;
        BlockPos lv = BlockPos.ofFloored(source.getPosition());
        BlockPos lv3 = StructureTestUtil.findNearestStructureBlock(lv, 15, lv2 = source.getWorld());
        if (lv3 == null) {
            TestCommand.sendMessage(lv2, "Couldn't find any structure block within 15 radius", Formatting.RED);
            return 0;
        }
        StructureBlockBlockEntity lv4 = (StructureBlockBlockEntity)lv2.getBlockEntity(lv3);
        String string = lv4.getStructurePath();
        return TestCommand.executeExport(source, string);
    }

    private static int executeExport(ServerCommandSource source, String testName) {
        Path path = Paths.get(StructureTestUtil.testStructuresDirectoryName, new String[0]);
        Identifier lv = new Identifier("minecraft", testName);
        Path path2 = source.getWorld().getStructureTemplateManager().getTemplatePath(lv, ".nbt");
        Path path3 = NbtProvider.convertNbtToSnbt(DataWriter.UNCACHED, path2, testName, path);
        if (path3 == null) {
            TestCommand.sendMessage(source, "Failed to export " + path2);
            return 1;
        }
        try {
            Files.createDirectories(path3.getParent(), new FileAttribute[0]);
        }
        catch (IOException iOException) {
            TestCommand.sendMessage(source, "Could not create folder " + path3.getParent());
            iOException.printStackTrace();
            return 1;
        }
        TestCommand.sendMessage(source, "Exported " + testName + " to " + path3.toAbsolutePath());
        return 0;
    }

    private static int executeImport(ServerCommandSource source, String testName) {
        Path path = Paths.get(StructureTestUtil.testStructuresDirectoryName, testName + ".snbt");
        Identifier lv = new Identifier("minecraft", testName);
        Path path2 = source.getWorld().getStructureTemplateManager().getTemplatePath(lv, ".nbt");
        try {
            BufferedReader bufferedReader = Files.newBufferedReader(path);
            String string2 = IOUtils.toString(bufferedReader);
            Files.createDirectories(path2.getParent(), new FileAttribute[0]);
            try (OutputStream outputStream = Files.newOutputStream(path2, new OpenOption[0]);){
                NbtIo.writeCompressed(NbtHelper.fromNbtProviderString(string2), outputStream);
            }
            TestCommand.sendMessage(source, "Imported to " + path2.toAbsolutePath());
            return 0;
        }
        catch (CommandSyntaxException | IOException exception) {
            System.err.println("Failed to load structure " + testName);
            exception.printStackTrace();
            return 1;
        }
    }

    private static void sendMessage(ServerWorld world, String message, Formatting formatting) {
        world.getPlayers(player -> true).forEach(player -> player.sendMessage(Text.literal(formatting + message)));
    }

    static class Listener
    implements TestListener {
        private final ServerWorld world;
        private final TestSet tests;

        public Listener(ServerWorld world, TestSet tests) {
            this.world = world;
            this.tests = tests;
        }

        @Override
        public void onStarted(GameTestState test) {
        }

        @Override
        public void onPassed(GameTestState test) {
            TestCommand.onCompletion(this.world, this.tests);
        }

        @Override
        public void onFailed(GameTestState test) {
            TestCommand.onCompletion(this.world, this.tests);
        }
    }
}

