/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.util.profiling.jfr;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.LongSerializationPolicy;
import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;
import net.minecraft.util.Util;
import net.minecraft.util.math.Quantiles;
import net.minecraft.util.profiling.jfr.JfrProfile;
import net.minecraft.util.profiling.jfr.sample.ChunkGenerationSample;
import net.minecraft.util.profiling.jfr.sample.CpuLoadSample;
import net.minecraft.util.profiling.jfr.sample.FileIoSample;
import net.minecraft.util.profiling.jfr.sample.GcHeapSummarySample;
import net.minecraft.util.profiling.jfr.sample.LongRunningSampleStatistics;
import net.minecraft.util.profiling.jfr.sample.NetworkIoStatistics;
import net.minecraft.util.profiling.jfr.sample.ServerTickTimeSample;
import net.minecraft.util.profiling.jfr.sample.ThreadAllocationStatisticsSample;
import net.minecraft.world.chunk.ChunkStatus;

public class JfrJsonReport {
    private static final String BYTES_PER_SECOND = "bytesPerSecond";
    private static final String COUNT = "count";
    private static final String DURATION_NANOS_TOTAL = "durationNanosTotal";
    private static final String TOTAL_BYTES = "totalBytes";
    private static final String COUNT_PER_SECOND = "countPerSecond";
    final Gson gson = new GsonBuilder().setPrettyPrinting().setLongSerializationPolicy(LongSerializationPolicy.DEFAULT).create();

