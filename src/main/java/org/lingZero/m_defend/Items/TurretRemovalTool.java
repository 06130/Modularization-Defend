package org.lingZero.m_defend.Items;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.lingZero.m_defend.Blocks.MultiblockFrame.AffiliateBlock;
import org.lingZero.m_defend.Blocks.MultiblockFrame.BaseTurretBlock;
import org.lingZero.m_defend.ModularizationDefend;
import org.lingZero.m_defend.Register.ModItems;
import org.lingZero.m_defend.util.DebugLogger;

import java.util.List;

@EventBusSubscriber(modid = ModularizationDefend.MODID)
public class TurretRemovalTool extends Item {
    public TurretRemovalTool(Properties properties) {
        super(properties);
    }
    
    /**
     * 添加物品悬浮提示（Tooltip）
     *
     * @param stack              物品堆栈
     * @param context            提示上下文
     * @param tooltipComponents  提示组件列表
     * @param flag               提示标志
     */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipComponents, flag);
        
        // 添加使用说明
        tooltipComponents.add(Component.translatable("tooltip.modularization_defend.turret_removal_tool.usage")
                .withStyle(style -> style.withColor(0xAAAAAA)));
    }
    
    /**
     * 处理左键点击方块事件
     * 用于拆除炮塔多方块结构
     */
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Level level = event.getLevel();
        Player player = event.getEntity();
        BlockPos clickedPos = event.getPos();
        ItemStack itemStack = player.getMainHandItem();
        
        // 检查玩家是否手持炮塔拆除工具
        if (!itemStack.is(ModItems.TURRET_REMOVAL_TOOL.get())) {
            return;
        }
        
        // 只在服务端执行逻辑
        if (!level.isClientSide && player != null) {
            // 使用重定向方法获取目标坐标
            BlockPos targetPos = AffiliateBlock.redirectIfAffiliate(level, clickedPos);
            
            // 检查目标方块是否为主方块
            if (level.getBlockState(targetPos).getBlock() instanceof BaseTurretBlock turretBlock) {
                DebugLogger.info("使用拆除工具左键主方块 at: " + targetPos);
                
                // 执行拆除逻辑
                turretBlock.dismantleStructure(level, targetPos, player);
            }
        }
    }
}
