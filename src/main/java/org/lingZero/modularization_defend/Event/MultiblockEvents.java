package org.lingZero.modularization_defend.Event;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.lingZero.modularization_defend.ModularizationDefend;
import org.lingZero.modularization_defend.Blocks.Multiblock.newMultiblock.AbstractMultiblock;
import org.lingZero.modularization_defend.Blocks.Multiblock.newMultiblock.MultiblockBreaker;
import org.lingZero.modularization_defend.util.DebugLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * 多方块结构事件处理器
 * 负责处理与多方块放置相关的所有事件
 */
@EventBusSubscriber(modid = ModularizationDefend.MODID)
public class MultiblockEvents {
    
    // 注册的多方块定义列表
    private static final List<AbstractMultiblock> MULTIBLOCK_REGISTRY = new ArrayList<>();
    
    /**
     * 注册多方块定义到事件处理器
     * 应在模组初始化时调用
     */
    public static void registerMultiblock(AbstractMultiblock multiblock) {
        if (!MULTIBLOCK_REGISTRY.contains(multiblock)) {
            MULTIBLOCK_REGISTRY.add(multiblock);
            DebugLogger.debug("注册多方块：" + multiblock.getMultiblockId());
        }
    }
    
    /**
     * 监听玩家右键点击方块事件
     * 用于拦截并处理多方块放置
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        var hitResult = event.getHitVec();
            
        // 检查玩家手持的物品是否是多方块物品
        var itemStack = player.getItemInHand(hand);
        if (itemStack.isEmpty()) {
            return;
        }
            
        DebugLogger.debug("===== MultiblockEvents.onRightClickBlock 被调用 =====");
        DebugLogger.debug("手持物品：" + itemStack.getItem().getName(itemStack).getString());
            
        // 遍历所有注册的多方块，查找匹配的
        for (AbstractMultiblock multiblock : MULTIBLOCK_REGISTRY) {
            if (itemStack.getItem() == multiblock.getMasterItem()) {
                DebugLogger.info("检测到多方块物品 [" + multiblock.getMultiblockId() + "],尝试放置");
                    
                // 调用放置逻辑
                var result = multiblock.onRightClickPlace(
                    event.getLevel(), 
                    player, 
                    hand, 
                    hitResult
                );
                    
                DebugLogger.info("放置结果：" + result);
                    
                // 如果放置成功，取消原版事件
                if (result.consumesAction()) {
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                    DebugLogger.info("放置成功，已阻止原版放置事件");
                }
                    
                // 已处理，退出循环
                break;
            }
        }
    }
        
    /**
     * 监听方块破坏事件
     * 用于在破坏多方块结构的任意方块时触发连锁破坏
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        var levelAccessor = event.getLevel();
        var pos = event.getPos();
        var player = event.getPlayer();
            
        DebugLogger.debug("===== MultiblockEvents.onBlockBreak 被调用 =====");
        DebugLogger.debug("破坏位置：" + pos);
        
        // LevelAccessor 需要转换为 Level
        if (!(levelAccessor instanceof net.minecraft.world.level.Level level)) {
            DebugLogger.debug("无法转换为 Level，跳过");
            return;
        }
            
        // 只在服务器端处理
        if (level.isClientSide) {
            return;
        }
            
        // 如果正在破坏多方块结构中，跳过此次事件（防止递归触发）
        if (MultiblockBreaker.isBreakingMultiblock()) {
            DebugLogger.debug("正在破坏多方块结构中，跳过此次事件");
            return;
        }
            
        // 遍历所有注册的多方块，检查是否属于某个多方块结构
        for (AbstractMultiblock multiblock : MULTIBLOCK_REGISTRY) {
            // 使用 MultiblockBreaker 检查是否为结构方块并处理连锁破坏
            if (multiblock.onBreak(level, pos, player)) {
                DebugLogger.info("触发了多方块 [" + multiblock.getMultiblockId() + "] 的连锁破坏");
                break;
            }
        }
    }
}
