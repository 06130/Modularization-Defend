package org.lingZero.modularization_defend.Blocks.BlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lingZero.modularization_defend.Blocks.Multiblock.ElectricityRepeaterConfig;
import org.lingZero.modularization_defend.Blocks.Multiblock.IMultiblockComponent;
import org.lingZero.modularization_defend.Blocks.Multiblock.IMultiblockConfig;
import org.lingZero.modularization_defend.Blocks.Multiblock.MultiblockData;
import org.lingZero.modularization_defend.Blocks.Multiblock.MultiblockFormation;

import static org.lingZero.modularization_defend.Register.ModBlockEntities.Electricity_Repeater_BLOCK_ENTITY;


public class ElectricityRepeaterBlockEntity extends BlockEntity implements IMultiblockComponent {
    private int value;
    private MultiblockData multiblockData;
    private boolean isController = false; // 初始不是控制器，由 onPlace 设置
    private boolean isFormingMultiblock = false; // 防止递归调用的标志
    private static final IMultiblockConfig CONFIG = new ElectricityRepeaterConfig();
    
    public ElectricityRepeaterBlockEntity(BlockPos pos, BlockState state) {
        super(Electricity_Repeater_BLOCK_ENTITY.get(), pos, state);
    }
    
    /**
     * 设置多方块数据
     */
    @Override
    public void setMultiblockData(MultiblockData multiblockData) {
        this.multiblockData = multiblockData;
    }
    
    /**
     * 初始化多方块数据（只验证，不放置方块）
     */
    @Override
    public void initializeMultiblock() {
        if (level != null && isController) {
            // 创建并验证多方块数据
            MultiblockData data = new MultiblockData(level, worldPosition);
            if (data.tryForm()) {
                // 验证成功，保存数据
                this.multiblockData = data;
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }
    
    /**
     * 尝试形成多方块结构（Mekanism 风格）
     * 流程：1.检测 2.提示 3.放置
     * @param player 触发玩家
     * @return 是否成功形成
     */
    public boolean tryFormMultiblock(Player player) {
        if (level == null || level.isClientSide || !isController() || isFormingMultiblock) {
            return false;
        }
        
        isFormingMultiblock = true;
        try {
            // 使用 MultiblockFormation 来处理成型逻辑
            MultiblockFormation formation = new MultiblockFormation(level, worldPosition, CONFIG);
            return formation.tryForm(player);
        } finally {
            isFormingMultiblock = false;
        }
    }
    
    /**
     * 形成多方块结构（包括自动填充缺失的方块）
     */
    public void formMultiblockWithPlacement() {
        if (level == null || level.isClientSide || isFormingMultiblock) {
            return;
        }
        
        // 设置标志，防止递归调用
        isFormingMultiblock = true;
        
        try {
            // 使用 MultiblockFormation 来填充缺失的方块
            MultiblockFormation formation = new MultiblockFormation(level, worldPosition, CONFIG);
            formation.fillMissingBlocks(
                org.lingZero.modularization_defend.Register.ModBlocks.ELECTRICITY_REPEATER_BLOCK.get(),
                (be, isController) -> {
                    if (be instanceof ElectricityRepeaterBlockEntity repeater) {
                        repeater.setController(isController);
                    }
                }
            );
        } finally {
            // 重置标志
            isFormingMultiblock = false;
        }
    }
    
    /**
     * 破坏多方块结构（连锁破坏所有结构方块）
     */
    public void breakMultiblock() {
        if (level == null || level.isClientSide) {
            return;
        }
        
        // 使用 MultiblockFormation 来破坏结构，传入当前方块位置
        MultiblockFormation.breakMultiblock(level, worldPosition, multiblockData);
        setChanged();
    }
    
    /**
     * 获取多方块数据
     */
    public MultiblockData getMultiblockData() {
        return multiblockData;
    }
    
    /**
     * 检查是否为控制器
     */
    public boolean isController() {
        return isController;
    }
    
    /**
     * 设置是否为控制器
     */
    public void setController(boolean controller) {
        isController = controller;
    }
    
    /**
     * 检查多方块是否已成型
     */
    public boolean isMultiblockFormed() {
        return multiblockData != null && multiblockData.isFormed();
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.value = tag.getInt("value");
        this.isController = tag.getBoolean("isController");
        // 重新加载后重新检查多方块结构
        if (level != null && isController) {
            initializeMultiblock();
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("value", this.value);
        tag.putBoolean("isController", this.isController);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ElectricityRepeaterBlockEntity blockEntity) {
        // 在每次 tick 中执行的操作
        if (!level.isClientSide) {
            // 服务器端：定期检查多方块结构（使用 level 的 gameTime）
            // 注释掉，因为现在使用右键点击来形成多方块
            // if (level.getGameTime() % 20 == 0) { // 每 20 tick (1 秒) 检查一次
            //     blockEntity.checkAndFormMultiblock();
            // }
            
            // 在这里添加其他逻辑，例如能量传输等
        }
    }
    

}
