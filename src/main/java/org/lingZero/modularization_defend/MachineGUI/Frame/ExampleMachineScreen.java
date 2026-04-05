package org.lingZero.modularization_defend.MachineGUI.Frame;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

/**
 * 示例机器GUI屏幕
 * 演示如何使用BaseMachineScreen框架创建类似于Mekanism的机器界面
 */
public class ExampleMachineScreen extends BaseMachineScreen<AbstractContainerMenu> {
    
    // 配置按钮
    private ConfigButton upgradeButton;
    private ConfigButton energyButton;
    private ConfigButton sideConfigButton;
    
    // 升级配置窗口
    private UpgradeConfigWindow upgradeWindow;
    
    public ExampleMachineScreen(AbstractContainerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;  // 标准GUI宽度
        this.imageHeight = 166; // 标准GUI高度
        this.dynamicSlots = true; // 启用动态槽位
    }
    
    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        
        // 添加配置按钮（右侧）
        int buttonStartX = leftPos + imageWidth + 5;
        int buttonStartY = topPos + 10;
        
        upgradeButton = new ConfigButton(this, buttonStartX, buttonStartY, 20, 20, 
                Component.literal("↑"), ConfigButton.Type.UPGRADE, this::onUpgradeButtonClicked);
        energyButton = new ConfigButton(this, buttonStartX, buttonStartY + 25, 20, 20, 
                Component.literal("⚡"), ConfigButton.Type.ENERGY, this::onEnergyButtonClicked);
        sideConfigButton = new ConfigButton(this, buttonStartX, buttonStartY + 50, 20, 20, 
                Component.literal("⚙"), ConfigButton.Type.SIDE_CONFIG, this::onSideConfigButtonClicked);

        //未修复错误
        //addRenderableWidget(upgradeButton);
        //addRenderableWidget(energyButton);
        //addRenderableWidget(sideConfigButton);
        
        // 添加能量显示条（可选）
        addEnergyGauge();
    }
    
    /**
     * 添加能量显示条
     */
    private void addEnergyGauge() {
        // 这里可以添加能量显示条元素
        // 例如：EnergyGauge energyGauge = new EnergyGauge(this, leftPos + 8, topPos + 20);
        // addRenderableWidget(energyGauge);
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
        // String energyText = "能量: 1000/10000 RF";
        // guiGraphics.drawString(font, energyText, 8, 70, 0x404040);
        
        // 渲染处理进度
        // String progressText = "进度: 50%";
        // guiGraphics.drawString(font, progressText, 8, 80, 0x404040);
    }
    
    /**
     * 升级按钮点击事件
     */
    private void onUpgradeButtonClicked() {
        if (upgradeWindow == null || !upgradeWindow.isVisible()) {
            // 创建升级配置窗口
            upgradeWindow = new UpgradeConfigWindow(this, 
                    leftPos + imageWidth + 30, 
                    topPos + 10);
            addWindow(upgradeWindow);
        } else {
            // 关闭升级窗口
            removeWindow(upgradeWindow);
            upgradeWindow = null;
        }
    }
    
    /**
     * 能量按钮点击事件
     */
    private void onEnergyButtonClicked() {
        // 这里可以打开能量配置窗口
        System.out.println("能量配置按钮被点击");
    }
    
    /**
     * 侧面配置按钮点击事件
     */
    private void onSideConfigButtonClicked() {
        // 这里可以打开侧面配置窗口
        System.out.println("侧面配置按钮被点击");
    }
    
    /**
     * 配置按钮类
     * 用于右侧的配置按钮
     */
    private static class ConfigButton extends MachineElement {
        
        public enum Type {
            UPGRADE, ENERGY, SIDE_CONFIG
        }
        
        private final Component text;
        private final Type type;
        private final Runnable onClick;
        private boolean pressed = false;
        
        public ConfigButton(BaseMachineScreen<?> screen, int x, int y, int width, int height, 
                           Component text, Type type, Runnable onClick) {
            super(screen, x, y, width, height);
            this.text = text;
            this.type = type;
            this.onClick = onClick;
        }
        
        @Override
        public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // 渲染按钮背景
            int bgColor = pressed ? 0xFF606060 : (isMouseOver(mouseX, mouseY) ? 0xFF808080 : 0xFF404040);
            guiGraphics.fill(x, y, x + width, y + height, bgColor);
            
            // 渲染按钮边框
            int borderColor = getBorderColor();
            guiGraphics.fill(x, y, x + width, y + 1, borderColor);
            guiGraphics.fill(x, y + height - 1, x + width, y + height, borderColor);
            guiGraphics.fill(x, y, x + 1, y + height, borderColor);
            guiGraphics.fill(x + width - 1, y, x + width, y + height, borderColor);
            
            // 渲染按钮文字
            int textWidth = screen.getMinecraft().font.width(text);
            int textX = x + (width - textWidth) / 2;
            int textY = y + (height - 9) / 2;
            guiGraphics.drawString(screen.getMinecraft().font, text, textX, textY, 0xFFFFFF);
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isMouseOver(mouseX, mouseY) && button == 0) {
                pressed = true;
                onClick.run();
                return true;
            }
            return false;
        }

        public void onRelease(double mouseX, double mouseY) {
            pressed = false;
        }
        
        /**
         * 获取边框颜色
         */
        private int getBorderColor() {
            return switch (type) {
                case UPGRADE -> 0xFF00FFFF;  // 青色
                case ENERGY -> 0xFFFFFF00;   // 黄色
                case SIDE_CONFIG -> 0xFFFF00FF; // 紫色
            };
        }
        
        @Override
        protected java.util.List<Component> getTooltips(int mouseX, int mouseY) {
            String tooltip = switch (type) {
                case UPGRADE -> "升级配置";
                case ENERGY -> "能量配置";
                case SIDE_CONFIG -> "侧面配置";
            };
            return java.util.List.of(Component.literal(tooltip));
        }
    }
}