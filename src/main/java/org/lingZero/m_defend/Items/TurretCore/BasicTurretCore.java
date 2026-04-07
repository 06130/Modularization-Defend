package org.lingZero.m_defend.Items.TurretCore;

import org.lingZero.m_defend.Items.TurretCore.frame.CampType;
import org.lingZero.m_defend.Items.TurretCore.frame.TurretCore;

/**
 * 基础炮塔核心物品实现类
 */
public class BasicTurretCore extends TurretCore {


    /**
     * 获取该炮塔核心的阵营类型
     * @return 阵营类型
     */
    @Override
    public CampType getCampType() {
        return CampType.END;
    }
    
    public BasicTurretCore(Properties properties) {
        super(properties);
    }
}
