/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import net.minecraft.datafixer.fix.EntitySimpleTransformFix;

public class EntityZombieSplitFix
extends EntitySimpleTransformFix {
    public EntityZombieSplitFix(Schema schema, boolean bl) {
        super("EntityZombieSplitFix", schema, bl);
    }

    @Override
    protected Pair<String, Dynamic<?>> transform(String choice, Dynamic<?> dynamic) {
        if (Objects.equals("Zombie", choice)) {
            String string2 = "Zombie";
            int i = dynamic.get("ZombieType").asInt(0);
            switch (i) {
                default: {
                    break;
                }
                case 1: 
                case 2: 
                case 3: 
                case 4: 
                case 5: {
                    string2 = "ZombieVillager";
                    dynamic = dynamic.set("Profession", dynamic.createInt(i - 1));
                    break;
                }
                case 6: {
                    string2 = "Husk";
                }
            }
            dynamic = dynamic.remove("ZombieType");
            return Pair.of(string2, dynamic);
        }
        return Pair.of(choice, dynamic);
    }
}