    public String toString(JfrProfile profile) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("startedEpoch", profile.startTime().toEpochMilli());
        jsonObject.addProperty("endedEpoch", profile.endTime().toEpochMilli());
        jsonObject.addProperty("durationMs", profile.duration().toMillis());
        Duration duration = profile.worldGenDuration();
        if (duration != null) {
            jsonObject.addProperty("worldGenDurationMs", duration.toMillis());
        }
        jsonObject.add("heap", this.collectHeapSection(profile.gcHeapSummaryStatistics()));
        jsonObject.add("cpuPercent", this.collectCpuPercentSection(profile.cpuLoadSamples()));
        jsonObject.add("network", this.collectNetworkSection(profile));
        jsonObject.add("fileIO", this.collectFileIoSection(profile));
        jsonObject.add("serverTick", this.collectServerTickSection(profile.serverTickTimeSamples()));
        jsonObject.add("threadAllocation", this.collectThreadAllocationSection(profile.threadAllocationMap()));
        jsonObject.add("chunkGen", this.collectChunkGenSection(profile.getChunkGenerationSampleStatistics()));
        return this.gson.toJson(jsonObject);
    }

    private JsonElement collectHeapSection(GcHeapSummarySample.Statistics statistics) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("allocationRateBytesPerSecond", statistics.allocatedBytesPerSecond());
        jsonObject.addProperty("gcCount", statistics.count());
        jsonObject.addProperty("gcOverHeadPercent", Float.valueOf(statistics.getGcDurationRatio()));
        jsonObject.addProperty("gcTotalDurationMs", statistics.gcDuration().toMillis());
        return jsonObject;
    }

    private JsonElement collectChunkGenSection(List<Pair<ChunkStatus, LongRunningSampleStatistics<ChunkGenerationSample>>> statistics) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(DURATION_NANOS_TOTAL, statistics.stream().mapToDouble(pair -> ((LongRunningSampleStatistics)pair.getSecond()).totalDuration().toNanos()).sum());
        JsonArray jsonArray = Util.make(new JsonArray(), json -> jsonObject.add("status", (JsonElement)json));
        for (Pair<ChunkStatus, LongRunningSampleStatistics<ChunkGenerationSample>> pair2 : statistics) {
            LongRunningSampleStatistics<ChunkGenerationSample> lv = pair2.getSecond();
            JsonObject jsonObject2 = Util.make(new JsonObject(), jsonArray::add);
            jsonObject2.addProperty("state", pair2.getFirst().getId());
            jsonObject2.addProperty(COUNT, lv.count());
            jsonObject2.addProperty(DURATION_NANOS_TOTAL, lv.totalDuration().toNanos());
            jsonObject2.addProperty("durationNanosAvg", lv.totalDuration().toNanos() / (long)lv.count());
            JsonObject jsonObject3 = Util.make(new JsonObject(), json -> jsonObject2.add("durationNanosPercentiles", (JsonElement)json));
            lv.quantiles().forEach((quantile, value) -> jsonObject3.addProperty("p" + quantile, (Number)value));
            Function<ChunkGenerationSample, JsonElement> function = sample -> {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("durationNanos", sample.duration().toNanos());
                jsonObject.addProperty("level", sample.worldKey());
                jsonObject.addProperty("chunkPosX", sample.chunkPos().x);
                jsonObject.addProperty("chunkPosZ", sample.chunkPos().z);
                jsonObject.addProperty("worldPosX", sample.centerPos().x());
                jsonObject.addProperty("worldPosZ", sample.centerPos().z());
                return jsonObject;
            };
            jsonObject2.add("fastest", function.apply(lv.fastestSample()));
            jsonObject2.add("slowest", function.apply(lv.slowestSample()));
            jsonObject2.add("secondSlowest", lv.secondSlowestSample() != null ? function.apply(lv.secondSlowestSample()) : JsonNull.INSTANCE);
        }
        return jsonObject;
    }

    private JsonElement collectThreadAllocationSection(ThreadAllocationStatisticsSample.AllocationMap statistics) {
        JsonArray jsonArray = new JsonArray();
        statistics.allocations().forEach((threadName, allocation) -> jsonArray.add(Util.make(new JsonObject(), json -> {
            json.addProperty("thread", (String)threadName);
            json.addProperty(BYTES_PER_SECOND, (Number)allocation);
        })));
        return jsonArray;
    }

    private JsonElement collectServerTickSection(List<ServerTickTimeSample> samples) {
        if (samples.isEmpty()) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        double[] ds = samples.stream().mapToDouble(sample -> (double)sample.averageTickMs().toNanos() / 1000000.0).toArray();
        DoubleSummaryStatistics doubleSummaryStatistics = DoubleStream.of(ds).summaryStatistics();
        jsonObject.addProperty("minMs", doubleSummaryStatistics.getMin());
        jsonObject.addProperty("averageMs", doubleSummaryStatistics.getAverage());
        jsonObject.addProperty("maxMs", doubleSummaryStatistics.getMax());
        Map<Integer, Double> map = Quantiles.create(ds);
        map.forEach((quantile, value) -> jsonObject.addProperty("p" + quantile, (Number)value));
        return jsonObject;
    }

    private JsonElement collectFileIoSection(JfrProfile profile) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("write", this.collectFileIoSection(profile.fileWriteStatistics()));
        jsonObject.add("read", this.collectFileIoSection(profile.fileReadStatistics()));
        return jsonObject;
    }

    private JsonElement collectFileIoSection(FileIoSample.Statistics statistics) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(TOTAL_BYTES, statistics.totalBytes());
        jsonObject.addProperty(COUNT, statistics.count());
        jsonObject.addProperty(BYTES_PER_SECOND, statistics.bytesPerSecond());
        jsonObject.addProperty(COUNT_PER_SECOND, statistics.countPerSecond());
        JsonArray jsonArray = new JsonArray();
        jsonObject.add("topContributors", jsonArray);
        statistics.topContributors().forEach(pair -> {
            JsonObject jsonObject = new JsonObject();
            jsonArray.add(jsonObject);
            jsonObject.addProperty("path", (String)pair.getFirst());
            jsonObject.addProperty(TOTAL_BYTES, (Number)pair.getSecond());
        });
        return jsonObject;
    }

    private JsonElement collectNetworkSection(JfrProfile profile) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("sent", this.collectPacketSection(profile.packetSentStatistics()));
        jsonObject.add("received", this.collectPacketSection(profile.packetReadStatistics()));
        return jsonObject;
    }

    private JsonElement collectPacketSection(NetworkIoStatistics statistics) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(TOTAL_BYTES, statistics.getTotalSize());
        jsonObject.addProperty(COUNT, statistics.getTotalCount());
        jsonObject.addProperty(BYTES_PER_SECOND, statistics.getBytesPerSecond());
        jsonObject.addProperty(COUNT_PER_SECOND, statistics.getCountPerSecond());
        JsonArray jsonArray = new JsonArray();
        jsonObject.add("topContributors", jsonArray);
        statistics.getTopContributors().forEach(pair -> {
            JsonObject jsonObject = new JsonObject();
            jsonArray.add(jsonObject);
            NetworkIoStatistics.Packet lv = (NetworkIoStatistics.Packet)pair.getFirst();
            NetworkIoStatistics.PacketStatistics lv2 = (NetworkIoStatistics.PacketStatistics)pair.getSecond();
            jsonObject.addProperty("protocolId", lv.protocolId());
            jsonObject.addProperty("packetId", lv.packetId());
            jsonObject.addProperty("packetName", lv.getName());
            jsonObject.addProperty(TOTAL_BYTES, lv2.totalSize());
            jsonObject.addProperty(COUNT, lv2.totalCount());
        });
        return jsonObject;
    }

    private JsonElement collectCpuPercentSection(List<CpuLoadSample> samples2) {
        JsonObject jsonObject = new JsonObject();
        BiFunction<List, ToDoubleFunction, JsonObject> biFunction = (samples, valueGetter) -> {
            JsonObject jsonObject = new JsonObject();
            DoubleSummaryStatistics doubleSummaryStatistics = samples.stream().mapToDouble(valueGetter).summaryStatistics();
            jsonObject.addProperty("min", doubleSummaryStatistics.getMin());
            jsonObject.addProperty("average", doubleSummaryStatistics.getAverage());
            jsonObject.addProperty("max", doubleSummaryStatistics.getMax());
            return jsonObject;
        };
        jsonObject.add("jvm", biFunction.apply(samples2, CpuLoadSample::jvm));
        jsonObject.add("userJvm", biFunction.apply(samples2, CpuLoadSample::userJvm));
        jsonObject.add("system", biFunction.apply(samples2, CpuLoadSample::system));
        return jsonObject;
    }
}

