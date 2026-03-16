package org.lingZero.modularization_defend.Blocks.ElectricityRepeater;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lingZero.modularization_defend.ModularizationDefend;

/**
 * 电力中继器 GUI 屏幕
 * Mekanism 风格实现
 */
public class ElectricityRepeaterScreen extends AbstractContainerScreen<ElectricityRepeaterMenu> {
    
    // GUI 背景颜色（浅灰色）
    private static final int BACKGROUND_COLOR = 0xC6C6C6;
    // 信息面板背景颜色（深灰色）
    private static final int INFO_PANEL_COLOR = 0x8B8B8B;
    // 边框颜色
    private static final int BORDER_COLOR = 0x000000;
    
    // GUI 尺寸
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;
    
    // 元素位置
    private static final int ENERGY_BAR_X = 164;
    private static final int ENERGY_BAR_Y = 16;
    private static final int ENERGY_BAR_WIDTH = 4;
    private static final int ENERGY_BAR_HEIGHT = 52;
    
    private static final int INFO_PANEL_X = 20;
    private static final int INFO_PANEL_Y = 10;
    private static final int INFO_PANEL_WIDTH = 136;
    private static final int INFO_PANEL_HEIGHT = 60;
    
    private static final int UPGRADE_BUTTON_X = 152;
    private static final int UPGRADE_BUTTON_Y = 10;
    
    private static final int ACCESS_BUTTON_X = 152;
    private static final int ACCESS_BUTTON_Y = 34;
    
    private static final int SIGNAL_BUTTON_X = 152;
    private static final int SIGNAL_BUTTON_Y = 58;
    
    // 颜色
    private static final int ENERGY_FILLED_COLOR = 0x00FFAA; // 青绿色
    private static final int ENERGY_EMPTY_COLOR = 0x000000;   // 黑色
    private static final int INFO_TEXT_COLOR = 0x00FF00;      // 绿色
    private static final int STATUS_GREEN = 0x00AA00;         // 状态显示绿色
    
    public ElectricityRepeaterScreen(ElectricityRepeaterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        // 不在构造函数中设置 titleLabelX 和 titleLabelY，在 init() 中设置
    }
    
    @Override
    protected void init() {
        super.init();
        // 在 init() 中设置标题位置，此时 font 已初始化
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 4;
        
        // 可以在这里添加按钮等交互元素
        // 升级按钮（预留）
        // 访问权限按钮（预留）
        // 信号控制按钮（预留）
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
    
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        
        // 渲染背景（浅灰色）
        guiGraphics.fill(x, y, x + GUI_WIDTH, y + GUI_HEIGHT, BACKGROUND_COLOR);
        
        // 渲染边框
        guiGraphics.fill(x - 1, y - 1, x + GUI_WIDTH + 1, y + GUI_HEIGHT + 1, BORDER_COLOR);
        
        // 渲染信息面板背景
        guiGraphics.fill(x + INFO_PANEL_X - 2, y + INFO_PANEL_Y - 2, 
                        x + INFO_PANEL_X + INFO_PANEL_WIDTH + 2, y + INFO_PANEL_Y + INFO_PANEL_HEIGHT + 2, 
                        INFO_PANEL_COLOR);
        
        // 渲染能量槽
        renderEnergyBar(guiGraphics, x, y);
        
        // 渲染状态显示
        renderStatusDisplay(guiGraphics, x, y);
        
        // 渲染升级按钮（预留，灰色方块）
        renderPlaceholderButton(guiGraphics, x, UPGRADE_BUTTON_X, UPGRADE_BUTTON_Y, "升级");
        
        // 渲染访问权限按钮（预留）
        renderPlaceholderButton(guiGraphics, x, ACCESS_BUTTON_X, ACCESS_BUTTON_Y, "权限");
        
        // 渲染信号控制按钮（预留）
        renderPlaceholderButton(guiGraphics, x, SIGNAL_BUTTON_X, SIGNAL_BUTTON_Y, "信号");
    }
    
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 渲染标题
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        
        // 渲染物品栏标签
        guiGraphics.drawString(font, playerInventoryTitle, 8, imageHeight - 94, 0x404040, false);
        
