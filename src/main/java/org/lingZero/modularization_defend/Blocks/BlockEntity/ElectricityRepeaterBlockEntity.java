package org.lingZero.modularization_defend.Blocks.BlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lingZero.modularization_defend.Blocks.Multiblock.MultiblockData;

import static org.lingZero.modularization_defend.Register.ModBlockEntities.Electricity_Repeater_BLOCK_ENTITY;


public class ElectricityRepeaterBlockEntity extends BlockEntity {
    private int value;
    private MultiblockData multiblockData;
    private boolean isController = false; // 初始不是控制器，由 onPlace 设置
    private boolean isFormingMultiblock = false; // 防止递归调用的标志
    
    public ElectricityRepeaterBlockEntity(BlockPos pos, BlockState state) {
        super(Electricity_Repeater_BLOCK_ENTITY.get(), pos, state);
    }
    
    /**
     * 初始化多方块数据（只验证，不放置方块）
     */
    public void initializeMultiblock() {
        if (level != null && isController) {
            multiblockData = new MultiblockData(level, worldPosition);
            // 直接验证结构（所有方块已经放置好了）
            multiblockData.tryForm();
            
            if (multiblockData.isFormed()) {
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
            // 初始化 multiblockData
            if (multiblockData == null) {
                multiblockData = new MultiblockData(level, worldPosition);
            }
            
            // 1. 检测整个 2x2x6 区域
            if (!canFormMultiblock()) {
                // 检测失败，提示玩家
                if (player != null) {
                    String message = "§c无法放置：结构区域内有其他方块阻挡！";
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal(message), true);
                }
                return false;
            }
            
            // 2. 检测通过，一次性放置所有方块
            placeEntireStructure();
            
            // 3. 验证并注册多方块结构
            if (multiblockData != null && multiblockData.isFormed()) {
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                return true;
            }
            
            return false;
        } finally {
            isFormingMultiblock = false;
        }
    }
    
    /**
     * 检测是否可以形成多方块（不放置方块）
     */
    private boolean canFormMultiblock() {
        if (level == null) {
            return false;
        }
        
        // 检查 2x2x6 的所有位置
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 2; x++) {
                for (int z = 0; z < 2; z++) {
                    BlockPos checkPos = worldPosition.offset(x, y, z);
                    
                    // 跳过控制器位置（已经存在）
                    if (checkPos.equals(worldPosition)) {
                        continue;
                    }
                    
                    // 检查该位置是否为空或可替换
                    BlockState state = level.getBlockState(checkPos);
                    if (!level.isEmptyBlock(checkPos) && !state.canBeReplaced()) {
                        return false; // 有阻挡方块
                    }
                }
            }
        }
        
        return true;
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
            if (multiblockData == null) {
                multiblockData = new MultiblockData(level, worldPosition);
            }
            
            boolean placedAny = false;
            
            // 遍历 2x2x6 的所有位置，自动填充空位
            for (int y = 0; y < 6; y++) {
                for (int x = 0; x < 2; x++) {
                    for (int z = 0; z < 2; z++) {
                        BlockPos checkPos = worldPosition.offset(x, y, z);
                        
                        // 跳过控制器位置（已经存在）
                        if (checkPos.equals(worldPosition)) {
                            continue;
                        }
                        
                        // 检查该位置是否为空或可替换
                        if (level.isEmptyBlock(checkPos) || level.getBlockState(checkPos).canBeReplaced()) {
                            // 自动放置方块（不触发 onPlace 事件）
                            BlockState stateToPlace = getBlockState();
                            if (level.setBlock(checkPos, stateToPlace, 3)) { // 使用 flags=3 避免触发 onPlace
                                placedAny = true;
                                // 将新方块的 isController 设为 false
                                BlockEntity newBE = level.getBlockEntity(checkPos);
                                if (newBE instanceof ElectricityRepeaterBlockEntity newRepeater) {
                                    newRepeater.setController(false);
                                }
                            }
                        }
                    }
                }
            }
            
            // 放置完成后，重新检查结构
            if (placedAny || !multiblockData.isFormed()) {
                multiblockData.tryForm();
                
                if (multiblockData.isFormed()) {
                    setChanged();
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                }
            }
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
        
        // 尝试从管理器获取多方块数据
        MultiblockData dataToBreak = org.lingZero.modularization_defend.Blocks.Multiblock.MultiblockManager.getMultiblockByPosition(level, worldPosition);
        
        // 如果没有从管理器获取到，使用本地的 multiblockData
        if (dataToBreak == null) {
            dataToBreak = multiblockData;
        }
        
        if (dataToBreak != null && dataToBreak.isFormed()) {
            // 获取所有组成方块的位置（使用配置类）
            java.util.Set<BlockPos> blocks = dataToBreak.getBlocks();
            
            // 遍历并破坏所有方块（不掉落物品）
            for (BlockPos blockPos : blocks) {
                if (!blockPos.equals(worldPosition)) { // 跳过当前正在被破坏的方块
                    // 破坏方块但不掉落物品（flags=2 只发送客户端更新）
                    level.setBlock(blockPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
                }
            }
            
            // 最后破坏多方块数据
            dataToBreak.breakMultiblock();
            setChanged();
        }
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
    
    /**
     * 一次性放置整个 2x2x6 结构（在检测通过后调用）
     */
    private void placeEntireStructure() {
        if (level == null || level.isClientSide) {
            return;
        }
        
        // 设置标志，防止递归调用
        isFormingMultiblock = true;
        
        try {
            if (multiblockData == null) {
                multiblockData = new MultiblockData(level, worldPosition);
            }
            
            // 遍历 2x2x6 的所有位置，放置所有方块
            for (int y = 0; y < 6; y++) {
                for (int x = 0; x < 2; x++) {
                    for (int z = 0; z < 2; z++) {
                        BlockPos checkPos = worldPosition.offset(x, y, z);
                        
                        // 跳过控制器位置（已经存在）
                        if (checkPos.equals(worldPosition)) {
                            continue;
                        }
                        
                        // 直接放置方块（不触发 onPlace 事件）
                        BlockState stateToPlace = getBlockState();
                        level.setBlock(checkPos, stateToPlace, 3); // flags=3 避免触发 onPlace
                        
                        // 将新方块的 isController 设为 false
                        BlockEntity newBE = level.getBlockEntity(checkPos);
                        if (newBE instanceof ElectricityRepeaterBlockEntity newRepeater) {
                            newRepeater.setController(false);
                        }
                    }
                }
            }
            
            // 验证并注册多方块结构
            multiblockData.tryForm();
        } finally {
            // 重置标志
            isFormingMultiblock = false;
        }
    }
}
