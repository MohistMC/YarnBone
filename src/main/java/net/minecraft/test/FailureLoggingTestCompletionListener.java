/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.test;

import com.mojang.logging.LogUtils;
import net.minecraft.test.GameTestState;
import net.minecraft.test.TestCompletionListener;
import net.minecraft.util.Util;
import org.slf4j.Logger;

public class FailureLoggingTestCompletionListener
implements TestCompletionListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onTestFailed(GameTestState test) {
        if (test.isRequired()) {
            LOGGER.error("{} failed! {}", (Object)test.getTemplatePath(), (Object)Util.getInnermostMessage(test.getThrowable()));
        } else {
            LOGGER.warn("(optional) {} failed. {}", (Object)test.getTemplatePath(), (Object)Util.getInnermostMessage(test.getThrowable()));
        }
    }

    @Override
    public void onTestPassed(GameTestState test) {
    }
}

