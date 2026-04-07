package org.lingZero.m_defend.Event;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.lingZero.m_defend.ModularizationDefend;
import org.lingZero.m_defend.Register.ModItems;
import org.lingZero.m_defend.util.DebugLogger;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * 玩家右键物品事件监听器
 * 当玩家手持特定物品按下右键时触发
 */
@EventBusSubscriber(modid = ModularizationDefend.MODID)
public class RightClickItemHandler {
    
    /**
     * 处理玩家右键物品事件
     * 当玩家手持物品右键时触发
     * 
     * @param event 右键物品事件
     */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack itemStack = event.getItemStack();
        
        // 检查是否为客户端（避免重复执行）
        if (player.level().isClientSide) {
            return;
        }
        
        // 检查玩家手持的物品是否为目标物品
        // 示例：检查是否为 defend_core
        if (itemStack.is(ModItems.DEFEND_CORE.get())) {
            handleDefendCoreRightClick(player, hand, itemStack);
            // 如果需要取消默认行为，可以取消事件
            // event.setCanceled(true);
        }
        
        // 可以继续添加其他物品的判断
        // else if (itemStack.is(ModItems.OTHER_ITEM.get())) {
        //     handleOtherItemRightClick(player, hand, itemStack);
        // }
    }
    
    /**
     * 处理 DefendCore 右键点击
     * 
     * @param player 玩家实例
     * @param hand 使用的手
     * @param itemStack 物品堆栈
     */
    private static void handleDefendCoreRightClick(Player player, InteractionHand hand, ItemStack itemStack) {
        DebugLogger.info("玩家 " + player.getName().getString() + " 右键点击了 DefendCore");
        
        // 在这里添加你的逻辑
        // 例如：打开GUI、执行特殊功能等
        
        // 示例：向玩家发送消息
        // player.sendSystemMessage(Component.literal("你右键点击了 DefendCore！"));
    }
}
