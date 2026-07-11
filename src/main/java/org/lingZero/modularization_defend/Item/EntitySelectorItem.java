package org.lingZero.modularization_defend.Item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.lingZero.modularization_defend.DataComponents.ModDataComponents;

import java.util.List;
import java.util.Optional;

/**
 * 实体选取器——潜行+右键实体时，将该实体的类型 ID 存储到物品中。
 * <p>
 * 参考 Mekanism 的 ItemDictionary（interactLivingEntity）和
 * ItemConfigurationCard（DataComponent 存储）的实现模式。
 * <p>
 * 操作：
 * <ul>
 *   <li>潜行 + 右键实体 → 存储实体类型 ID</li>
 *   <li>右键方块 → 清除已存储的 ID</li>
 *   <li>光标悬停 → 查看已存储的实体 ID</li>
 * </ul>
 */
public class EntitySelectorItem extends Item {

    public EntitySelectorItem(Item.Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    /**
     * 潜行+右键实体：存储该实体的类型 ID。
     * 普通右键（不潜行）返回 PASS，让实体自身的交互（如骑马、交易）优先处理。
     */
    @NotNull
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS; // 不潜行时放行，让实体交互优先
        }
        Level level = player.level();
        if (!level.isClientSide) {
            ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            stack.set(ModDataComponents.STORED_ENTITY_ID.get(), entityId);
            player.sendSystemMessage(Component.translatable(
                    "item.modularization_defend.entity_selector.captured",
                    entityId.toString()));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * 右键方块：清除已存储的实体 ID。
     */
    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        if (!level.isClientSide && stack.has(ModDataComponents.STORED_ENTITY_ID.get())) {
            stack.remove(ModDataComponents.STORED_ENTITY_ID.get());
            player.sendSystemMessage(Component.translatable(
                    "item.modularization_defend.entity_selector.cleared"));
            return InteractionResult.sidedSuccess(false);
        }
        return InteractionResult.PASS;
    }

    /**
     * 工具提示：显示已存储的实体 ID。
     */
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (!stack.has(ModDataComponents.STORED_ENTITY_ID.get())) {
            tooltip.add(Component.translatable("item.modularization_defend.entity_selector.empty"));
            return;
        }
        ResourceLocation entityId = stack.get(ModDataComponents.STORED_ENTITY_ID.get());
        if (entityId == null) {
            tooltip.add(Component.translatable("item.modularization_defend.entity_selector.empty"));
            return;
        }
        Optional<EntityType<?>> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(entityId);
        if (entityType.isPresent()) {
            tooltip.add(Component.literal("§7实体: §f")
                    .append(entityType.get().getDescription()));
            tooltip.add(Component.literal("§7ID: §8" + entityId));
        } else {
            tooltip.add(Component.translatable(
                    "item.modularization_defend.entity_selector.tooltip_unknown",
                    entityId.toString()));
        }
    }
}
