package org.lingZero.m_defend.Blocks.Multiblock;


import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.lingZero.m_defend.Register.ModBlocks;

/**
 * 附属方块，用于多方块结构中的附属部分
 */
public class AffiliateBlock extends Block implements EntityBlock {
    
    public AffiliateBlock(Properties properties) {
        super(properties);
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AffiliateBlockEntity(pos, state);
    }
    
    /**
     * 设置方块的渲染层为透明
     */
    @OnlyIn(Dist.CLIENT)
    public static void initClient() {
        net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.AFFILIATE_BLOCK.get(), RenderType.translucent());
    }
}
