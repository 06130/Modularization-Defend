package org.lingZero.modularization_defend.Item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.lingZero.modularization_defend.DataComponents.ModDataComponents;

import java.util.List;

/**
 * 玩家ID卡——潜行+右键：若为空则写入持有者名称和UUID；若已有数据则清除。
 */
public class PlayerIdCardItem extends Item {

    public PlayerIdCardItem(Item.Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    /**
     * 潜行+右键空气：
     * <ul>
     *   <li>卡为空 → 写入当前玩家的名称和 UUID</li>
     *   <li>卡有数据 → 清除原有数据</li>
     * </ul>
     */
    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            return super.use(level, player, hand);
        }
        if (!level.isClientSide) {
            if (stack.has(ModDataComponents.STORED_PLAYER_NAME.get())) {
                // 已有数据 → 清除
                stack.remove(ModDataComponents.STORED_PLAYER_NAME.get());
                stack.remove(ModDataComponents.STORED_PLAYER_UUID.get());
                player.sendSystemMessage(Component.translatable(
                        "item.modularization_defend.player_id_card.cleared"));
            } else {
                // 空卡 → 写入
                stack.set(ModDataComponents.STORED_PLAYER_NAME.get(), player.getName().getString());
                stack.set(ModDataComponents.STORED_PLAYER_UUID.get(), player.getUUID().toString());
                player.sendSystemMessage(Component.translatable(
                        "item.modularization_defend.player_id_card.stored"));
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (!stack.has(ModDataComponents.STORED_PLAYER_NAME.get())) {
            tooltip.add(Component.translatable("item.modularization_defend.player_id_card.empty"));
            return;
        }
        String name = stack.get(ModDataComponents.STORED_PLAYER_NAME.get());
        String uuid = stack.get(ModDataComponents.STORED_PLAYER_UUID.get());
        if (name != null) {
            tooltip.add(Component.literal("§7玩家: §f" + name));
        }
        if (uuid != null) {
            tooltip.add(Component.literal("§7UUID: §8" + uuid));
        }
    }
}
