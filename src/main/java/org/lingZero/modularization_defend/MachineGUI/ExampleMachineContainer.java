package org.lingZero.modularization_defend.MachineGUI;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * 示例机器容器
 * 用于演示机器GUI框架的使用
 */
public class ExampleMachineContainer extends AbstractContainerMenu {
    
    private final ContainerLevelAccess access;
    
    public ExampleMachineContainer(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }
    
    public ExampleMachineContainer(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(null, containerId); // 注意：这里应该传入实际的MenuType
        this.access = access;
        
        // 添加玩家物品栏槽位
        addPlayerInventory(playerInventory);
        
        // 添加机器特定的槽位
        addMachineSlots();
    }
    
    /**
     * 添加玩家物品栏槽位
     */
    private void addPlayerInventory(Inventory playerInventory) {
        // 主物品栏（27个槽位）
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        
        // 快捷栏（9个槽位）
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }
    
    /**
     * 添加机器特定的槽位
     */
    private void addMachineSlots() {
        // 输入槽位
        addSlot(new Slot(null, 0, 44, 17)); // 输入槽
        
        // 输出槽位
        addSlot(new Slot(null, 1, 116, 35)); // 输出槽
        
        // 能量槽位
        addSlot(new Slot(null, 2, 8, 62)); // 能量输入槽
    }
    
    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(access, player, null); // 注意：这里应该传入实际的方块
    }
    
    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            
            if (index < 36) { // 玩家物品栏
                if (!moveItemStackTo(itemstack1, 36, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else { // 机器槽位
                if (!moveItemStackTo(itemstack1, 0, 36, false)) {
                    return ItemStack.EMPTY;
                }
            }
            
            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        
        return itemstack;
    }
}