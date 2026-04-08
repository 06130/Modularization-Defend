package org.lingZero.m_defend.Blocks.Multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.lingZero.m_defend.Blocks.MultiblockFrame.BaseTurretBlock;
import org.lingZero.m_defend.DataComponents.TurretType;
import org.lingZero.m_defend.Register.ModItems;

/**
 * 测试炮塔1的主方块
 */
public class Turret1Block extends BaseTurretBlock {

    public Turret1Block(Properties properties) {
        super(properties);
    }

    public int getStructureWidth() {
        return super.getStructureWidth();
    }

    public int getStructureHeight() {
        return super.getStructureHeight();
    }



    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new Turret1BlockEntity(pos, state);
    }


    /**
     * 获取多方块对应物品
     *
     * @return Item
     */
    @Override
    protected Item getTurretItem() {
        return ModItems.TURRET_1_ITEM.get();
    }
    
    /**
     * 获取炮塔类型
     *
     * @return 炮塔类型枚举
     */
    @Override
    public TurretType getTurretType() {
        return TurretType.LASER; // 测试炮塔1为激光炮塔
    }
}
