package org.lingZero.m_defend.Items;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.lingZero.m_defend.Blocks.Multiblock.AffiliateBlock;
import org.lingZero.m_defend.Blocks.Multiblock.BaseTurretBlock;
import org.lingZero.m_defend.util.DebugLogger;

public class TurretRemovalTool extends Item {
    public TurretRemovalTool(Properties properties) {
        super(properties);
    }
    
    /**
     * 右键使用物品时触发
     * 用于拆除炮塔多方块结构
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos clickedPos = context.getClickedPos();
        ItemStack itemStack = context.getItemInHand();
        
        if (!level.isClientSide && player != null) {
            // 使用重定向方法获取目标坐标
            BlockPos targetPos = AffiliateBlock.redirectIfAffiliate(level, clickedPos);
            
            // 检查目标方块是否为主方块
            if (level.getBlockState(targetPos).getBlock() instanceof BaseTurretBlock turretBlock) {
                DebugLogger.info("使用拆除工具右键主方块 at: " + targetPos);
                
                // 执行拆除逻辑
                turretBlock.dismantleStructure(level, targetPos, player);
                
                return InteractionResult.SUCCESS;
            } else {
                DebugLogger.warn("右键的不是主方块，无法拆除");
            }
        }
        
        return InteractionResult.PASS;
    }
}
