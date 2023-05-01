/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.advancement;

import java.util.Collection;

public interface CriterionMerger {
    public static final CriterionMerger AND = criteriaNames -> {
        String[][] strings = new String[criteriaNames.size()][];
        int i = 0;
        for (String string : criteriaNames) {
            strings[i++] = new String[]{string};
        }
        return strings;
    };
    public static final CriterionMerger OR = criteriaNames -> new String[][]{criteriaNames.toArray(new String[0])};

    public String[][] createRequirements(Collection<String> var1);
}

