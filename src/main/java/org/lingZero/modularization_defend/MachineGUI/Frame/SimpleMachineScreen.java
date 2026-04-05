package org.lingZero.modularization_defend.MachineGUI.Frame;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

/**
 * 简化的机器GUI屏幕示例
 * 演示如何使用BaseMachineScreen框架创建类似于Mekanism的机器界面
 * 这个版本专注于核心功能，避免复杂的编译错误
 */
public class SimpleMachineScreen extends BaseMachineScreen<AbstractContainerMenu> {
    
    // 升级配置窗口
    private SimpleUpgradeWindow upgradeWindow;
    
    public SimpleMachineScreen(AbstractContainerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;  // 标准GUI宽度
        this.imageHeight = 166; // 标准GUI高度
    }
    
    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        
        // 添加配置按钮（右侧）
        int buttonStartX = leftPos + imageWidth + 5;
        int buttonStartY = topPos + 10;
        
        // 创建升级按钮 - 使用原生的按钮组件
        net.minecraft.client.gui.components.Button upgradeButton = 
            net.minecraft.client.gui.components.Button.builder(
                Component.literal("↑"), 
                button -> onUpgradeButtonClicked()
            )
            .pos(buttonStartX, buttonStartY)
            .size(20, 20)
            .build();
        
        addRenderableWidget(upgradeButton);
    }
    
    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTick, mouseX, mouseY);
        
        // 可以在这里添加额外的背景渲染
        // 例如：渲染机器特定的背景元素
    }
    
    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        
        // 渲染机器特定的信息
        renderMachineInfo(guiGraphics);
    }
    
    /**
     * 渲染机器信息
     */
    private void renderMachineInfo(GuiGraphics guiGraphics) {
        // 渲染能量信息
        String energyText = "能量: 1000/10000 RF";
        guiGraphics.drawString(font, energyText, 8, 70, 0x404040, false);
        
        // 渲染处理进度
        String progressText = "进度: 50%";
        guiGraphics.drawString(font, progressText, 8, 80, 0x404040, false);
    }
    
    /**
     * 升级按钮点击事件
     */
    private void onUpgradeButtonClicked() {
        if (upgradeWindow == null || !upgradeWindow.isVisible()) {
            // 创建升级配置窗口
            upgradeWindow = new SimpleUpgradeWindow(this, 
                    leftPos + imageWidth + 30, 
                    topPos + 10);
            addWindow(upgradeWindow);
        } else {
            // 关闭升级窗口
            removeWindow(upgradeWindow);
            upgradeWindow = null;
        }
    }
}