package org.lingZero.modularization_defend.Events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent.MouseButton;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.lingZero.modularization_defend.Items.EnergyNetworkConfigurationToolItem;
import org.lingZero.modularization_defend.ModularizationDefend;
import org.lingZero.modularization_defend.Register.ModBlocks;
import org.lingZero.modularization_defend.Register.ModItems;


@EventBusSubscriber(modid = ModularizationDefend.MODID, value = Dist.CLIENT)
public class EnergyNetworkConfigurationToolLinkEvents {
    
    // 标记鼠标是否已按下（用于防止长按连续触发）
    private static boolean isMouseDown = false;
    
    // 记录第一次点击的目标方块坐标
    public static BlockPos firstClickedBlockPos = null;
    
    // 记录第二次点击的目标方块坐标
    public static BlockPos secondClickedBlockPos = null;
    
    /**
     * 监听玩家左键点击方块事件
     * 当玩家手持 energy_network_configuration_tool_item 左键点击 electricity_repeater_block 时，记录坐标并计算距离
     */
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (player == null || !player.level().isClientSide()) {
            return; // 仅在客户端执行
        }
        
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();
        
        // 检查是否已经记录了第一次点击，但当前没有手持工具
        if (firstClickedBlockPos != null && secondClickedBlockPos == null) {
            // 已经有第一次点击，检查是否还持有工具
            if (!isEnergyNetworkConfigurationTool(mainHandItem) && 
                !isEnergyNetworkConfigurationTool(offHandItem)) {
                // 没有手持工具，清空连接
                clearClickedPositions();
                net.minecraft.network.chat.Component message = net.minecraft.network.chat.Component.translatable(
                    "message.modularization_defend.connection_broken"
                );
                player.sendSystemMessage(message);
                return;
            }
        }
        
        // 检查主手或副手是否持有电网配置工具
        if (isEnergyNetworkConfigurationTool(mainHandItem) || 
            isEnergyNetworkConfigurationTool(offHandItem)) {
            // 检查点击的方块是否为中继塔方块
            if (event.getLevel().getBlockState(event.getPos()).getBlock() == ModBlocks.ELECTRICITY_REPEATER_BLOCK.get()) {
                // 如果鼠标已经按下，则不触发（防止长按连续触发）
                if (isMouseDown) {
                    return;
                }
                
                // 标记为已按下
                isMouseDown = true;
                
                // 记录点击的方块坐标
                BlockPos currentPos = event.getPos().immutable();
                
                // 判断是第几次点击
                if (firstClickedBlockPos == null) {
                    // 第一次点击
                    firstClickedBlockPos = currentPos;
                } else {
                    // 第二次点击
                    secondClickedBlockPos = currentPos;
                }
                
                // 调用物品的方法
                if (mainHandItem.getItem() instanceof EnergyNetworkConfigurationToolItem) {
                    ((EnergyNetworkConfigurationToolItem) mainHandItem.getItem()).onLeftClick(player, net.minecraft.world.InteractionHand.MAIN_HAND, firstClickedBlockPos, secondClickedBlockPos);
                } else if (offHandItem.getItem() instanceof EnergyNetworkConfigurationToolItem) {
                    ((EnergyNetworkConfigurationToolItem) offHandItem.getItem()).onLeftClick(player, net.minecraft.world.InteractionHand.OFF_HAND, firstClickedBlockPos, secondClickedBlockPos);
                }
            }
        }
    }
    
    /**
     * 监听鼠标释放事件，重置按下状态
     */
    @SubscribeEvent
    public static void onMouseRelease(MouseButton.Post event) {
        // 当鼠标按钮释放时，重置状态
        if (event.getButton() == 0) { // 0 = 左键
            isMouseDown = false;
        }
    }
    
    /**
     * 获取第一次点击的方块坐标
     * @return 方块坐标，如果未点击过则返回 null
     */
    public static BlockPos getFirstClickedBlockPos() {
        return firstClickedBlockPos;
    }
    
    /**
     * 获取第二次点击的方块坐标
     * @return 方块坐标，如果未点击过则返回 null
     */
    public static BlockPos getSecondClickedBlockPos() {
        return secondClickedBlockPos;
    }
    
    /**
     * 清空点击记录
     */
    public static void clearClickedPositions() {
        firstClickedBlockPos = null;
        secondClickedBlockPos = null;
    }
    
    /**
     * 判断物品是否为电网配置工具
     */
    private static boolean isEnergyNetworkConfigurationTool(ItemStack itemStack) {
        return itemStack.getItem() == ModItems.ENERGY_NETWORK_CONFIGURATION_TOOL_ITEM.get();
    }
}
