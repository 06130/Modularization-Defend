package org.lingZero.m_defend.Items.Turret;

import net.minecraft.world.level.block.Block;
import org.lingZero.m_defend.Register.ModBlocks;

/**
 * 测试炮塔1的物品
 */
public class Turret1Item extends BaseTurretItem {
    
    public Turret1Item(Properties properties) {
        super(properties);
    }
    
    @Override
    protected Block getTurretBlock() {
        return ModBlocks.TURRET1_BLOCK.get();
    }
}
