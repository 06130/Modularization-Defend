package org.lingZero.modularization_defend.Blocks.Multiblock.AgreementCore;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.lingZero.modularization_defend.Register.ModMenuTypes;

/**
 * 协议核心 GUI 菜单
 */
public class AgreementCoreMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final ContainerData data;
    
    public AgreementCoreMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new BlockPos(0, 0, 0), new SimpleContainerData(4));
    }
    
    public AgreementCoreMenu(int containerId, Inventory playerInventory, BlockPos pos, ContainerData data) {
        super(ModMenuTypes.AGREEMENT_CORE_MENU.get(), containerId);
        this.pos = pos;
        this.data = data;
        
        // 添加玩家物品栏槽位
        addPlayerInventory(playerInventory);
    }
    
    /**
     * 添加玩家物品栏（右侧布局）
     */
    private void addPlayerInventory(Inventory playerInventory) {
        // 主物品栏（3 行 x9 列）- 靠右布局
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 227 + 8 + col * 18, 67 + 84 + row * 18));
            }
        }
        
        // 快捷栏（1 行 x9 列）- 靠右布局
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 227 + 8 + col * 18, 67 + 142));
        }
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            
            // 尝试移动到另一个槽位
            if (index < 27) {
                // 从主物品栏移动到快捷栏
                if (!this.moveItemStackTo(itemstack1, 27, 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // 从快捷栏移动到主物品栏
                if (!this.moveItemStackTo(itemstack1, 0, 27, false)) {
                    return ItemStack.EMPTY;
                }
            }
            
            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            
            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            
            slot.onTake(player, itemstack1);
        }
        
        return itemstack;
    }
    
    @Override
    public boolean stillValid(Player player) {
        // 检查玩家是否还在方块附近
        double maxDistance = 64.0;
        return player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= maxDistance * maxDistance;
    }
    
    /**
     * 获取容器数据
     */
    public ContainerData getData() {
        return data;
    }
}
