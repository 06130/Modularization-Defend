package org.lingZero.modularization_defend.Blocks.Multiblock.ElectricityRepeater;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lingZero.modularization_defend.Config;

/**
 * 电力中继器 GUI 屏幕
 * 使用 MCreator 生成的纹理实现 Mekanism 风格
 */
//TODO : 添加 功率曲线
public class ElectricityRepeaterScreen extends AbstractContainerScreen<ElectricityRepeaterMenu> {
    
    // GUI 纹理
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath("modularization_defend", "textures/gui/electricity_repeater_screen.png");
    
    // GUI 尺寸
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;
    
    // 纹理图片实际尺寸（用于 UV 映射）
    private static final int TEXTURE_WIDTH = GUI_WIDTH;
    private static final int TEXTURE_HEIGHT = GUI_HEIGHT;
    
    // 能量槽位置（基于纹理）
    private static final int ENERGY_BAR_X = 110;
    private static final int ENERGY_BAR_Y = 8;
    private static final int ENERGY_BAR_WIDTH = 4;
    private static final int ENERGY_BAR_HEIGHT = 52;
    
    // 信息面板位置
    private static final int INFO_PANEL_X = 115;//45
    private static final int INFO_PANEL_Y = 10;
    
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
    private static final int TEXT_GREEN = Config.textColour;           // 绿色文字
    private static final int STATUS_GREEN = 0x00AA00;         // 状态绿
    
    // 信息面板缩放比例 (0.9 = 90%)
    private static final float INFO_TEXT_SCALE = 0.7f;
    
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
        this.titleLabelX = 8;
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
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY + 2, 0x404040, false);
        
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
        PoseStack poseStack = guiGraphics.pose();
            
        // 保存当前的变换矩阵
        poseStack.pushPose();
            
        // 应用缩放
        poseStack.scale(INFO_TEXT_SCALE, INFO_TEXT_SCALE, 0.7f);
            
        // 计算缩放后的偏移量，确保文本从正确位置开始渲染
        int scaledX = (int) (INFO_PANEL_X / INFO_TEXT_SCALE);
        int scaledY = (int) (INFO_PANEL_Y / INFO_TEXT_SCALE);
        int lineHeight = (int) (7 / INFO_TEXT_SCALE); // 行距也按比例缩放
            
        // 使用翻译键显示本地化文本
        guiGraphics.drawString(font,
                Component.translatable("gui.modularization_defend.electricity_repeater.messageText").getString()
                        , scaledX, scaledY, TEXT_GREEN, false);
            
        guiGraphics.drawString(font,
                Component.translatable("gui.modularization_defend.electricity_repeater.powerText").getString()
                        + "0" + "Wh", scaledX, scaledY + lineHeight, TEXT_GREEN, false);
            
        guiGraphics.drawString(font,
                Component.translatable("gui.modularization_defend.electricity_repeater.timeRemaining").getString()
                        + "0 s", scaledX, scaledY + lineHeight * 2, TEXT_GREEN, false);
            
        guiGraphics.drawString(font,
                Component.translatable("gui.modularization_defend.electricity_repeater.limitText").getString()
                        + "0 Wh", scaledX, scaledY + lineHeight * 3, TEXT_GREEN, false);
            
        guiGraphics.drawString(font,
                Component.translatable("gui.modularization_defend.electricity_repeater.onlinePower").getString()
                        + "0 W", scaledX, scaledY + lineHeight * 4, TEXT_GREEN, false);
            
        guiGraphics.drawString(font,
                Component.translatable("gui.modularization_defend.electricity_repeater.loadPower").getString()
                        + "0 W", scaledX, scaledY + lineHeight * 5, TEXT_GREEN, false);
            
        // 协议网络状态（暂时显示未连接，将来会根据实际连接状态动态显示）
        guiGraphics.drawString(font, Component.translatable("gui.modularization_defend.electricity_repeater.agreementNetwork1").getString() + 
            Component.translatable("gui.modularization_defend.electricity_repeater.agreementNetwork1_1").getString(), 
            scaledX, scaledY + lineHeight * 6, TEXT_GREEN, false);
            
        // 恢复变换矩阵
        poseStack.popPose();
    }
}

