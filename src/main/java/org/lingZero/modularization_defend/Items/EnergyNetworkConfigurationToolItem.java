package org.lingZero.modularization_defend.Items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

/**
 * 电网配置工具 - 左键点击时记录坐标并计算距离
 */
public class EnergyNetworkConfigurationToolItem extends Item {

    public EnergyNetworkConfigurationToolItem(Properties properties) {
        super(properties);
    }

    /**
     * 监测玩家手持此物品时的左键点击
     * 该方法会在事件处理器中被调用
     * @param player 玩家
     * @param hand 使用的手
     * @param firstPos 第一次点击的方块坐标，如果为 null 则不显示
     * @param secondPos 第二次点击的方块坐标，如果为 null 则只显示第一次点击
     */
    public void onLeftClick(Player player, InteractionHand hand, net.minecraft.core.BlockPos firstPos, net.minecraft.core.BlockPos secondPos) {
        if (!player.level().isClientSide) {
            return;
        }
        
        if (firstPos != null) {
            if (secondPos != null) {
                // 两次点击，计算距离并显示第二次坐标
                double distance = calculateDistance(firstPos, secondPos);
                net.minecraft.network.chat.Component message = net.minecraft.network.chat.Component.translatable(
                    "message.modularization_defend.block_distance",
                    secondPos.getX(), secondPos.getY(), secondPos.getZ(),
                    String.format("%.2f", distance)
                );
                player.sendSystemMessage(message);
                
                // 完成后清除坐标数据
                org.lingZero.modularization_defend.Events.EnergyNetworkConfigurationToolLinkEvents.clearClickedPositions();
            } else {
                // 只有第一次点击
                net.minecraft.network.chat.Component message = net.minecraft.network.chat.Component.translatable(
                    "message.modularization_defend.first_click",
                    firstPos.getX(), firstPos.getY(), firstPos.getZ()
                );
                player.sendSystemMessage(message);
            }
        }
    }
    
    /**
     * 计算两个坐标之间的直线距离
     * @param pos1 第一个坐标
     * @param pos2 第二个坐标
     * @return 直线距离
     */
    private double calculateDistance(net.minecraft.core.BlockPos pos1, net.minecraft.core.BlockPos pos2) {
        int dx = pos2.getX() - pos1.getX();
        int dy = pos2.getY() - pos1.getY();
        int dz = pos2.getZ() - pos1.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
