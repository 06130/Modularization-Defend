package org.lingZero.modularization_defend.Block.example;

import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lingZero.modularization_defend.Block.ModBlockEntities;

/**
 * 关卡编辑器方块——放置后在其上方创建 15x15x15 的可建造区域。
 * 区域边界通过客户端 BlockEntityRenderer 以半透明线框渲染，内部为自由建造空间。
 */
public class LevelEditorBlock extends Block implements EntityBlock, BlockUIMenuType.BlockUI {

    public LevelEditorBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.5F, 3600000.0F)
                .noOcclusion());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LevelEditorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        if (type == ModBlockEntities.LEVEL_EDITOR.get()) {
            @SuppressWarnings("unchecked")
            BlockEntityTicker<T> ticker = (BlockEntityTicker<T>) (level1, pos1, state1, be) ->
                    LevelEditorBlockEntity.serverTick(level1, pos1, state1, (LevelEditorBlockEntity) be);
            return ticker;
        }
        return null;
    }

    // ==================== LDLib2 BlockUI ====================

    @NotNull
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            BlockUIMenuType.openUI(sp, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public ModularUI createUI(BlockUIMenuType.BlockUIHolder holder) {
        return LevelEditorUI.createUI(holder);
    }
}
