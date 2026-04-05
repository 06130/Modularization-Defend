package org.lingZero.modularization_defend.MachineGUI.Frame;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 机器GUI窗口类，提供可拖动的悬浮窗功能
 * 类似于Mekanism的GuiWindow
 */
public class MachineWindow extends MachineElement {
    
    // 窗口背景纹理
    private final ResourceLocation windowTexture;
    
    // 窗口状态
    private double dragX, dragY;
    private int prevDX, prevDY;
    private boolean dragging = false;
    private boolean pinned = false;
    
    // 事件监听器
    private Consumer<MachineWindow> closeListener;
    private Consumer<MachineWindow> focusLostListener;
    private Consumer<MachineWindow> focusGainedListener;
    
    // 交互策略
    protected InteractionStrategy interactionStrategy = InteractionStrategy.CONTAINER;
    
    // 子元素
    private final List<MachineElement> children = new ArrayList<>();
    
    public enum InteractionStrategy {
        NONE,       // 不允许任何交互
        CONTAINER,  // 只允许与容器槽位交互
        ALL         // 允许所有交互
    }
    
    public MachineWindow(BaseMachineScreen<?> screen, int x, int y, int width, int height) {
        this(screen, x, y, width, height, BaseMachineScreen.BASE_BACKGROUND);
    }
    
    public MachineWindow(BaseMachineScreen<?> screen, int x, int y, int width, int height, ResourceLocation windowTexture) {
        super(screen, x, y, width, height);
        this.windowTexture = windowTexture;
    }
    
    /**
     * 添加子元素到窗口
     */
    public <T extends MachineElement> T addChild(T child) {
        children.add(child);
        return child;
    }
    
    /**
     * 移除子元素从窗口
     */
    public void removeChild(MachineElement child) {
        children.remove(child);
    }
    
    /**
     * 获取所有子元素
     */
    public List<MachineElement> getChildren() {
        return new ArrayList<>(children);
    }
    
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 渲染窗口背景
        renderWindowBackground(guiGraphics);
        
