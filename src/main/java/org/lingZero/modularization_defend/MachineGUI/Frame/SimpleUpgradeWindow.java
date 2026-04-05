package org.lingZero.modularization_defend.MachineGUI.Frame;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lingZero.modularization_defend.DataComponents.DefendCoreData;
import org.lingZero.modularization_defend.Items.DefendCore;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * 简化的升级配置窗口
 * 提供一个可拖动的悬浮窗，用于显示和修改升级信息
 */
public class SimpleUpgradeWindow extends MachineWindow {
    
    private static final int MAX_UPGRADE_LEVEL = 4;
    
    public SimpleUpgradeWindow(BaseMachineScreen<?> screen, int x, int y) {
        super(screen, x, y, 220, 180);
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
        
        // 优先从 Curios 饰品槽获取 DefendCore
        ItemStack stack = getEquippedDefendCore();
        
        if (!(stack.getItem() instanceof DefendCore)) {
            guiGraphics.drawString(screen.getMinecraft().font, "未检测到核心物品", startX, startY, 0xFF0000);
            return;
        }
        
        // 获取数据组件
        DefendCoreData data = DefendCore.getData(stack);
        
        // 渲染升级类型列表
        String[] upgradeTypes = {
            "速度升级",
            "能量升级",
            "效率升级",
            "容量升级",
            "安全升级"
        };
        
        int[] currentLevels = {
            data.speedUpgradeLevel(),
            data.energyUpgradeLevel(),
            data.efficiencyUpgradeLevel(),
            data.capacityUpgradeLevel(),
            data.securityUpgradeLevel()
        };
        
        String[] descriptions = {
            "射速 +" + (data.speedUpgradeLevel() * 10) + "%",
            "伤害 +" + (data.energyUpgradeLevel() * 15) + "%",
            "能耗 -" + (data.efficiencyUpgradeLevel() * 8) + "%",
            "容量 +" + (data.capacityUpgradeLevel() * 5000) + " RF",
            "安全性提升"
        };
        
        for (int i = 0; i < upgradeTypes.length; i++) {
            int yPos = startY + i * 24;
            
            // 渲染升级名称和等级
            String levelText = upgradeTypes[i] + " (" + currentLevels[i] + "/" + MAX_UPGRADE_LEVEL + ")";
            guiGraphics.drawString(screen.getMinecraft().font, levelText, startX, yPos, 0xFFFFFF);
            
            // 渲染效果描述
            guiGraphics.drawString(screen.getMinecraft().font, descriptions[i], startX + 10, yPos + 12, 0xAAAAAA);
            
            // 渲染进度条背景
            int barX = startX + 150;
            int barY = yPos + 4;
            int barWidth = 50;
            int barHeight = 8;
            guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF404040);
            
            // 渲染进度条填充
            int fillWidth = (currentLevels[i] * barWidth) / MAX_UPGRADE_LEVEL;
            int fillColor = getUpgradeColor(i);
            guiGraphics.fill(barX, barY, barX + fillWidth, barY + barHeight, fillColor);
        }
        
        // 渲染说明文字
        String infoText = "使用升级模块右键点击以提升等级";
        guiGraphics.drawString(screen.getMinecraft().font, infoText, startX, startY + 130, 0xCCCCCC);
    }
    
    /**
     * 获取升级进度条颜色
     */
    private int getUpgradeColor(int index) {
        return switch (index) {
            case 0 -> 0xFF00FFFF;  // 速度 - 青色
            case 1 -> 0xFFFF0000;  // 能量 - 红色
            case 2 -> 0xFF00FF00;  // 效率 - 绿色
            case 3 -> 0xFFFFFF00;  // 容量 - 黄色
            case 4 -> 0xFFFF00FF;  // 安全 - 紫色
            default -> 0xFFFFFFFF;
        };
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 这里可以添加点击升级的逻辑
        // 例如：检测点击了哪个升级项，然后执行升级操作
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    /**
     * 获取玩家装备的 DefendCore（优先从 Curios 饰品槽）
     */
    private ItemStack getEquippedDefendCore() {
        final ItemStack[] result = {ItemStack.EMPTY};
        
        CuriosApi.getCuriosInventory(screen.getMinecraft().player).ifPresent(handler -> {
            var curios = handler.findCurios(item -> item.getItem() instanceof DefendCore);
            if (!curios.isEmpty()) {
                result[0] = curios.getFirst().stack();
            }
        });
        
        return result[0];
    }
}