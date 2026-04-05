package org.lingZero.modularization_defend.Event;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lingZero.modularization_defend.Items.DefendCore;
import org.lingZero.modularization_defend.ModularizationDefend;
import org.lingZero.modularization_defend.Register.ModKeyBindings;
import org.lingZero.modularization_defend.ldlibUI.NewUIScreen;
import top.theillusivec4.curios.api.CuriosApi;

import static org.lingZero.modularization_defend.ldlibUI.DefendCoreGUI.createModularUI;

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

        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            var curios = handler.findCurios(item -> item.getItem() instanceof DefendCore);
            if (!curios.isEmpty()) {
                var stack = curios.getFirst().stack();
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.setScreen(new NewUIScreen(createModularUI(player)));
            }
        });
    }
}
