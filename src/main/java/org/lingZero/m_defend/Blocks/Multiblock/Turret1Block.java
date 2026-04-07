package org.lingZero.m_defend.Blocks.Multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.lingZero.m_defend.Register.ModItems;

/**
 * 测试炮塔1的主方块
 */
public class Turret1Block extends BaseTurretBlock {
    
    public Turret1Block(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new Turret1BlockEntity(pos, state);
    }
    
    @Override
    protected Item getTurretItem() {
        return ModItems.TURRET_1_ITEM.get();
    }
}
