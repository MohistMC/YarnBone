/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.timer;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.timer.Timer;
import net.minecraft.world.timer.TimerCallback;

public class FunctionTimerCallback
implements TimerCallback<MinecraftServer> {
    final Identifier name;

    public FunctionTimerCallback(Identifier name) {
        this.name = name;
    }

    @Override
    public void call(MinecraftServer minecraftServer, Timer<MinecraftServer> arg, long l) {
        CommandFunctionManager lv = minecraftServer.getCommandFunctionManager();
        lv.getFunction(this.name).ifPresent(function -> lv.execute((CommandFunction)function, lv.getScheduledCommandSource()));
    }

    @Override
    public /* synthetic */ void call(Object server, Timer events, long time) {
        this.call((MinecraftServer)server, (Timer<MinecraftServer>)events, time);
    }

    public static class Serializer
    extends TimerCallback.Serializer<MinecraftServer, FunctionTimerCallback> {
        public Serializer() {
            super(new Identifier("function"), FunctionTimerCallback.class);
        }

        @Override
        public void serialize(NbtCompound arg, FunctionTimerCallback arg2) {
            arg.putString("Name", arg2.name.toString());
        }

        @Override
        public FunctionTimerCallback deserialize(NbtCompound arg) {
            Identifier lv = new Identifier(arg.getString("Name"));
            return new FunctionTimerCallback(lv);
        }

        @Override
        public /* synthetic */ TimerCallback deserialize(NbtCompound nbt) {
            return this.deserialize(nbt);
        }
    }
}

