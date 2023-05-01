/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai.pathing;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.TargetPathNode;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class Path {
    private final List<PathNode> nodes;
    private PathNode[] debugNodes = new PathNode[0];
    private PathNode[] debugSecondNodes = new PathNode[0];
    @Nullable
    private Set<TargetPathNode> debugTargetNodes;
    private int currentNodeIndex;
    private final BlockPos target;
    private final float manhattanDistanceFromTarget;
    private final boolean reachesTarget;

    public Path(List<PathNode> nodes, BlockPos target, boolean reachesTarget) {
        this.nodes = nodes;
        this.target = target;
        this.manhattanDistanceFromTarget = nodes.isEmpty() ? Float.MAX_VALUE : this.nodes.get(this.nodes.size() - 1).getManhattanDistance(this.target);
        this.reachesTarget = reachesTarget;
    }

    public void next() {
        ++this.currentNodeIndex;
    }

    public boolean isStart() {
        return this.currentNodeIndex <= 0;
    }

    public boolean isFinished() {
        return this.currentNodeIndex >= this.nodes.size();
    }

    @Nullable
    public PathNode getEnd() {
        if (!this.nodes.isEmpty()) {
            return this.nodes.get(this.nodes.size() - 1);
        }
        return null;
    }

    public PathNode getNode(int index) {
        return this.nodes.get(index);
    }

    public void setLength(int length) {
        if (this.nodes.size() > length) {
            this.nodes.subList(length, this.nodes.size()).clear();
        }
    }

    public void setNode(int index, PathNode node) {
        this.nodes.set(index, node);
    }

    public int getLength() {
        return this.nodes.size();
    }

    public int getCurrentNodeIndex() {
        return this.currentNodeIndex;
    }

    public void setCurrentNodeIndex(int nodeIndex) {
        this.currentNodeIndex = nodeIndex;
    }

    public Vec3d getNodePosition(Entity entity, int index) {
        PathNode lv = this.nodes.get(index);
        double d = (double)lv.x + (double)((int)(entity.getWidth() + 1.0f)) * 0.5;
        double e = lv.y;
        double f = (double)lv.z + (double)((int)(entity.getWidth() + 1.0f)) * 0.5;
        return new Vec3d(d, e, f);
    }

    public BlockPos getNodePos(int index) {
        return this.nodes.get(index).getBlockPos();
    }

    public Vec3d getNodePosition(Entity entity) {
        return this.getNodePosition(entity, this.currentNodeIndex);
    }

    public BlockPos getCurrentNodePos() {
        return this.nodes.get(this.currentNodeIndex).getBlockPos();
    }

    public PathNode getCurrentNode() {
        return this.nodes.get(this.currentNodeIndex);
    }

    @Nullable
    public PathNode getLastNode() {
        return this.currentNodeIndex > 0 ? this.nodes.get(this.currentNodeIndex - 1) : null;
    }

    public boolean equalsPath(@Nullable Path o) {
        if (o == null) {
            return false;
        }
        if (o.nodes.size() != this.nodes.size()) {
            return false;
        }
        for (int i = 0; i < this.nodes.size(); ++i) {
            PathNode lv = this.nodes.get(i);
            PathNode lv2 = o.nodes.get(i);
            if (lv.x == lv2.x && lv.y == lv2.y && lv.z == lv2.z) continue;
            return false;
        }
        return true;
    }

    public boolean reachesTarget() {
        return this.reachesTarget;
    }

    @Debug
    void setDebugInfo(PathNode[] debugNodes, PathNode[] debugSecondNodes, Set<TargetPathNode> debugTargetNodes) {
        this.debugNodes = debugNodes;
        this.debugSecondNodes = debugSecondNodes;
        this.debugTargetNodes = debugTargetNodes;
    }

    @Debug
    public PathNode[] getDebugNodes() {
        return this.debugNodes;
    }

    @Debug
    public PathNode[] getDebugSecondNodes() {
        return this.debugSecondNodes;
    }

    public void toBuffer(PacketByteBuf buffer) {
        if (this.debugTargetNodes == null || this.debugTargetNodes.isEmpty()) {
            return;
        }
        buffer.writeBoolean(this.reachesTarget);
        buffer.writeInt(this.currentNodeIndex);
        buffer.writeInt(this.debugTargetNodes.size());
        this.debugTargetNodes.forEach(arg2 -> arg2.write(buffer));
        buffer.writeInt(this.target.getX());
        buffer.writeInt(this.target.getY());
        buffer.writeInt(this.target.getZ());
        buffer.writeInt(this.nodes.size());
        for (PathNode lv : this.nodes) {
            lv.write(buffer);
        }
        buffer.writeInt(this.debugNodes.length);
        for (PathNode lv2 : this.debugNodes) {
            lv2.write(buffer);
        }
        buffer.writeInt(this.debugSecondNodes.length);
        for (PathNode lv2 : this.debugSecondNodes) {
            lv2.write(buffer);
        }
    }

    public static Path fromBuffer(PacketByteBuf buffer) {
        boolean bl = buffer.readBoolean();
        int i = buffer.readInt();
        int j = buffer.readInt();
        HashSet<TargetPathNode> set = Sets.newHashSet();
        for (int k = 0; k < j; ++k) {
            set.add(TargetPathNode.fromBuffer(buffer));
        }
        BlockPos lv = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
        ArrayList<PathNode> list = Lists.newArrayList();
        int l = buffer.readInt();
        for (int m = 0; m < l; ++m) {
            list.add(PathNode.fromBuf(buffer));
        }
        PathNode[] lvs = new PathNode[buffer.readInt()];
        for (int n = 0; n < lvs.length; ++n) {
            lvs[n] = PathNode.fromBuf(buffer);
        }
        PathNode[] lvs2 = new PathNode[buffer.readInt()];
        for (int o = 0; o < lvs2.length; ++o) {
            lvs2[o] = PathNode.fromBuf(buffer);
        }
        Path lv2 = new Path(list, lv, bl);
        lv2.debugNodes = lvs;
        lv2.debugSecondNodes = lvs2;
        lv2.debugTargetNodes = set;
        lv2.currentNodeIndex = i;
        return lv2;
    }

    public String toString() {
        return "Path(length=" + this.nodes.size() + ")";
    }

    public BlockPos getTarget() {
        return this.target;
    }

    public float getManhattanDistanceFromTarget() {
        return this.manhattanDistanceFromTarget;
    }
}

