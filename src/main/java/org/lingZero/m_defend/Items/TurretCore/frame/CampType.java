package org.lingZero.m_defend.Items.TurretCore.frame;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * 炮塔核心所属阵营类型枚举
 */
public enum CampType implements StringRepresentable {
    NONE("camp.modularization_defend.none"),      // 无，默认
    END("camp.modularization_defend.end"),        // 终末地工业
    GREGTECH("camp.modularization_defend.gregtech"),  // 格雷科技（联动）
    LATERANO("camp.modularization_defend.camp.laterano"),  //拉特兰
    YAN("camp.modularization_defend.camp.yan"),     //炎
    YAN_SUI("camp.modularization_defend.camp.sui");     //岁
    
    private final String name;
    
    CampType(String name) {
        this.name = name;
    }
    
    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }
}
