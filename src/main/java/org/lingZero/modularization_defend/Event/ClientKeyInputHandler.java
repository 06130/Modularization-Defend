package org.lingZero.modularization_defend.Event;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lingZero.modularization_defend.Items.DefendCore;
import org.lingZero.modularization_defend.ModularizationDefend;
import org.lingZero.modularization_defend.Register.ModKeyBindings;
import org.lingZero.modularization_defend.util.DebugLogger;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * 客户端按键事件处理器
 * 负责检测按键按下并触发对应功能
 */
@EventBusSubscriber(modid = ModularizationDefend.MODID, value = Dist.CLIENT)
public class ClientKeyInputHandler {
    
    /**
     * 用于跟踪按键状态，防止长按时重复触发
     */
    private static boolean wasKeyDown = false;
    
    /**
     * 客户端刻事件
     * 在每次客户端刻时调用，用于检测按键按下
     * 
     * @param event 客户端刻事件
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        
        // 检查按键是否被按下
        boolean isKeyDown = ModKeyBindings.openTurretGuiKey.isDown();
        
        // 检测按键按下的瞬间（从松开到按下）
        if (isKeyDown && !wasKeyDown) {
            handleKeyPress(player);
        }
        
        wasKeyDown = isKeyDown;
    }
    
    /**
     * 处理按键按下事件
     * 
     * @param player 玩家实例
     */
    private static void handleKeyPress(Player player) {
        DebugLogger.debug("检测到按键按下，检查 Curios 装备");
        
        // 检查玩家是否在 Curios 槽位装备了 DefendCore
        CuriosApi.getCuriosHelper().findEquippedCurio(item -> item.getItem() instanceof DefendCore, player).ifPresent(pair -> {
            ItemStack stack = pair.getRight();
            
            DebugLogger.info("检测到 DefendCore 装备，触发功能");
            
            // TODO: 在这里实现打开 GUI 或其他功能
            // 示例：打开炮塔配置 GUI
            openTurretGui(player, stack);
        });
    }
    
    /**
     * 打开炮塔配置 GUI
     * 
     * @param player 玩家
     * @param stack 防御核心物品栈
     */
    private static void openTurretGui(Player player, ItemStack stack) {
        // 客户端请求打开 GUI
        if (player.level().isClientSide) {
            DebugLogger.info("Requesting to open DefendCore GUI from client");
            // 使用 ApricityUI 的网络处理器请求打开 UI
            com.sighs.apricityui.instance.network.handler.ApricityScreenNetworkHandler.requestOpenScreen(
                "modularization_defend:Item/CurioItem/DefendCoreUI/index.html"
            );
        }
    }
}
