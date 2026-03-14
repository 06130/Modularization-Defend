package org.lingZero.modularization_defend.Events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent.MouseButton;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.lingZero.modularization_defend.Items.EnergyNetworkConfigurationToolItem;
import org.lingZero.modularization_defend.ModularizationDefend;
import org.lingZero.modularization_defend.Register.ModBlocks;
import org.lingZero.modularization_defend.Register.ModItems;


@EventBusSubscriber(modid = ModularizationDefend.MODID, value = Dist.CLIENT)
public class EnergyNetworkConfigurationToolLinkEvents {

    // 鼠标按下状态标记，防止长按连续触发
    private static boolean isMouseDown = false;

    // 第一次点击的方块坐标
    public static BlockPos firstClickedBlockPos = null;

    // 第二次点击的方块坐标
    public static BlockPos secondClickedBlockPos = null;

    // 上次显示距离的玩家坐标（用于减少重复消息）
    private static BlockPos lastDisplayedPlayerPos = null;

    /**
     * 监听玩家左键点击方块事件
     */
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (player == null || !player.level().isClientSide) {
            return;
        }

        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();

        // 检查连接完整性：已有第一次点击但不再持有工具
        if (hasIncompleteConnection() && !isHoldingTool(mainHandItem, offHandItem)) {
            clearClickedPositions();
            lastDisplayedPlayerPos = null;
            sendSystemMessage(player, "message.modularization_defend.connection_broken");
            return;
        }

        // 必须手持电网配置工具
        if (!isHoldingTool(mainHandItem, offHandItem)) {
            return;
        }

        // 必须点击中继塔方块
        if (!isRepeaterBlock(event.getLevel().getBlockState(event.getPos()))) {
            return;
        }

        // 防抖处理：鼠标已按下则忽略
        if (isMouseDown) {
            return;
        }

        isMouseDown = true;
        BlockPos currentPos = event.getPos().immutable();

        // 记录点击坐标
        if (firstClickedBlockPos == null) {
            firstClickedBlockPos = currentPos;
            lastDisplayedPlayerPos = null; // 重置上次显示位置
            // 第一次点击显示在动作栏
            sendActionBarMessage(player, "message.modularization_defend.first_click",
                firstClickedBlockPos.getX(), firstClickedBlockPos.getY(), firstClickedBlockPos.getZ());
        } else {
            secondClickedBlockPos = currentPos;
        }

        // 调用物品处理方法
        invokeItemClick(player, mainHandItem, offHandItem);
    }

    /**
     * 监听鼠标释放事件
     */
    @SubscribeEvent
    public static void onMouseRelease(MouseButton.Post event) {
        if (event.getButton() == 0) { // 左键
            isMouseDown = false;
        }
    }

    /**
     * 监听玩家刻事件，实时更新距离显示
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide) {
            return;
        }

        // 只有在未完成连接且手持工具时才显示
        if (!hasIncompleteConnection()) {
            return;
        }

        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();
        if (!isHoldingTool(mainHandItem, offHandItem)) {
            return;
        }

        // 获取玩家当前位置（取整到方块坐标）
        BlockPos playerPos = player.blockPosition();

        // 如果位置没有变化，不更新（减少聊天框刷屏）
        if (lastDisplayedPlayerPos != null && lastDisplayedPlayerPos.equals(playerPos)) {
            return;
        }

        // 计算玩家到第一次点击方块的距离
        double distance = calculateDistance(playerPos, firstClickedBlockPos);
                
        // 在动作栏显示距离消息
        sendActionBarMessage(player, "message.modularization_defend.player_distance",
            String.format("%.2f", distance),
            playerPos.getX(), playerPos.getY(), playerPos.getZ(),
            firstClickedBlockPos.getX(), firstClickedBlockPos.getY(), firstClickedBlockPos.getZ());
                
        // 更新上次显示位置
        lastDisplayedPlayerPos = playerPos;
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
        lastDisplayedPlayerPos = null;
    }
    
    /**
     * 检查是否存在未完成的连接（仅有第一次点击）
     */
    private static boolean hasIncompleteConnection() {
        return firstClickedBlockPos != null && secondClickedBlockPos == null;
    }
    
    /**
     * 检查是否手持电网配置工具
     */
    private static boolean isHoldingTool(ItemStack mainHand, ItemStack offHand) {
        return isEnergyNetworkConfigurationTool(mainHand) || 
               isEnergyNetworkConfigurationTool(offHand);
    }
    
    /**
     * 判断物品是否为电网配置工具
     */
    private static boolean isEnergyNetworkConfigurationTool(ItemStack itemStack) {
        return itemStack != null && itemStack.getItem() == ModItems.ENERGY_NETWORK_CONFIGURATION_TOOL_ITEM.get();
    }
    
    /**
     * 检查方块是否为中继塔
     */
    private static boolean isRepeaterBlock(net.minecraft.world.level.block.state.BlockState state) {
        return state.getBlock() == ModBlocks.ELECTRICITY_REPEATER_BLOCK.get();
    }
    
    /**
     * 发送带参数的系统消息给玩家（聊天栏）
     */
    private static void sendSystemMessage(Player player, String translationKey, Object... args) {
        player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(translationKey, args));
    }
    
    /**
     * 发送带参数的动作栏消息给玩家
     */
    private static void sendActionBarMessage(Player player, String translationKey, Object... args) {
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable(translationKey, args), true);
    }
    
    /**
     * 计算两个坐标之间的直线距离
     */
    private static double calculateDistance(BlockPos pos1, BlockPos pos2) {
        int dx = pos2.getX() - pos1.getX();
        int dy = pos2.getY() - pos1.getY();
        int dz = pos2.getZ() - pos1.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    
    /**
     * 调用物品的点击处理方法
     */
    private static void invokeItemClick(Player player, ItemStack mainHand, ItemStack offHand) {
        if (mainHand.getItem() instanceof EnergyNetworkConfigurationToolItem tool) {
            tool.onLeftClick(player, net.minecraft.world.InteractionHand.MAIN_HAND, 
                           firstClickedBlockPos, secondClickedBlockPos);
        } else if (offHand.getItem() instanceof EnergyNetworkConfigurationToolItem tool) {
            tool.onLeftClick(player, net.minecraft.world.InteractionHand.OFF_HAND, 
                           firstClickedBlockPos, secondClickedBlockPos);
        }
    }
}
