package org.lingZero.m_defend.Blocks.Multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.lingZero.m_defend.Blocks.MultiblockFrame.BaseTurretBlock;
import org.lingZero.m_defend.DataComponents.TurretType;
import org.lingZero.m_defend.Register.ModBlockEntities;
import org.lingZero.m_defend.Register.ModItems;

/**
 * 基础子弹炮塔V1的主方块
 */
public class BasicBulletTurretV1Block extends BaseTurretBlock {

    public BasicBulletTurretV1Block(Properties properties) {
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
        org.lingZero.m_defend.util.DebugLogger.info("创建 BasicBulletTurretV1BlockEntity at: %s", pos);
        return new BasicBulletTurretV1BlockEntity(pos, state);
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            net.minecraft.world.level.Level pLevel,
            BlockState pState,
            BlockEntityType<T> pBlockEntityType) {
        // 只在服务端注册 tick
        if (pLevel.isClientSide) {
            return null;
        }
        // 检查 BlockEntityType 是否匹配
        if (pBlockEntityType == ModBlockEntities.BASIC_BULLET_TURRET_V1_BLOCK_ENTITY.get()) {
            return (BlockEntityTicker<T>) (level, pos, state, blockEntity) -> {
                if (blockEntity instanceof BasicBulletTurretV1BlockEntity turret) {
                    turret.tick();
                }
            };
        }
        return null;
    }


    /**
     * 获取多方块对应物品
     *
     * @return Item
     */
    @Override
    protected Item getTurretItem() {
        return ModItems.BASIC_BULLET_TURRET_V1.get();
    }
    
    /**
     * 获取炮塔类型
     *
     * @return 炮塔类型枚举
     */
    public TurretType getTurretType() {
        return TurretType.LASER; // 基础子弹炮塔V1为激光炮塔
    }
}
