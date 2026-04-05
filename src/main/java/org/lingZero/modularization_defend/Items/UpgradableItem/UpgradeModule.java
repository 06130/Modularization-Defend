package org.lingZero.modularization_defend.Items.UpgradableItem;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.lingZero.modularization_defend.util.DebugLogger;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;

/**
 * 通用升级模块物品基类
 * 可以用于任何实现了 IUpgradableItem 接口的物品
 */
public class UpgradeModule extends Item {
    
    public enum UpgradeType {
        SPEED("upgrade.modularization_defend.speed", "tooltip.modularization_defend.upgrade.speed"),
        ENERGY("upgrade.modularization_defend.energy", "tooltip.modularization_defend.upgrade.energy"),
        EFFICIENCY("upgrade.modularization_defend.efficiency", "tooltip.modularization_defend.upgrade.efficiency"),
        CAPACITY("upgrade.modularization_defend.capacity", "tooltip.modularization_defend.upgrade.capacity"),
        SECURITY("upgrade.modularization_defend.security", "tooltip.modularization_defend.upgrade.security");
        
        private final String nameKey;
        private final String descriptionKey;
        
        UpgradeType(String nameKey, String descriptionKey) {
            this.nameKey = nameKey;
            this.descriptionKey = descriptionKey;
        }
        
        /**
         * 获取翻译后的名称
         */
        public Component getTranslatedName() {
            return Component.translatable(nameKey);
        }
        
        /**
         * 获取翻译后的描述
         */
        public Component getTranslatedDescription() {
            return Component.translatable(descriptionKey);
        }
        
        /**
         * 获取翻译键（用于日志等）
         */
        public String getNameKey() {
            return nameKey;
        }
        
        /**
         * 获取描述翻译键
         */
        public String getDescriptionKey() {
            return descriptionKey;
        }
    }
    
    private final UpgradeType upgradeType;
    
    public UpgradeModule(Properties properties, UpgradeType type) {
        super(properties);
        this.upgradeType = type;
    }
    
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        
        // 使用翻译键显示名称和描述
        tooltip.add(Component.translatable(upgradeType.getNameKey())
                .withStyle(style -> style.withColor(0x00FFFF)));
        tooltip.add(Component.translatable(upgradeType.getDescriptionKey())
                .withStyle(style -> style.withColor(0xAAAAAA)));
        tooltip.add(Component.translatable("tooltip.modularization_defend.upgrade_module.usage")
                .withStyle(style -> style.withColor(0xCCCCCC)));
    }
    
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack upgradeStack = player.getItemInHand(hand);
        
        // 从 Curios 饰品槽查找可升级物品
        ItemStack targetStack = findUpgradableItem(player);
        
        if (targetStack.isEmpty()) {
            DebugLogger.warn("未在 Curios 饰品槽中找到可升级物品");
            return InteractionResultHolder.fail(upgradeStack);
        }
        
        if (!(targetStack.getItem() instanceof IUpgradableItem upgradableItem)) {
            DebugLogger.warn("找到的物品不支持升级系统");
            return InteractionResultHolder.fail(upgradeStack);
        }
        
        // 检查是否可以接受该升级类型
        if (!upgradableItem.canAcceptUpgrade(targetStack, upgradeType)) {
            DebugLogger.warn("该物品不接受 " + upgradeType.getNameKey() + " 升级");
            return InteractionResultHolder.fail(upgradeStack);
        }
        
        // 获取当前等级
        int currentLevel = upgradableItem.getUpgradeLevel(targetStack, upgradeType);
        int maxLevel = upgradableItem.getMaxUpgradeLevel(upgradeType);
        
        if (currentLevel >= maxLevel) {
            DebugLogger.warn("该升级已达到最大等级 (" + currentLevel + "/" + maxLevel + ")");
            return InteractionResultHolder.fail(upgradeStack);
        }
        
        // 应用升级
        upgradableItem.applyUpgrade(targetStack, upgradeType, currentLevel + 1);
        
        DebugLogger.info("成功应用升级: " + upgradeType.getNameKey() + " 等级 " + (currentLevel + 1));
        
        // 消耗升级模块
        if (!player.isCreative()) {
            upgradeStack.shrink(1);
        }
        
        return InteractionResultHolder.success(upgradeStack);
    }
    
    /**
     * 从 Curios 饰品槽查找可升级物品
     */
    private ItemStack findUpgradableItem(Player player) {
        final ItemStack[] result = {ItemStack.EMPTY};
        
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            var curios = handler.findCurios(item -> item.getItem() instanceof IUpgradableItem);
            if (!curios.isEmpty()) {
                result[0] = curios.getFirst().stack();
            }
        });
        
        return result[0];
    }
}
