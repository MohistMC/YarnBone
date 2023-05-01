/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.screen;

import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.text.Text;

public interface NamedScreenHandlerFactory
extends ScreenHandlerFactory {
    public Text getDisplayName();
}

