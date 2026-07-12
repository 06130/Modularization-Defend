package org.lingZero.modularization_defend.Item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.lingZero.modularization_defend.nodegraph.GraphEditorOpener;

import java.util.List;

/**
 * 程序存储卡——潜行右键打开节点图编辑器，可编辑并保存炮塔逻辑图。
 *
 * <p>图数据存储在 {@link DataComponents#CUSTOM_DATA} 组件的
 * {@code modularization_defend:graph} 键下。</p>
 */
public class ProgramStorageCardItem extends Item {

    private static final String GRAPH_KEY = "modularization_defend:graph";

    public ProgramStorageCardItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    /** 潜行+右键 → 打开节点图编辑器；普通右键 → 无操作 */
    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            return super.use(level, player, hand);
        }

        if (level.isClientSide) {
            CompoundTag graphTag = getGraphTag(stack);
            GraphEditorOpener.openCardEditor(
                    graphTag,
                    hand == InteractionHand.MAIN_HAND,
                    player);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    /** 从物品 CUSTOM_DATA 组件中读取存储的图 NBT */
    public static CompoundTag getGraphTag(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains(GRAPH_KEY)) {
                return tag.getCompound(GRAPH_KEY);
            }
        }
        return new CompoundTag();
    }

    /** 将图 NBT 写入物品 CUSTOM_DATA 组件 */
    public static void setGraphTag(ItemStack stack, CompoundTag graphTag) {
        CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = (existing != null) ? existing.copyTag() : new CompoundTag();
        tag.put(GRAPH_KEY, graphTag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /** 检查卡片是否已存储图数据 */
    public static boolean hasGraphData(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        return customData != null && customData.copyTag().contains(GRAPH_KEY);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        if (hasGraphData(stack)) {
            tooltip.add(Component.translatable(
                    "item.modularization_defend.program_storage_card.has_data"));
        } else {
            tooltip.add(Component.translatable(
                    "item.modularization_defend.program_storage_card.empty"));
        }
        tooltip.add(Component.translatable(
                "item.modularization_defend.program_storage_card.hint"));
    }
}
