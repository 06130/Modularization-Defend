package org.lingZero.m_defend.Blocks.MultiblockFrame;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import org.lingZero.m_defend.Register.ModBlockEntities;

/**
 * 附属方块实体，用于保存主方块坐标并支持物品交互重定向
 */
public class AffiliateBlockEntity extends BlockEntity {
    
    private BlockPos mainBlockPos = BlockPos.ZERO;
    
    // 缓存的主方块物品处理器
    // 只在首次访问时获取一次
    @Nullable
    private IItemHandler cachedMainBlockHandler = null;
    private boolean cacheInitialized = false; // 标记缓存是否已初始化
    
    public AffiliateBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AFFILIATE_BLOCK_ENTITY.get(), pos, state);
    }
    
    /**
     * 从 NBT 标签中读取数据
     */
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        
        // 读取主方块坐标
        if (tag.contains("MainBlockX") && tag.contains("MainBlockY") && tag.contains("MainBlockZ")) {
            int x = tag.getInt("MainBlockX");
            int y = tag.getInt("MainBlockY");
            int z = tag.getInt("MainBlockZ");
            this.mainBlockPos = new BlockPos(x, y, z);
        }
    }
    
    /**
     * 将数据保存到 NBT 标签
     */
    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        
        // 保存主方块坐标
        tag.putInt("MainBlockX", mainBlockPos.getX());
        tag.putInt("MainBlockY", mainBlockPos.getY());
        tag.putInt("MainBlockZ", mainBlockPos.getZ());
    }
    
    /**
     * 获取主方块坐标
     */
    public BlockPos getMainBlockPos() {
        return mainBlockPos;
    }
    
    /**
     * 设置主方块坐标
     */
    public void setMainBlockPos(BlockPos pos) {
        this.mainBlockPos = pos != null ? pos : BlockPos.ZERO;
        invalidateCache(); // 清除缓存
        setChanged();
    }
    
    /**
     * 获取物品处理器能力（重定向到主方块）
     * 使用一次性缓存机制优化性能（主方块坐标不变，只需获取一次）
     *
     * @param capability 能力类型
     * @param side       方向
     * @return 能力实例
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getCapability(net.neoforged.neoforge.capabilities.BlockCapability<T, Direction> capability, @Nullable Direction side) {
        if (capability != Capabilities.ItemHandler.BLOCK) {
            return null;
        }
        
        Level level = getLevel();
        if (level == null || level.isClientSide) {
            return null;
        }
        
        // 如果缓存已初始化，直接返回（主方块坐标不变，无需重新获取）
        if (cacheInitialized) {
            return (T) cachedMainBlockHandler;
        }
        
        // 首次访问，获取并缓存主方块的物品处理器
        BlockPos actualMainPos = mainBlockPos;
        if (actualMainPos.equals(BlockPos.ZERO)) {
            // 如果主方块坐标未设置，尝试从重定向方法获取
            actualMainPos = AffiliateBlock.redirectIfAffiliate(level, getBlockPos());
            if (actualMainPos.equals(getBlockPos())) {
                cacheInitialized = true; // 标记为已初始化（即使失败）
                return null; // 无法找到主方块
            }
        }
        
        // 获取主方块的 BlockEntity
        BlockEntity mainBE = level.getBlockEntity(actualMainPos);
        if (mainBE instanceof BaseTurretBlockEntity turretBE) {
            cachedMainBlockHandler = turretBE.getCapability(Capabilities.ItemHandler.BLOCK, side);
            cacheInitialized = true; // 标记缓存已初始化
            return (T) cachedMainBlockHandler;
        }
        
        // 即使获取失败也标记为已初始化，避免重复尝试
        cacheInitialized = true;
        return null;
    }
    
    /**
     * 使缓存失效（当主方块坐标改变时调用）
     * 注：正常情况下主方块坐标不会改变，此方法保留以备特殊情况
     */
    private void invalidateCache() {
        cachedMainBlockHandler = null;
        cacheInitialized = false;
    }
}
