package org.lingZero.m_defend.ldlibUI;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lingZero.m_defend.Register.ModMenuTypes;

/**
 * DefendCore 的简单容器菜单
 * 用于提供正确的容器上下文以支持 InventorySlots 的物品栏同步
 */
public class DefendCoreContainerMenu extends AbstractContainerMenu {
    private final Player player;
    
    public DefendCoreContainerMenu(int containerId, Inventory playerInventory) {
        super(ModMenuTypes.DEFEND_CORE_MENU.get(), containerId);
        this.player = playerInventory.player;
        
        // 不需要手动添加槽位，LDLib2 的 InventorySlots 会在 UI 层面处理
        // 这里只需要提供正确的容器上下文
    }
    
    @Override
    public boolean stillValid(@NotNull Player player) {
        // 只要玩家存活就保持有效
        return player.isAlive();
    }
    
    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        // 由于我们没有手动添加槽位，这个方法不会被调用
        return ItemStack.EMPTY;
    }
}
