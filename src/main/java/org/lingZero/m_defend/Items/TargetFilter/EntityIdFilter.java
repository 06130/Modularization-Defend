package org.lingZero.m_defend.Items.TargetFilter;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.lingZero.m_defend.DataComponents.TargetFilterData;
import org.lingZero.m_defend.Register.ModDataComponents;
import org.lingZero.m_defend.Register.ModItems;
import org.lingZero.m_defend.util.DebugLogger;

/**
 * 实体ID过滤器物品
 * 用于筛选特定实体ID作为炮塔目标
 * 支持持久化存储实体ID
 */
public class EntityIdFilter extends BaseTargetFilter {

    public EntityIdFilter(Properties properties) {
        super(properties);
    }

    @Override
    public TargetFilterData.FilterType getFilterType() {
        return TargetFilterData.FilterType.ENTITY_ID;
    }
    
    /**
     * 实体交互事件处理器
     * 当玩家右键点击实体时，如果手持实体ID过滤器，保存该实体的ID到物品数据组件中
     */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        Entity targetEntity = event.getTarget();
        ItemStack stack = event.getItemStack();
        
        // 检查是否手持实体ID过滤器
        if (!stack.is(ModItems.ENTITY_ID_FILTER.get())) {
            return;
        }
        
        // 只在服务端执行
        if (player.level().isClientSide()) {
            return;
        }
        
        // 获取实体的资源位置ID
        String entityId = targetEntity.getType().builtInRegistryHolder().key().location().toString();
        
        // 更新物品的数据组件
        TargetFilterData newData = BaseTargetFilter.getFilterData(stack)
                .withEntityId(entityId);
        stack.set(ModDataComponents.TARGET_FILTER_DATA.get(), newData);
        
        // 发送消息给玩家
        Component message = Component.translatable("message.modularization_defend.entity_id_filter.saved", entityId)
                .withStyle(style -> style.withColor(0x00ff00));
        player.sendSystemMessage(message);
        
        DebugLogger.debug("实体ID过滤器已保存实体ID: %s", entityId);
        
        // 取消事件，防止其他交互
        event.setCanceled(true);
    }
}
