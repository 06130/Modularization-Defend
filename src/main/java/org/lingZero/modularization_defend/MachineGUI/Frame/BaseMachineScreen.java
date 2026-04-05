package org.lingZero.modularization_defend.MachineGUI.Frame;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 机器GUI的基础类，提供类似于Mekanism的GUI框架功能
 * 包含窗口管理、元素渲染、拖拽支持等核心功能
 */
public abstract class BaseMachineScreen<MENU extends AbstractContainerMenu> extends AbstractContainerScreen<MENU> {
    
    // GUI背景纹理
    public static final ResourceLocation BASE_BACKGROUND = ResourceLocation.fromNamespaceAndPath("modularization_defend", "textures/gui/base.png");
    public static final ResourceLocation SHADOW = ResourceLocation.fromNamespaceAndPath("modularization_defend", "textures/gui/shadow.png");
    public static final ResourceLocation BLUR = ResourceLocation.fromNamespaceAndPath("modularization_defend", "textures/gui/blur.png");
    
    // GUI配置
    protected boolean dynamicSlots = false;
    protected final List<MachineWindow> windows = new ArrayList<>();
    
    public BaseMachineScreen(MENU menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }
    
    @Override
    protected void init() {
        super.init();
        addGuiElements();
    }
    
    /**
     * 添加GUI元素到界面中
     * 子类应该重写此方法来添加特定的GUI元素
     */
    protected void addGuiElements() {
        // 默认不添加任何元素，由子类实现
    }
    
    @Override
    public void removed() {
        // 关闭所有窗口（创建副本列表以避免 ConcurrentModificationException）
        new ArrayList<>(windows).forEach(MachineWindow::close);
        super.removed();
    }
    
    @Override
    public void containerTick() {
        super.containerTick();
        // 更新所有窗口
        windows.forEach(MachineWindow::tick);
    }
    
    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 渲染标题
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        
        // 渲染物品栏文字
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
        
        // 渲染窗口
        for (MachineWindow window : windows) {
            window.renderForeground(guiGraphics, mouseX, mouseY);
        }
        
        // 渲染工具提示
        MachineWindow hoveredWindow = getWindowHovering(mouseX, mouseY);
        if (hoveredWindow != null) {
            hoveredWindow.renderToolTip(guiGraphics, mouseX, mouseY);
        }
    }
    
    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // 渲染背景
        renderBackgroundTexture(guiGraphics, BASE_BACKGROUND, 4, 4, leftPos, topPos, imageWidth, imageHeight, 256, 256);
    }
    
    /**
     * 渲染背景纹理
     */
    protected void renderBackgroundTexture(GuiGraphics guiGraphics, ResourceLocation texture, int textureU, int textureV, 
                                          int x, int y, int width, int height, int textureWidth, int textureHeight) {
        guiGraphics.blit(texture, x, y, textureU, textureV, width, height, textureWidth, textureHeight);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 首先检查窗口点击
        for (MachineWindow window : windows) {
            if (window.mouseClicked(mouseX, mouseY, button)) {
                setFocused(window);
                if (button == org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    setDragging(true);
                }
                return true;
            }
        }
        
        // 然后检查普通元素
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // 释放拖拽
        for (MachineWindow window : windows) {
            window.onRelease(mouseX, mouseY);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double mouseXOld, double mouseYOld) {
        super.mouseDragged(mouseX, mouseY, button, mouseXOld, mouseYOld);
        return getFocused() != null && isDragging() && button == org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT && 
               getFocused().mouseDragged(mouseX, mouseY, button, mouseXOld, mouseYOld);
    }
    
    /**
     * 添加窗口到GUI
     */
    public void addWindow(MachineWindow window) {
        windows.add(window);
        window.onFocused();
    }
    
    /**
     * 移除窗口从GUI
     */
    public void removeWindow(MachineWindow window) {
        windows.remove(window);
        window.onFocusLost();
        if (getFocused() == window) {
            setFocused(null);
        }
    }
    
    /**
     * 获取鼠标悬停的窗口
     */
    @Nullable
    public MachineWindow getWindowHovering(double mouseX, double mouseY) {
        // 从后往前检查，最后添加的窗口在最上层
        for (int i = windows.size() - 1; i >= 0; i--) {
            MachineWindow window = windows.get(i);
            if (window.isMouseOver(mouseX, mouseY)) {
                return window;
            }
        }
        return null;
    }
    
    /**
     * 获取GUI左上角的X坐标
     */
    public int getGuiLeft() {
        return leftPos;
    }
    
    /**
     * 获取GUI左上角的Y坐标
     */
    public int getGuiTop() {
        return topPos;
    }
    
    /**
     * 获取GUI的宽度
     */
    public int getGuiWidth() {
        return imageWidth;
    }
    
    /**
     * 获取GUI的高度
     */
    public int getGuiHeight() {
        return imageHeight;
    }
}