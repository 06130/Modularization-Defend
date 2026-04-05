package org.lingZero.modularization_defend.MachineGUI;

import net.minecraft.client.gui.GuiGraphics;

/**
 * 简化的升级配置窗口
 * 提供一个可拖动的悬浮窗，用于显示升级信息
 */
public class SimpleUpgradeWindow extends MachineWindow {
    
    public SimpleUpgradeWindow(BaseMachineScreen<?> screen, int x, int y) {
        super(screen, x, y, 200, 150);
    }
    
    @Override
    protected void renderTitleBar(GuiGraphics guiGraphics) {
        // 渲染窗口标题栏背景
        guiGraphics.fill(x + 4, y + 4, x + width - 4, y + 18, 0xC0000000);
        
        // 渲染标题
        String titleText = "升级配置";
        int titleWidth = screen.getMinecraft().font.width(titleText);
        int titleX = x + (width - titleWidth) / 2;
        int titleY = y + 6;
        guiGraphics.drawString(screen.getMinecraft().font, titleText, titleX, titleY, 0xFFFFFF);
    }
    
    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        
        // 渲染升级信息
        renderUpgradeInfo(guiGraphics);
    }
    
    /**
     * 渲染升级信息
     */
    private void renderUpgradeInfo(GuiGraphics guiGraphics) {
        int startY = y + 30;
        int startX = x + 10;
        
        // 渲染升级类型列表
        String[] upgradeTypes = {"速度升级", "能量升级", "效率升级", "容量升级", "安全升级"};
        int[] levels = {2, 1, 0, 3, 1};
        
        for (int i = 0; i < upgradeTypes.length; i++) {
            String text = upgradeTypes[i] + " (等级 " + levels[i] + "/4)";
            guiGraphics.drawString(screen.getMinecraft().font, text, startX, startY + i * 12, 0xFFFFFF);
        }
        
        // 渲染说明文字
        String infoText = "点击升级查看详细信息";
        guiGraphics.drawString(screen.getMinecraft().font, infoText, startX, startY + 80, 0xCCCCCC);
    }
}