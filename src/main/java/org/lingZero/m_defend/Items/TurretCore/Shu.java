package org.lingZero.m_defend.Items.TurretCore;

import org.lingZero.m_defend.Items.TurretCore.frame.CampType;
import org.lingZero.m_defend.Items.TurretCore.frame.TurretCore;

public class Shu extends TurretCore {

    @Override
    public CampType getCampType() {
        return CampType.YAN_SUI;
    }
    public Shu(Properties properties) {
        super(properties);
    }

}
