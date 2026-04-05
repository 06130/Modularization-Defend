package org.lingZero.modularization_defend.MachineGUI.Frame;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * 机器GUI槽位类
 * 用于渲染和交互物品槽位
 */
public class MachineSlot extends MachineElement {
    
    // 槽位类型
    public enum SlotType {
        NORMAL(18, 18),      // 普通槽位
        INPUT(18, 18),       // 输入槽位
        OUTPUT(18, 18),      // 输出槽位
        POWER(18, 18),       // 能量槽位
        UPGRADE(18, 18);     // 升级槽位
        
        private final int width;
        private final int height;
        
        SlotType(int width, int height) {
            this.width = width;
            this.height = height;
        }
        
        public int getWidth() {
            return width;
        }
        
        public int getHeight() {
            return height;
        }
    }
    
    private final SlotType slotType;
    private ItemStack stack = ItemStack.EMPTY;
    private boolean hovered = false;
    
    public MachineSlot(BaseMachineScreen<?> screen, int x, int y) {
        this(screen, x, y, SlotType.NORMAL);
    }
    
    public MachineSlot(BaseMachineScreen<?> screen, int x, int y, SlotType slotType) {
        super(screen, x, y, slotType.getWidth(), slotType.getHeight());
        this.slotType = slotType;
    }
    
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 渲染槽位背景
        renderSlotBackground(guiGraphics);
        
        // 渲染物品
        if (!stack.isEmpty()) {
            renderItem(guiGraphics, mouseX, mouseY);
        }
        
        // 检查悬停状态
        hovered = isMouseOver(mouseX, mouseY);
    }
    
    /**
     * 渲染槽位背景
     */
    protected void renderSlotBackground(GuiGraphics guiGraphics) {
        // 渲染槽位边框
        int color = getSlotColor();
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, color);
        
        // 渲染槽位内部
        int innerColor = hovered ? 0x80FFFFFF : 0x80E0E0E0;
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, innerColor);
    }
    
    /**
     * 获取槽位颜色
     */
    protected int getSlotColor() {
        return switch (slotType) {
            case INPUT -> 0xFF00FF00;    // 绿色
            case OUTPUT -> 0xFFFF0000;   // 红色
            case POWER -> 0xFFFFFF00;    // 黄色
            case UPGRADE -> 0xFF00FFFF;  // 青色
            default -> 0xFFFFFFFF;       // 白色
        };
    }
    
    /**
     * 渲染物品
     */
    protected void renderItem(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 渲染物品图标
        guiGraphics.renderItem(stack, x + 1, y + 1);
        
        // 渲染物品数量
        if (stack.getCount() > 1) {
            String countText = String.valueOf(stack.getCount());
            guiGraphics.drawString(screen.getMinecraft().font, countText, x + 10, y + 9, 0xFFFFFF, true);
        }
    }
    
    @Override
    public void renderToolTip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!stack.isEmpty() && hovered) {
            guiGraphics.renderTooltip(screen.getMinecraft().font, stack, mouseX, mouseY);
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hovered && button == 0) { // 左键点击
            // 这里可以处理物品拿取/放置逻辑
            return true;
        }
        return false;
    }
    
    /**
     * 设置槽位中的物品
     */
    public void setStack(@NotNull ItemStack stack) {
        this.stack = stack.copy();
    }
    
    /**
     * 获取槽位中的物品
     */
    @NotNull
    public ItemStack getStack() {
        return stack;
    }
    
    /**
     * 检查槽位是否为空
     */
    public boolean isEmpty() {
        return stack.isEmpty();
    }
    
    /**
     * 获取槽位类型
     */
    public SlotType getSlotType() {
        return slotType;
    }
    
    /**
     * 检查是否悬停
     */
    public boolean isHovered() {
        return hovered;
    }
}