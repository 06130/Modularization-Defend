package org.lingZero.m_defend.Blocks.Multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lingZero.m_defend.Register.ModBlockEntities;

/**
 * 附属方块实体，用于保存主方块坐标
 */
public class AffiliateBlockEntity extends BlockEntity {
    
    private BlockPos mainBlockPos = BlockPos.ZERO;
    
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
        setChanged();
    }
}
