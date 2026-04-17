package org.lingZero.m_defend.Blocks.MultiblockFrame;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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

public abstract class BaseTurretBlockEntity extends BlockEntity {

    // 槽位索引常量
    public static final int SLOT_CORE = 0;              // 核心槽
    public static final int SLOT_GUIDANCE_CONTROL = 1;  // 制导控制组件槽
    public static final int SLOT_TARGET_SELECTOR = 2;  // 目标选择器槽
    public static final int SLOT_COUNT = 3;             // 总槽位数

    // 默认TAG条目：是否激活
    protected boolean isActive = false;
    

    // NeoForge物品处理器（3个槽位）
    protected final IItemHandler itemHandler = createItemHandler();
    
    // 计时器相关字段（不持久化）
    protected long lastTriggerTick = 0;             // 上次触发的游戏刻
    protected int triggerInterval = 20;             // 触发间隔（tick），默认1秒（20 ticks）
    protected boolean timerEnabled = true;          // 计时器是否启用
    
    // 射击系统管理器（封装所有射击相关逻辑）
    protected final TurretFireSystem fireSystem = new TurretFireSystem(this);
    
    // 过滤器数据（持久化）
    @Nullable
    protected TargetFilterData cachedFilterData = null;  // 缓存的过滤器数据

    public BaseTurretBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    
    /**
     * 创建物品处理器
     * 子类可以重写此方法以自定义槽位数量和行為
     *
     * @return 物品处理器实例
     */
    protected IItemHandler createItemHandler() {
        return new ItemStackHandler(SLOT_COUNT) {
            @Override
            protected void onContentsChanged(int slot) {
                // 当物品变化时标记数据已更改
                setChanged();
            }
            
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                // 子类可以重写此方法以限制可插入的物品
                // TODO: 根据槽位类型验证物品
                return true;
            }
        };
    }
    
    /**
     * 获取物品处理器能力
     * 用于与其他 MOD 的物流系统交互（如 Mekanism、Immersive Engineering 等）
     *
     * @param capability 能力类型
     * @param side       方向（可为 null）
     * @return 能力实例
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getCapability(BlockCapability<T, Direction> capability, @Nullable Direction side) {
        if (capability == Capabilities.ItemHandler.BLOCK) {
            return (T) itemHandler;
        }
        return null;
    }
    
    /**
     * 从 NBT 标签中读取数据
     */
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        
        // 读取激活状态
        if (tag.contains("IsActive")) {
            this.isActive = tag.getBoolean("IsActive");
        }
        
        // 读取物品处理器的数据
        if (itemHandler instanceof ItemStackHandler) {
            ((ItemStackHandler) itemHandler).deserializeNBT(registries, tag.getCompound("ItemHandler"));
        }
        
        // 读取过滤器数据
        if (tag.contains("FilterData")) {
            try {
                this.cachedFilterData = TargetFilterData.CODEC.parse(
                    net.minecraft.nbt.NbtOps.INSTANCE, tag.getCompound("FilterData")
                ).result().orElse(null);
            } catch (Exception e) {
                this.cachedFilterData = null;
            }
        }
        
        // 读取射击系统数据
        fireSystem.readFromNBT(tag);
    }
    
    /**
     * 将数据保存到 NBT 标签
     */
    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        
        // 保存激活状态
        tag.putBoolean("IsActive", isActive);
        
        // 保存物品处理器的数据
        if (itemHandler instanceof ItemStackHandler) {
            tag.put("ItemHandler", ((ItemStackHandler) itemHandler).serializeNBT(registries));
        }
        
        // 保存过滤器数据
        if (cachedFilterData != null) {
            tag.put("FilterData", 
                TargetFilterData.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, cachedFilterData)
                    .result().orElse(new CompoundTag())
            );
        }
        
        // 保存射击系统数据
        fireSystem.writeToNBT(tag);
    }
    
    /**
     * 计算实际射击间隔（应用增益系统）
     * 子类可以重写此方法以自定义增益计算逻辑
     * 
     * @return 实际射击间隔（tick）
     * 
     * @example 示例子类实现：
     * <pre>{@code
     * @Override
     * protected int getActualFireInterval() {
     *     // 从核心槽位获取炮塔核心数据
     *     ItemStack coreStack = getItemInSlot(SLOT_CORE);
     *     if (coreStack.isEmpty() || !(coreStack.getItem() instanceof TurretCore)) {
     *         return fireSystem.getBaseFireInterval(); // 没有核心时使用基础间隔
     *     }
     *     
     *     TurretCoreData coreData = TurretCore.getData(coreStack);
     *     int firingRateLevel = coreData.firingRateLevel(); // 射速等级
     *     
     *     // 应用增益：每级减少10%间隔
     *     // 例如：baseInterval=40, level=3 -> 40 / (1 + 3*0.1) = 40 / 1.3 ≈ 30 ticks
     *     double multiplier = 1.0 + (firingRateLevel * 0.1);
     *     return Math.max(1, (int)(fireSystem.getBaseFireInterval() / multiplier));
     * }
     * }</pre>
     */
    protected int getActualFireInterval() {
        // 默认实现：直接返回基础间隔
        // 子类应重写此方法，从炮塔核心读取射速等级并应用增益
        return fireSystem.getBaseFireInterval();
    }
    
    /**
     * 射击回调方法
     * 子类必须重写此方法以实现具体的攻击逻辑
     * 例如：发射子弹、激光、导弹等
     */
    protected void onFire() {
        // 默认实现为空，子类必须重写
        org.lingZero.m_defend.util.DebugLogger.warn("onFire() 未被子类实现");
    }
    
    /**
     * 设置目标锁定状态
     * 优化：自动启用/禁用射击计时器
     * 
     * @param locked 是否锁定目标
     */
    public void setTargetLocked(boolean locked) {
        fireSystem.setTargetLocked(locked);
    }
    
    /**
     * 检查是否锁定目标
     * 
     * @return 是否锁定目标
     */
    public boolean hasTarget() {
        return fireSystem.hasTarget();
    }
    
    /**
     * 获取激活状态
     *
     * @return 是否激活
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * 设置激活状态
     *
     * @param active 激活状态
     */
    public void setActive(boolean active) {
        this.isActive = active;
        setChanged();
    }
    
    /**
     * 获取指定槽位的物品
     *
     * @param slot 槽位索引
     * @return 物品栈
     */
    public ItemStack getItemInSlot(int slot) {
        return itemHandler.getStackInSlot(slot);
    }
    
    /**
     * 设置指定槽位的物品
     *
     * @param slot  槽位索引
     * @param stack 物品栈
     */
    public void setItemInSlot(int slot, ItemStack stack) {
        if (itemHandler instanceof net.neoforged.neoforge.items.IItemHandlerModifiable) {
            ((net.neoforged.neoforge.items.IItemHandlerModifiable) itemHandler).setStackInSlot(slot, stack);
        }
    }
    
    /**
     * 获取或设置核心槽的物品
     *
     * @param stack 物品栈（null 时为获取，非 null 时为设置）
     * @return 当前核心槽的物品栈
     */
    public ItemStack coreItem(ItemStack stack) {
        if (stack != null) {
            setItemInSlot(SLOT_CORE, stack);
            // 检查是否所有槽位都已安装，自动激活炮塔
            checkAndAutoActivate();
        }
        return getItemInSlot(SLOT_CORE);
    }
    
    /**
     * 获取或设置制导控制组件槽的物品
     *
     * @param stack 物品栈（null 时为获取，非 null 时为设置）
     * @return 当前制导控制组件槽的物品栈
     */
    public ItemStack guidanceControlItem(ItemStack stack) {
        if (stack != null) {
            setItemInSlot(SLOT_GUIDANCE_CONTROL, stack);
            // 检查是否所有槽位都已安装，自动激活炮塔
            checkAndAutoActivate();
        }
        return getItemInSlot(SLOT_GUIDANCE_CONTROL);
    }
    
    /**
     * 获取或设置目标选择器槽的物品
     *
     * @param stack 物品栈（null 时为获取，非 null 时为设置）
     * @return 当前目标选择器槽的物品栈
     */
    public ItemStack targetSelectorItem(ItemStack stack) {
        if (stack != null) {
            setItemInSlot(SLOT_TARGET_SELECTOR, stack);
            // 更新过滤器数据缓存
            updateFilterDataCache();
            // 检查是否所有槽位都已安装，自动激活炮塔
            checkAndAutoActivate();
        }
        return getItemInSlot(SLOT_TARGET_SELECTOR);
    }
    
    /**
     * 检查所有槽位是否都已安装物品，并自动激活/停用炮塔
     * 当所有3个槽位都有物品时激活炮塔，否则停用
     */
    protected void checkAndAutoActivate() {
        boolean allSlotsFilled = true;
        
        // 检查所有槽位是否都有物品
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            if (getItemInSlot(slot).isEmpty()) {
                allSlotsFilled = false;
                break;
            }
        }
        
        // 根据检查结果设置激活状态
        if (allSlotsFilled && !isActive) {
            setActive(true);
            org.lingZero.m_defend.util.DebugLogger.info("炮塔已自动激活（所有槽位已安装）");
        } else if (!allSlotsFilled && isActive) {
            setActive(false);
            org.lingZero.m_defend.util.DebugLogger.info("炮塔已自动停用（槽位不完整）");
        }
    }
    
    /**
     * 更新过滤器数据缓存
     * 从目标选择器槽的物品中读取过滤器配置并缓存
     */
    protected void updateFilterDataCache() {
        ItemStack filterItem = getItemInSlot(SLOT_TARGET_SELECTOR);
        
        if (filterItem.isEmpty()) {
            cachedFilterData = null;
            return;
        }
        
        // 从物品的 DataComponent 中读取过滤器数据
        TargetFilterData data = filterItem.get(
            org.lingZero.m_defend.Register.ModDataComponents.TARGET_FILTER_DATA.get()
        );
        
        if (data != null) {
            cachedFilterData = data;
            setChanged();  // 标记数据已更改，需要保存
        } else {
            cachedFilterData = null;
        }
    }
    
    /**
     * 获取缓存的过滤器数据
     * 
     * @return 过滤器数据，如果未安装则返回 null
     */
    @Nullable
    public TargetFilterData getCachedFilterData() {
        return cachedFilterData;
    }
    
    /**
     * 更新计时器
     * 应在子类的 serverTick() 方法中调用
     * 使用世界游戏刻进行计时，达到间隔时自动触发 onTimerTrigger()
     */
    protected void updateTimer() {
        if (!timerEnabled || !isActive || level == null) {
            return;
        }
        
        long currentTick = level.getGameTime();
        
        // 检查是否达到触发间隔
        if (currentTick - lastTriggerTick >= triggerInterval) {
            lastTriggerTick = currentTick;  // 更新上次触发时间
            onTimerTrigger();               // 触发回调
        }
    }

    /**
     * BlockEntity tick 方法
     * 每个游戏刻调用一次，由 Minecraft 自动调用
     */
    public void tick() {
        if (level == null || level.isClientSide) {
            return;
        }
        
        // 调用子类的自定义 tick 逻辑
        onCustomTick();
        
        // 更新计时器（激活时会自动触发 onTimerTrigger）
        updateTimer();
        
        // 更新射击系统（如果锁定目标且达到射击间隔则自动射击）
        fireSystem.tick();
    }
    
    /**
     * 自定义 tick 逻辑
     * 子类可以重写此方法添加额外的 tick 处理
     */
    protected void onCustomTick() {
        // 默认实现为空
    }

    /**
     * 计时器触发回调
     * 子类应重写此方法以实现定时触发的逻辑
     * 例如：炮塔射击、扫描目标、发射弹药等
     */
    protected void onTimerTrigger() {
        // 默认实现为空，子类应重写此方法
    }
    
    /**
     * 获取上次触发的游戏刻
     *
     * @return 上次触发的刻数
     */
    public long getLastTriggerTick() {
        return lastTriggerTick;
    }
    
    /**
     * 获取触发间隔（tick）
     *
     * @return 触发间隔
     */
    public int getTriggerInterval() {
        return triggerInterval;
    }
    
    /**
     * 设置触发间隔
     *
     * @param interval 新的触发间隔（tick），必须大于 0
     */
    public void setTriggerInterval(int interval) {
        if (interval > 0) {
            this.triggerInterval = interval;
            setChanged();
        }
    }
    
    /**
     * 检查计时器是否启用
     *
     * @return 是否启用
     */
    public boolean isTimerEnabled() {
        return timerEnabled;
    }
    
    /**
     * 设置计时器启用状态
     *
     * @param enabled 是否启用
     */
    public void setTimerEnabled(boolean enabled) {
        this.timerEnabled = enabled;
        if (!enabled && level != null) {
            lastTriggerTick = level.getGameTime(); // 禁用时重置为当前时间
        }
        setChanged();
    }
    
    /**
     * 手动重置计时器
     * 将下次触发时间设置为当前时间 + 间隔
     */
    public void resetTimer() {
        if (level != null) {
            lastTriggerTick = level.getGameTime();
        }
    }
    
    /**
     * 立即触发一次计时器事件
     * 不会重置计数器
     */
    protected void triggerNow() {
        onTimerTrigger();
    }
    
    // ==================== 射击系统 API（委托给 fireSystem）====================
    
    /**
     * 获取基础射击间隔
     * 
     * @return 基础射击间隔（tick）
     */
    public int getBaseFireInterval() {
        return fireSystem.getBaseFireInterval();
    }
    
    /**
     * 设置基础射击间隔
     * 
     * @param interval 新的基础射击间隔（tick），必须大于 0
     */
    public void setBaseFireInterval(int interval) {
        fireSystem.setBaseFireInterval(interval);
    }
    
    /**
     * 获取实际射击间隔（应用增益后）
     * 
     * @return 实际射击间隔（tick）
     */
    public int getCurrentFireInterval() {
        return fireSystem.getActualFireInterval();
    }
    
    /**
     * 获取上次射击的游戏刻
     * 
     * @return 上次射击的刻数
     */
    public long getLastFireTick() {
        return fireSystem.getLastFireTick();
    }
    
    /**
     * 检查射击计时器是否启用
     * 
     * @return 是否启用
     */
    public boolean isFireTimerEnabled() {
        return fireSystem.isFireTimerEnabled();
    }
    
    /**
     * 设置射击计时器启用状态
     * 
     * @param enabled 是否启用
     */
    public void setFireTimerEnabled(boolean enabled) {
        fireSystem.setFireTimerEnabled(enabled);
    }
    
    /**
     * 手动重置射击计时器
     * 将下次射击时间设置为当前时间 + 间隔
     */
    public void resetFireTimer() {
        fireSystem.resetFireTimer();
    }
    
    /**
     * 立即触发一次射击
     * 不会重置计数器，也不会检查间隔
     */
    protected void fireNow() {
        fireSystem.fireNow();
    }
    
    /**
     * 服务端 tick 方法
     * 子类应重写此方法并在其中调用 super.serverTick()
     */
    public void serverTick() {
        // 默认实现为空，子类可以重写
    }
}
