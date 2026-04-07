package org.lingZero.modularization_defend.Blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lingZero.modularization_defend.Register.ModBlockEntities;

public class UpgradeCraftingTable extends BlockEntity {

    private ItemStack itemStack = ItemStack.EMPTY;
    private java.util.List<ItemStack> UpgradeItemList = new java.util.ArrayList<>();

    public UpgradeCraftingTable(BlockPos pos, BlockState state) {
        super(ModBlockEntities.UPGRADE_CRAFTING_TABLE_ENTITY.get(), pos, state);
    }

    // 在此处从传入的CompoundTag中读取数值。
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        // 读取单个物品栈
        this.itemStack = ItemStack.parseOptional(registries, tag.getCompound("itemStack"));
        // 读取物品列表
        this.UpgradeItemList.clear();
        if (tag.contains("itemList", CompoundTag.TAG_LIST)) {
            ListTag listTag = tag.getList("itemList", CompoundTag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag itemTag = listTag.getCompound(i);
                ItemStack stack = ItemStack.parseOptional(registries, itemTag);
                if (!stack.isEmpty()) {
                    this.UpgradeItemList.add(stack);
                }
            }
        }
    }

   // 在此处将数值保存到传入的CompoundTag中。
    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        
        // 保存单个物品栈（仅当非空时）
        if (!this.itemStack.isEmpty()) {
            tag.put("itemStack", this.itemStack.save(registries));
        }
        
        // 保存物品列表
        ListTag listTag = new ListTag();
        for (ItemStack stack : this.UpgradeItemList) {
            if (!stack.isEmpty()) {
                listTag.add(stack.save(registries));
            }
        }
        tag.put("itemList", listTag);
    }
    
    /**
     * 获取存储的物品栈
     */
    public ItemStack getItemStack() {
        return itemStack;
    }
    
    /**
     * 设置存储的物品栈
     */
    public void setItemStack(ItemStack stack) {
        this.itemStack = stack != null ? stack.copy() : ItemStack.EMPTY;
        setChanged();
    }
    
    /**
     * 获取存储的物品列表
     */
    public java.util.List<ItemStack> getUpgradeItemList() {
        return UpgradeItemList;
    }
    
    /**
     * 向物品列表中添加物品
     */
    public void addItemToUpgradeList(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            this.UpgradeItemList.add(stack.copy());
            setChanged();
        }
    }
    
    /**
     * 清空物品列表
     */
    public void clearUpgradeItemList() {
        this.UpgradeItemList.clear();
        setChanged();
    }

}