        // 渲染所有子元素的背景
        for (MachineElement child : children) {
            if (child.isVisible()) {
                child.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
            }
        }
    }
    
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 渲染窗口标题栏
        renderTitleBar(guiGraphics);
        
        // 渲染所有子元素的前景
        for (MachineElement child : children) {
            if (child.isVisible()) {
                child.renderForeground(guiGraphics, mouseX, mouseY);
            }
        }
    }
    
    public void renderToolTip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 首先检查子元素的工具提示
        for (MachineElement child : children) {
            if (child.isVisible() && child.isMouseOver(mouseX, mouseY)) {
                child.renderToolTip(guiGraphics, mouseX, mouseY);
                return;
            }
        }
    }
    
    public void tick() {
        // 更新所有子元素
        for (MachineElement child : children) {
            child.tick();
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 首先检查子元素点击
        for (MachineElement child : children) {
            if (child.isActive() && child.isVisible() && child.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        
        // 然后检查窗口本身的拖拽区域（标题栏）
        if (isMouseOver(mouseX, mouseY) && mouseY < y + 18) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                dragging = true;
                dragX = mouseX;
                dragY = mouseY;
                prevDX = 0;
                prevDY = 0;
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double mouseXOld, double mouseYOld) {
        if (dragging && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            int newDX = (int) Math.round(mouseX - dragX);
            int newDY = (int) Math.round(mouseY - dragY);
            int changeX = clamp(newDX - prevDX, -x, screen.getMinecraft().getWindow().getGuiScaledWidth() - (x + width));
            int changeY = clamp(newDY - prevDY, -y, screen.getMinecraft().getWindow().getGuiScaledHeight() - (y + height));
            prevDX = newDX;
            prevDY = newDY;
            move(changeX, changeY);
        }
        
        // 传递给子元素
        for (MachineElement child : children) {
            if (child.isActive() && child.isVisible()) {
                child.mouseDragged(mouseX, mouseY, button, mouseXOld, mouseYOld);
            }
        }
        
        return true;
    }
    
    public void onRelease(double mouseX, double mouseY) {
        dragging = false;
        
        // 传递给子元素
        for (MachineElement child : children) {
            if (child instanceof MachineWindow window) {
                window.onRelease(mouseX, mouseY);
            }
        }
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC键关闭窗口（如果未固定）
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && !pinned) {
            close();
            return true;
        }
        
        // 传递给子元素
        for (MachineElement child : children) {
            if (child.isActive() && child.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        // 传递给子元素
        for (MachineElement child : children) {
            if (child.isActive() && child.charTyped(codePoint, modifiers)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // 传递给子元素
        for (MachineElement child : children) {
            if (child.isActive() && child.isVisible() && child.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
                return true;
            }
        }
        return false;
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
        if (focused) {
            screen.setFocused(this);
            onFocused();
        } else {
            if (screen.getFocused() == this) {
                screen.setFocused(null);
            }
            onFocusLost();
        }
    }
    
    /**
     * 渲染窗口背景
     */
    protected void renderWindowBackground(GuiGraphics guiGraphics) {
        // 渲染窗口背景纹理
        screen.renderBackgroundTexture(guiGraphics, windowTexture, 4, 4, x, y, width, height, 256, 256);
        
        // 渲染阴影效果
        renderShadow(guiGraphics);
    }
    
    /**
     * 渲染阴影效果
     */
    protected void renderShadow(GuiGraphics guiGraphics) {
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 0.75F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        screen.renderBackgroundTexture(guiGraphics, BaseMachineScreen.SHADOW, 4, 4, x - 3, y - 3, width + 6, height + 6, 256, 256);
        RenderSystem.disableBlend();
    }
    
    /**
     * 渲染标题栏
     */
    protected void renderTitleBar(GuiGraphics guiGraphics) {
        // 渲染标题栏背景（稍微不同的颜色）
        guiGraphics.fill(x + 4, y + 4, x + width - 4, y + 18, 0xC0000000);
        
        // 渲染标题
        Component title = getTitle();
        if (title != null) {
            String titleText = title.getString();
            int titleWidth = screen.getMinecraft().font.width(titleText);
            int titleX = x + (width - titleWidth) / 2;
            int titleY = y + 6;
            guiGraphics.drawString(screen.getMinecraft().font, titleText, titleX, titleY, 0xFFFFFF);
        }
    }
    
    /**
     * 获取窗口标题
     */
    @Nullable
    protected Component getTitle() {
        return null;
    }
    
    /**
     * 移动窗口
     */
    public void move(int deltaX, int deltaY) {
        x += deltaX;
        y += deltaY;
        
        // 同时移动所有子元素
        for (MachineElement child : children) {
            child.x += deltaX;
            child.y += deltaY;
        }
    }
    
    /**
     * 关闭窗口
     */
    public void close() {
        screen.removeWindow(this);
        if (closeListener != null) {
            closeListener.accept(this);
        }
    }
    
    /**
     * 当窗口获得焦点时调用
     */
    public void onFocused() {
        if (focusGainedListener != null) {
            focusGainedListener.accept(this);
        }
    }
    
    /**
     * 当窗口失去焦点时调用
     */
    public void onFocusLost() {
        dragging = false;
        if (focusLostListener != null) {
            focusLostListener.accept(this);
        }
    }
    
    /**
     * 设置关闭监听器
     */
    public void setCloseListener(Consumer<MachineWindow> listener) {
        this.closeListener = listener;
    }
    
    /**
     * 设置焦点变化监听器
     */
    public void setFocusListeners(Consumer<MachineWindow> lostListener, Consumer<MachineWindow> gainedListener) {
        this.focusLostListener = lostListener;
        this.focusGainedListener = gainedListener;
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
    
    public boolean isPinned() {
        return pinned;
    }
    
    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }
    
    public void togglePinned() {
        this.pinned = !this.pinned;
    }
    
    public InteractionStrategy getInteractionStrategy() {
        return interactionStrategy;
    }
    
    public void setInteractionStrategy(InteractionStrategy strategy) {
        this.interactionStrategy = strategy;
    }
    
    /**
     * 数值钳制函数
     */
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}