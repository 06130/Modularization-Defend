package org.lingZero.modularization_defend.Event;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.lingZero.modularization_defend.Blocks.Multiblock.ElectricityRepeaterConfig;
import org.lingZero.modularization_defend.Blocks.Multiblock.IMultiblockConfig;
import org.lingZero.modularization_defend.Blocks.Multiblock.MultiblockPlacer;
import org.lingZero.modularization_defend.ModularizationDefend;
import org.lingZero.modularization_defend.Register.ModBlocks;

/**
 * 多方块结构事件处理器
 * 负责处理与多方块放置相关的所有事件
 */
@EventBusSubscriber(modid = ModularizationDefend.MODID)
public class MultiblockEvents {
    
    // 电力中继器的配置实例
    private static final IMultiblockConfig ELECTRICITY_REPEATER_CONFIG = new ElectricityRepeaterConfig();
    
    /**
     * 监听玩家右键点击方块事件 - 在放置前检测多方块结构
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        // 只处理服务器端
        if (event.getLevel().isClientSide()) {
            return;
        }
            
        // 只处理玩家手持 ElectricityRepeater 的情况
        var itemStack = event.getItemStack();
        if (itemStack.getItem() == ModBlocks.ELECTRICITY_REPEATER_BLOCK.get().asItem()) {
            BlockPos pos = event.getPos();
            Level level = (Level) event.getLevel();
            Player player = event.getEntity();
                
            // 获取点击的面
            var face = event.getFace();
            if (face == null) {
                return;
            }
                
            // 计算实际放置位置（在点击方块的旁边）
            BlockPos controllerPos = pos.relative(face);
                
            // 检查以放置位置为底座的 2x2x6 区域是否有阻挡
            if (!MultiblockPlacer.canFormMultiblock(level, controllerPos, ELECTRICITY_REPEATER_CONFIG)) {
                // 有阻挡，阻止放置并显示提示
                // 完全取消事件，原版不会消耗物品
                event.setCanceled(true);
                event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
                        
                String messageKey = "message." + ModularizationDefend.MODID + ".multiblock_blocked";
                Component message = Component.translatable(messageKey);
                player.displayClientMessage(message, true);
                    
                // 重要：手动同步物品栏到客户端
                player.containerMenu.sendAllDataToRemote();
            } else {
                // 检测通过！取消原版放置逻辑，手动一次性放置所有方块
                event.setCanceled(true);
                event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
                    
                // 使用工具类放置多方块结构
                MultiblockPlacer.placeEntireMultiblock(level, controllerPos, ELECTRICITY_REPEATER_CONFIG);
                    
                // 消耗一个物品
                if (!player.isCreative()) {
                    itemStack.shrink(1);
                }
            }
        }
    }
}
