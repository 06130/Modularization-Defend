package org.lingZero.modularization_defend.Blocks.ElectricityRepeater;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 电力中继器 GUI 屏幕
 * 使用 MCreator 生成的纹理实现 Mekanism 风格
 */
public class ElectricityRepeaterScreen extends AbstractContainerScreen<ElectricityRepeaterMenu> {
    
    // GUI 纹理
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath("modularization_defend", "textures/gui/electricity_repeater_screen.png");
    
    // GUI 尺寸
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;
    
    // 纹理图片实际尺寸（用于 UV 映射）
    private static final int TEXTURE_WIDTH = 176;
    private static final int TEXTURE_HEIGHT = 166;
    
    // 能量槽位置（基于纹理）
    private static final int ENERGY_BAR_X = 175;
    private static final int ENERGY_BAR_Y = 15;
    private static final int ENERGY_BAR_WIDTH = 4;
    private static final int ENERGY_BAR_HEIGHT = 52;
    
    // 信息面板位置
    private static final int INFO_PANEL_X = 55;
    private static final int INFO_PANEL_Y = 20;
    
    // 状态显示位置
    private static final int STATUS_X = 18;
    private static final int STATUS_Y = 35;
    
    // 功能按钮位置
    private static final int UPGRADE_BUTTON_X = 152;
    private static final int UPGRADE_BUTTON_Y = 10;
    private static final int ACCESS_BUTTON_X = 152;
    private static final int ACCESS_BUTTON_Y = 34;
    private static final int SIGNAL_BUTTON_X = 152;
    private static final int SIGNAL_BUTTON_Y = 58;
    
    // 颜色
    private static final int ENERGY_FILLED_COLOR = 0x00FFAA; // 青绿色
    private static final int ENERGY_EMPTY_COLOR = 0x000000;   // 黑色
    private static final int TEXT_GREEN = 0x00FF00;           // 绿色文字
    private static final int STATUS_GREEN = 0x00AA00;         // 状态绿
    
    public ElectricityRepeaterScreen(ElectricityRepeaterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        // 调整物品栏标签位置
        this.inventoryLabelY = this.imageHeight - 94;
    }
    
    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 4;
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
        
        // 渲染背景纹理（指定纹理尺寸以避免错位）
        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, GUI_WIDTH, GUI_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        
        // 渲染能量槽（使用纯色填充）
        renderEnergyBar(guiGraphics, x, y);
    }
    
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 渲染标题
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        
        // 渲染物品栏标签
        guiGraphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
        
        // 渲染信息面板文本
        renderInfoText(guiGraphics);
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
        
        // 能量槽背景（黑色）
        guiGraphics.fill(
                x + ENERGY_BAR_X, y + ENERGY_BAR_Y,
                x + ENERGY_BAR_X + ENERGY_BAR_WIDTH, y + ENERGY_BAR_Y + ENERGY_BAR_HEIGHT,
                ENERGY_EMPTY_COLOR);
        
        // 能量填充（青绿色，从下往上）
        if (filledHeight > 0) {
            guiGraphics.fill(
                    x + ENERGY_BAR_X, y + ENERGY_BAR_Y + ENERGY_BAR_HEIGHT - filledHeight,
                    x + ENERGY_BAR_X + ENERGY_BAR_WIDTH, y + ENERGY_BAR_Y + ENERGY_BAR_HEIGHT,
                    ENERGY_FILLED_COLOR);
        }
    }
    
    /**
     * 渲染信息面板文本
     */
    private void renderInfoText(GuiGraphics guiGraphics) {
        int energy = menu.getEnergy();
        int maxEnergy = menu.getMaxEnergy();
        String energyText = String.format("%d/%d FE", energy, maxEnergy);
        guiGraphics.drawString(font, energyText, INFO_PANEL_X, INFO_PANEL_Y, TEXT_GREEN, false);
        
        // 渲染功率信息（预留）
        String powerText = "功率：0 FE/t";
        guiGraphics.drawString(font, powerText, INFO_PANEL_X, INFO_PANEL_Y + 10, TEXT_GREEN, false);
        
        // 渲染输出信息（预留）
        String outputText = "输出：0 FE/t";
        guiGraphics.drawString(font, outputText, INFO_PANEL_X, INFO_PANEL_Y + 20, TEXT_GREEN, false);
        
        // 渲染电网状态（预留）
        String gridStatus = "电网：未连接";
        guiGraphics.drawString(font, gridStatus, INFO_PANEL_X, INFO_PANEL_Y + 30, TEXT_GREEN, false);
    }
}
