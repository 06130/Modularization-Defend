package org.lingZero.modularization_defend.blocks.EntityBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.lingZero.modularization_defend.blocks.BlockEntity.ElectricityRepeaterBlockEntity;

import javax.annotation.Nullable;

import static org.lingZero.modularization_defend.register.ModBlockEntities.Electricity_Repeater_BLOCK_ENTITY;

public class ElectricityRepeaterEntityBlock extends Block implements EntityBlock {
    // 构造函数，委托给父类
    public ElectricityRepeaterEntityBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElectricityRepeaterBlockEntity(pos, state);
    }

    // 其他内容在此
    // 由于泛型转换，我们在这里使用第二个方法
    // 如果继承 `BaseEntityBlock`，该方法在那里也可作为受保护的静态方法使用
    private static <E extends BlockEntity, A extends BlockEntity> @Nullable BlockEntityTicker<A> createTickerHelper(
            BlockEntityType<A> type, BlockEntityType<E> checkedType, BlockEntityTicker<? super E> ticker
    ) {
        return checkedType == type ? (BlockEntityTicker<A>) ticker : null;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // 你可以在此根据不同的因素返回不同的 tickers。一个常见的用例是
        // 在客户端或服务器端返回不同的 tickers，仅在一侧开始 tick，
        // 或者仅为某些 blockstates 返回 ticker（例如使用"我的机器正在工作"的 blockstate 属性时）。
        return createTickerHelper(type, Electricity_Repeater_BLOCK_ENTITY.get(), ElectricityRepeaterBlockEntity::tick);
    }
}
