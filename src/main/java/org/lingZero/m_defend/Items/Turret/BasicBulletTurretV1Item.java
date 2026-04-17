package org.lingZero.m_defend.Items.Turret;

import net.minecraft.world.level.block.Block;
import org.lingZero.m_defend.Register.ModBlocks;

/**
 * 一级实弹炮塔物品
 */
public class BasicBulletTurretV1Item extends BaseTurretItem {
    
    public BasicBulletTurretV1Item(Properties properties) {
        super(properties);
    }
    
    @Override
    protected Block getTurretBlock() {
        return ModBlocks.BASIC_BULLET_TURRET_V1_BLOCK.get();
    }
}
