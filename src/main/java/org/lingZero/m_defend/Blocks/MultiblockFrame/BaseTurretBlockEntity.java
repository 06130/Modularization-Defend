package org.lingZero.m_defend.Blocks.MultiblockFrame;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.lingZero.m_defend.DataComponents.TargetFilterData;
import org.lingZero.m_defend.entity.EntityTrace.EntityTracker;

public abstract class BaseTurretBlockEntity extends BlockEntity {

    // 槽位索引常量
    public static final int SLOT_CORE = 0;
    public static final int SLOT_GUIDANCE_CONTROL = 1;
    public static final int SLOT_TARGET_SELECTOR = 2;
    public static final int SLOT_COUNT = 3;

    protected boolean isActive = false;

    // 物品系统
    protected final IItemHandler itemHandler = createItemHandler();

    // 射击系统（核心）
    protected final TurretFireSystem fireSystem = new TurretFireSystem(this);

    // 过滤器数据缓存
    @Nullable
    protected TargetFilterData cachedFilterData = null;

    public BaseTurretBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // ==================== 物品处理器 ====================

    protected IItemHandler createItemHandler() {
        return new ItemStackHandler(SLOT_COUNT) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return true; // 子类可重写
            }
        };
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getCapability(BlockCapability<T, Direction> capability, @Nullable Direction side) {
        if (capability == Capabilities.ItemHandler.BLOCK) {
            return (T) itemHandler;
        }
        return null;
    }

    // ==================== 抽象方法（子类实现） ====================

    /**
     * 子类实现：尝试锁定一个新目标并返回追踪器
     * @return 已锁定的 EntityTracker，若无法找到目标则返回 null
     */
    @Nullable
    protected abstract EntityTracker acquireTarget();

    /**
     * 子类实现：对指定实体执行一次攻击
     * @param target 目标实体（保证非 null 且有效）
     */
    protected abstract void onFire(Entity target);

    // ==================== Tick 驱动 ====================

    public void tick() {
        if (level == null || level.isClientSide) {
            return;
        }
        fireSystem.tick();      // 搜索 + 射击，一切行为由此驱动
        onCustomTick();         // 额外的每 tick 逻辑（粒子、动画等）
    }

    /**
     * 额外的自定义 tick 逻辑，默认为空
     */
    protected void onCustomTick() {
    }

    /**
     * 由射击系统调用的开火入口
     */
    void performFire(Entity target) {
        onFire(target);
    }

    /**
     * 目标丢失的回调（可被子类重写以播放音效等）
     */
    protected void onTargetLost(String reason) {
    }

    // ==================== 射击间隔的计算（可被子类覆盖以加入增益系统） ====================

    /**
     * 计算实际射击间隔
     * 默认返回基础间隔，子类可加入 buff 等修正
     */
    protected int getActualFireInterval() {
        return fireSystem.getBaseFireInterval();
    }

    // ==================== 状态管理 ====================

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        if (this.isActive == active) {
            return;
        }
        this.isActive = active;
        if (active) {
            fireSystem.setFireTimerEnabled(true);
        } else {
            fireSystem.releaseTarget();
        }
        onActiveStateChanged(active);
        setChanged();
    }

    protected void onActiveStateChanged(boolean active) {
    }

    // ==================== 物品槽位访问 ====================

    public ItemStack getItemInSlot(int slot) {
        return itemHandler.getStackInSlot(slot);
    }

    public void setItemInSlot(int slot, ItemStack stack) {
        if (itemHandler instanceof net.neoforged.neoforge.items.IItemHandlerModifiable modifiable) {
            modifiable.setStackInSlot(slot, stack);
        }
    }

    public ItemStack coreItem(ItemStack stack) {
        if (stack != null) {
            setItemInSlot(SLOT_CORE, stack);
            checkAndAutoActivate();
        }
        return getItemInSlot(SLOT_CORE);
    }

    public ItemStack guidanceControlItem(ItemStack stack) {
        if (stack != null) {
            setItemInSlot(SLOT_GUIDANCE_CONTROL, stack);
            checkAndAutoActivate();
        }
        return getItemInSlot(SLOT_GUIDANCE_CONTROL);
    }

    public ItemStack targetSelectorItem(ItemStack stack) {
        if (stack != null) {
            setItemInSlot(SLOT_TARGET_SELECTOR, stack);
            updateFilterDataCache();
            checkAndAutoActivate();
        }
        return getItemInSlot(SLOT_TARGET_SELECTOR);
    }

    protected void checkAndAutoActivate() {
        boolean allFilled = true;
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            if (getItemInSlot(slot).isEmpty()) {
                allFilled = false;
                break;
            }
        }
        if (allFilled && !isActive) {
            setActive(true);
        } else if (!allFilled && isActive) {
            setActive(false);
        }
    }

    protected void updateFilterDataCache() {
        ItemStack filterItem = getItemInSlot(SLOT_TARGET_SELECTOR);
        if (filterItem.isEmpty()) {
            cachedFilterData = null;
            return;
        }
        cachedFilterData = filterItem.get(
                org.lingZero.m_defend.Register.ModDataComponents.TARGET_FILTER_DATA.get()
        );
        if (cachedFilterData != null) {
            setChanged();
        }
    }

    @Nullable
    public TargetFilterData getCachedFilterData() {
        return cachedFilterData;
    }

    // ==================== NBT 持久化 ====================

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("IsActive")) {
            this.isActive = tag.getBoolean("IsActive");
        }
        if (itemHandler instanceof ItemStackHandler handler) {
            handler.deserializeNBT(registries, tag.getCompound("ItemHandler"));
        }
        if (tag.contains("FilterData")) {
            try {
                this.cachedFilterData = TargetFilterData.CODEC.parse(
                        net.minecraft.nbt.NbtOps.INSTANCE, tag.getCompound("FilterData")
                ).result().orElse(null);
            } catch (Exception e) {
                this.cachedFilterData = null;
            }
        }
        fireSystem.readFromNBT(tag);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("IsActive", isActive);
        if (itemHandler instanceof ItemStackHandler handler) {
            tag.put("ItemHandler", handler.serializeNBT(registries));
        }
        if (cachedFilterData != null) {
            tag.put("FilterData",
                    TargetFilterData.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, cachedFilterData)
                            .result().orElse(new CompoundTag())
            );
        }
        fireSystem.writeToNBT(tag);
    }
}