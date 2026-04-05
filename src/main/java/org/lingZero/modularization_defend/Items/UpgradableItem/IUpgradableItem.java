package org.lingZero.modularization_defend.Items.UpgradableItem;

import net.minecraft.world.item.ItemStack;

/**
 * 可升级物品接口
 * 任何支持升级系统的物品都应该实现此接口
 */
public interface IUpgradableItem {
    
    /**
     * 检查物品是否可以接受指定的升级类型
     * 
     * @param stack 物品栈
     * @param upgradeType 升级类型
     * @return 如果可以接受该升级则返回 true
     */
    boolean canAcceptUpgrade(ItemStack stack, UpgradeModule.UpgradeType upgradeType);
    
    /**
     * 获取指定升级类型的当前等级
     * 
     * @param stack 物品栈
     * @param upgradeType 升级类型
     * @return 当前等级
     */
    int getUpgradeLevel(ItemStack stack, UpgradeModule.UpgradeType upgradeType);
    
    /**
     * 应用升级到物品
     * 
     * @param stack 物品栈
     * @param upgradeType 升级类型
     * @param newLevel 新等级
     * @return 升级后的物品栈
     */
    ItemStack applyUpgrade(ItemStack stack, UpgradeModule.UpgradeType upgradeType, int newLevel);
    
    /**
     * 获取最大升级等级
     * 
     * @param upgradeType 升级类型
     * @return 最大等级
     */
    default int getMaxUpgradeLevel(UpgradeModule.UpgradeType upgradeType) {
        return 4; // 默认最大等级为 4
    }
}
