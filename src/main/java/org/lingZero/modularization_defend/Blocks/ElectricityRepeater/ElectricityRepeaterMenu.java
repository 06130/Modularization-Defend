package org.lingZero.modularization_defend.Blocks.ElectricityRepeater;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.lingZero.modularization_defend.Register.ModMenuTypes;

/**
 * 电力中继器 GUI 容器
 */
public class ElectricityRepeaterMenu extends AbstractContainerMenu {
    
    private final BlockPos blockPos;
    private final ContainerData data;
    
    public ElectricityRepeaterMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, null, new SimpleContainerData(4));
    }
    
    public ElectricityRepeaterMenu(int id, Inventory playerInventory, BlockPos blockPos, ContainerData data) {
        super(ModMenuTypes.ELECTRICITY_REPEATER_MENU.get(), id);
        this.blockPos = blockPos;
        this.data = data;
        
        // 添加玩家物品栏槽位
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        
        // 添加数据监听
        addDataSlots(data);
    }
    
    /**
     * 添加玩家物品栏（3x9）
     */
    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
    }
    
    /**
     * 玩家快捷栏
     */
    private void addPlayerHotbar(Inventory playerInventory) {
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }
    
    /**
     * 检查玩家是否可以使用此容器
     * 注：由于区块加载机制，玩家必须能加载该方块才能打开 GUI，因此无需额外的距离检测
     */
    @Override
    public boolean stillValid(Player player) {
        // 始终返回 true，允许玩家在任何距离打开 GUI
        // 只要方块存在且能被加载，玩家就能访问
        return blockPos != null && !player.isSpectator();
    }
    
    /**
     * 快速移动物品
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            
            // 尝试合并到玩家物品栏
            if (index < 27 + 9) {
                // 从玩家物品栏移动
                if (!this.moveItemStackTo(itemstack1, 27 + 9, 36 + 9, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // 从快捷栏移动
                if (!this.moveItemStackTo(itemstack1, 0, 27 + 9, false)) {
                    return ItemStack.EMPTY;
                }
            }
            
            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        
        return itemstack;
    }
    
    /**
     * 获取能量数据
     */
    public int getEnergy() {
        return data.get(0);
    }
    
    /**
     * 获取最大能量
     */
    public int getMaxEnergy() {
        return data.get(1);
    }
    
    /**
     * 获取电网状态（预留）
     */
    public int getGridStatus() {
        return data.get(2);
    }
    
    /**
     * 获取升级状态（预留）
     */
    public int getUpgradeStatus() {
        return data.get(3);
    }
}