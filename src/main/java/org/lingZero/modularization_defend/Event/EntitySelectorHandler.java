package org.lingZero.modularization_defend.Event;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.lingZero.modularization_defend.DataComponents.ModDataComponents;
import org.lingZero.modularization_defend.Item.EntitySelectorItem;

/**
 * 处理实体选取器的右键捕获逻辑。
 * <p>
 * 使用 NeoForge 的 PlayerInteractEvent.EntityInteract 事件而非
 * Item.interactLivingEntity()，因为后者在某些实体（马、村民等）上
 * 会被实体的 interact 方法抢先处理，导致潜行状态下也无法触发。
 */
public class EntitySelectorHandler {

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        Entity target = event.getTarget();
        InteractionHand hand = event.getHand();
        ItemStack stack = player.getItemInHand(hand);

        if (!(stack.getItem() instanceof EntitySelectorItem)) {
            return;
        }
        if (!player.isShiftKeyDown()) {
            return; // 不潜行时放行，让实体交互优先
        }

        // 仅服务端：存储实体 ID 并取消事件（阻止实体自身的交互）
        // 关键：不在客户端取消，否则 ServerboundInteractPacket 不会发送到服务端
        if (!player.level().isClientSide) {
            ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
            stack.set(ModDataComponents.STORED_ENTITY_ID.get(), entityId);
            player.sendSystemMessage(Component.translatable(
                    "item.modularization_defend.entity_selector.captured",
                    entityId.toString()));
            event.setCanceled(true); // 仅服务端取消，让客户端照常发数据包
        }
    }
}
