/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util.profiling.jfr;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;
import net.minecraft.util.profiling.jfr.JfrProfile;
import net.minecraft.util.profiling.jfr.sample.ChunkGenerationSample;
import net.minecraft.util.profiling.jfr.sample.CpuLoadSample;
import net.minecraft.util.profiling.jfr.sample.FileIoSample;
import net.minecraft.util.profiling.jfr.sample.GcHeapSummarySample;
import net.minecraft.util.profiling.jfr.sample.NetworkIoStatistics;
import net.minecraft.util.profiling.jfr.sample.ServerTickTimeSample;
import net.minecraft.util.profiling.jfr.sample.ThreadAllocationStatisticsSample;
import org.jetbrains.annotations.Nullable;

public class JfrProfileRecorder {
    private Instant startTime = Instant.EPOCH;
    private Instant endTime = Instant.EPOCH;
    private final List<ChunkGenerationSample> chunkGenerationSamples = Lists.newArrayList();
    private final List<CpuLoadSample> cpuLoadSamples = Lists.newArrayList();
    private final Map<NetworkIoStatistics.Packet, PacketCounter> receivedPacketsToCounter = Maps.newHashMap();
    private final Map<NetworkIoStatistics.Packet, PacketCounter> sentPacketsToCounter = Maps.newHashMap();
    private final List<FileIoSample> fileWriteSamples = Lists.newArrayList();
    private final List<FileIoSample> fileReadSamples = Lists.newArrayList();
    private int gcCount;
    private Duration gcDuration = Duration.ZERO;
    private final List<GcHeapSummarySample> gcHeapSummarySamples = Lists.newArrayList();
    private final List<ThreadAllocationStatisticsSample> threadAllocationStatisticsSamples = Lists.newArrayList();
    private final List<ServerTickTimeSample> serverTickTimeSamples = Lists.newArrayList();
    @Nullable
    private Duration worldGenDuration = null;

    private JfrProfileRecorder(Stream<RecordedEvent> events) {
        this.handleEvents(events);
    }

    public static JfrProfile readProfile(Path path) {
        JfrProfile jfrProfile;
        final RecordingFile recordingFile = new RecordingFile(path);
        try {
            Iterator<RecordedEvent> iterator = new Iterator<RecordedEvent>(){

                @Override
                public boolean hasNext() {
                    return recordingFile.hasMoreEvents();
                }

                @Override
                public RecordedEvent next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    try {
                        return recordingFile.readEvent();
                    }
                    catch (IOException iOException) {
                        throw new UncheckedIOException(iOException);
                    }
                }

                @Override
                public /* synthetic */ Object next() {
                    return this.next();
                }
            };
            Stream<RecordedEvent> stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 1297), false);
            jfrProfile = new JfrProfileRecorder(stream).createProfile();
        }
        catch (Throwable throwable) {
            try {
                try {
                    recordingFile.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException iOException) {
                throw new UncheckedIOException(iOException);
            }
        }
        recordingFile.close();
        return jfrProfile;
    }

    private JfrProfile createProfile() {
        Duration duration = Duration.between(this.startTime, this.endTime);
        return new JfrProfile(this.startTime, this.endTime, duration, this.worldGenDuration, this.serverTickTimeSamples, this.cpuLoadSamples, GcHeapSummarySample.toStatistics(duration, this.gcHeapSummarySamples, this.gcDuration, this.gcCount), ThreadAllocationStatisticsSample.toAllocationMap(this.threadAllocationStatisticsSamples), JfrProfileRecorder.createNetworkIoStatistics(duration, this.receivedPacketsToCounter), JfrProfileRecorder.createNetworkIoStatistics(duration, this.sentPacketsToCounter), FileIoSample.toStatistics(duration, this.fileWriteSamples), FileIoSample.toStatistics(duration, this.fileReadSamples), this.chunkGenerationSamples);
    }

    private void handleEvents(Stream<RecordedEvent> events) {
        events.forEach(event -> {
            if (event.getEndTime().isAfter(this.endTime) || this.endTime.equals(Instant.EPOCH)) {
                this.endTime = event.getEndTime();
            }
            if (event.getStartTime().isBefore(this.startTime) || this.startTime.equals(Instant.EPOCH)) {
                this.startTime = event.getStartTime();
            }
            switch (event.getEventType().getName()) {
                case "minecraft.ChunkGeneration": {
                    this.chunkGenerationSamples.add(ChunkGenerationSample.fromEvent(event));
                    break;
                }
                case "minecraft.LoadWorld": {
                    this.worldGenDuration = event.getDuration();
                    break;
                }
                case "minecraft.ServerTickTime": {
                    this.serverTickTimeSamples.add(ServerTickTimeSample.fromEvent(event));
                    break;
                }
                case "minecraft.PacketReceived": {
                    this.addPacket((RecordedEvent)event, event.getInt("bytes"), this.receivedPacketsToCounter);
                    break;
                }
                case "minecraft.PacketSent": {
                    this.addPacket((RecordedEvent)event, event.getInt("bytes"), this.sentPacketsToCounter);
                    break;
                }
                case "jdk.ThreadAllocationStatistics": {
                    this.threadAllocationStatisticsSamples.add(ThreadAllocationStatisticsSample.fromEvent(event));
                    break;
                }
                case "jdk.GCHeapSummary": {
                    this.gcHeapSummarySamples.add(GcHeapSummarySample.fromEvent(event));
                    break;
                }
                case "jdk.CPULoad": {
                    this.cpuLoadSamples.add(CpuLoadSample.fromEvent(event));
                    break;
                }
                case "jdk.FileWrite": {
                    this.addFileIoSample((RecordedEvent)event, this.fileWriteSamples, "bytesWritten");
                    break;
                }
                case "jdk.FileRead": {
                    this.addFileIoSample((RecordedEvent)event, this.fileReadSamples, "bytesRead");
                    break;
                }
                case "jdk.GarbageCollection": {
                    ++this.gcCount;
                    this.gcDuration = this.gcDuration.plus(event.getDuration());
                    break;
                }
            }
        });
    }

    private void addPacket(RecordedEvent event, int bytes, Map<NetworkIoStatistics.Packet, PacketCounter> packetsToCounter) {
        packetsToCounter.computeIfAbsent(NetworkIoStatistics.Packet.fromEvent(event), packet -> new PacketCounter()).add(bytes);
    }

    private void addFileIoSample(RecordedEvent event, List<FileIoSample> samples, String bytesKey) {
        samples.add(new FileIoSample(event.getDuration(), event.getString("path"), event.getLong(bytesKey)));
    }

    private static NetworkIoStatistics createNetworkIoStatistics(Duration duration, Map<NetworkIoStatistics.Packet, PacketCounter> packetsToCounter) {
        List<Pair<NetworkIoStatistics.Packet, NetworkIoStatistics.PacketStatistics>> list = packetsToCounter.entrySet().stream().map(entry -> Pair.of((NetworkIoStatistics.Packet)entry.getKey(), ((PacketCounter)entry.getValue()).toStatistics())).toList();
        return new NetworkIoStatistics(duration, list);
    }

    public static final class PacketCounter {
        private long totalCount;
        private long totalBytes;

        public void add(int bytes) {
            this.totalBytes += (long)bytes;
            ++this.totalCount;
        }

        public NetworkIoStatistics.PacketStatistics toStatistics() {
            return new NetworkIoStatistics.PacketStatistics(this.totalCount, this.totalBytes);
        }
    }
}

