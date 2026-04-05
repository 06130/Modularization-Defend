package org.lingZero.modularization_defend.MachineGUI;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 机器GUI元素的基础类
 * 所有GUI元素都应该继承此类
 */
public abstract class MachineElement implements GuiEventListener {
    
    protected final BaseMachineScreen<?> screen;
    protected int x;
    protected int y;
    protected final int width;
    protected final int height;
    
    protected boolean active = true;
    protected boolean visible = true;
    
    public MachineElement(BaseMachineScreen<?> screen, int x, int y, int width, int height) {
        this.screen = screen;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    /**
     * 渲染元素的背景
     */
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 默认不渲染背景
    }
    
    /**
     * 渲染元素的前景
     */
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 默认不渲染前景
    }
    
    /**
     * 渲染工具提示
     */
    public void renderToolTip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        List<Component> tooltips = getTooltips(mouseX, mouseY);
        if (!tooltips.isEmpty()) {
            guiGraphics.renderTooltip(screen.getMinecraft().font, tooltips, java.util.Optional.empty(), mouseX, mouseY);
        }
    }
    
    /**
     * 获取工具提示文本
     */
    protected List<Component> getTooltips(int mouseX, int mouseY) {
        return List.of();
    }
    
    /**
     * 更新元素状态
     */
    public void tick() {
        // 默认不更新状态
    }
    
    /**
     * 当窗口关闭时调用
     */
    public void onWindowClose() {
        // 默认不处理
    }
    
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return active && visible && 
               mouseX >= x && mouseX < x + width && 
               mouseY >= y && mouseY < y + height;
    }
    
    @Override
    public boolean isFocused() {
        return screen.getFocused() == this;
    }
    
    @Override
    public void setFocused(boolean focused) {
        // 焦点管理由屏幕处理
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public int getRight() {
        return x + width;
    }
    
    public int getBottom() {
        return y + height;
    }
    
    /**
     * 获取相对X坐标（相对于GUI左上角）
     */
    public int getRelativeX() {
        return x - screen.getGuiLeft();
    }
    
    /**
     * 获取相对Y坐标（相对于GUI左上角）
     */
    public int getRelativeY() {
        return y - screen.getGuiTop();
    }
}