        // 渲染信息显示
        renderInfoPanel(guiGraphics);
    }
    
    /**
     * 渲染能量槽
     */
    private void renderEnergyBar(GuiGraphics guiGraphics, int x, int y) {
        int energy = menu.getEnergy();
        int maxEnergy = menu.getMaxEnergy();
        
        // 计算填充比例
        double fillLevel = maxEnergy > 0 ? (double) energy / maxEnergy : 0;
        int filledHeight = (int) (fillLevel * ENERGY_BAR_HEIGHT);
        
        // 渲染背景（黑色）
        guiGraphics.fill(
                x + ENERGY_BAR_X, y + ENERGY_BAR_Y,
                x + ENERGY_BAR_X + ENERGY_BAR_WIDTH, y + ENERGY_BAR_Y + ENERGY_BAR_HEIGHT,
                ENERGY_EMPTY_COLOR);
        
        // 渲染填充部分（青绿色）
        if (filledHeight > 0) {
            guiGraphics.fill(
                    x + ENERGY_BAR_X, y + ENERGY_BAR_Y + ENERGY_BAR_HEIGHT - filledHeight,
                    x + ENERGY_BAR_X + ENERGY_BAR_WIDTH, y + ENERGY_BAR_Y + ENERGY_BAR_HEIGHT,
                    ENERGY_FILLED_COLOR);
        }
    }
    
    /**
     * 渲染状态显示（绿色方块）
     */
    private void renderStatusDisplay(GuiGraphics guiGraphics, int x, int y) {
        // 状态显示方块位置
        int statusX = x + 28;
        int statusY = y + 20;
        int statusSize = 16;
        
        // 渲染绿色状态方块
        guiGraphics.fill(statusX, statusY, statusX + statusSize, statusY + statusSize, STATUS_GREEN);
        
        // 渲染边框
        guiGraphics.drawString(font, "⚡", statusX + 4, statusY + 4, 0xFFFFFF);
    }
    
    /**
     * 渲染信息面板
     */
    private void renderInfoPanel(GuiGraphics guiGraphics) {
        // 信息面板背景（已在纹理中）
        
        // 渲染能量信息
        int energy = menu.getEnergy();
        int maxEnergy = menu.getMaxEnergy();
        String energyText = String.format("%d/%d FE", energy, maxEnergy);
        guiGraphics.drawString(font, energyText, INFO_PANEL_X + 50, INFO_PANEL_Y + 10, INFO_TEXT_COLOR, false);
        
        // 渲染功率信息（预留）
        String powerText = "功率：0 FE/t";
        guiGraphics.drawString(font, powerText, INFO_PANEL_X + 50, INFO_PANEL_Y + 25, INFO_TEXT_COLOR, false);
        
        // 渲染输出信息（预留）
        String outputText = "输出：0 FE/t";
        guiGraphics.drawString(font, outputText, INFO_PANEL_X + 50, INFO_PANEL_Y + 40, INFO_TEXT_COLOR, false);
        
        // 渲染电网状态（预留）
        String gridStatus = "电网：未连接";
        guiGraphics.drawString(font, gridStatus, INFO_PANEL_X + 50, INFO_PANEL_Y + 55, INFO_TEXT_COLOR, false);
    }
    
    /**
     * 渲染占位按钮
     */
    private void renderPlaceholderButton(GuiGraphics guiGraphics, int guiX, int x, int y, String label) {
        int buttonSize = 20;
        // 按钮背景（灰色）
        guiGraphics.fill(guiX + x, topPos + y, guiX + x + buttonSize, topPos + y + buttonSize, 0x8B8B8B);
        // 按钮边框
        guiGraphics.fill(guiX + x - 1, topPos + y - 1, guiX + x + buttonSize + 1, topPos + y + 1, 0xFFFFFF);
        guiGraphics.fill(guiX + x - 1, topPos + y + buttonSize, guiX + x + buttonSize + 1, topPos + y + buttonSize + 1, 0x555555);
        guiGraphics.fill(guiX + x - 1, topPos + y - 1, guiX + x + 1, topPos + y + buttonSize + 1, 0xFFFFFF);
        guiGraphics.fill(guiX + x + buttonSize, topPos + y - 1, guiX + x + buttonSize + 1, topPos + y + buttonSize + 1, 0x555555);
    }
}
