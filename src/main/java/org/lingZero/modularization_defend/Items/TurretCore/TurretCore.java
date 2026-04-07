package org.lingZero.modularization_defend.Items.TurretCore;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lingZero.modularization_defend.DataComponents.TurretCoreData;
import org.lingZero.modularization_defend.Register.ModDataComponents;

public class TurretCore extends Item {

    public TurretCore(Properties properties) {
        super(properties);
    }

    public static @NotNull TurretCoreData getData(@NotNull ItemStack stack) {
        TurretCoreData data = stack.get(ModDataComponents.TURRET_CORE_DATA.get());
        return data != null ? data : TurretCoreData.createDefault();
    }
}
