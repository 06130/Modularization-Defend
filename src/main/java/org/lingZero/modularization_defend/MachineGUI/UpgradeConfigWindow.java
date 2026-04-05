package org.lingZero.modularization_defend.MachineGUI;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * 升级配置窗口
 * 提供一个可拖动的悬浮窗，用于管理和安装升级
 * 类似于Mekanism的升级界面
 */
public class UpgradeConfigWindow extends MachineWindow {
    
    // 升级类型定义
    public enum UpgradeType {
        SPEED("速度升级", "提升机器处理速度", 0xFF00FF00),
        ENERGY("能量升级", "减少能量消耗", 0xFFFFFF00),
        EFFICIENCY("效率升级", "提高处理效率", 0xFF00FFFF),
        CAPACITY("容量升级", "增加内部缓存", 0xFFFF00FF),
        SECURITY("安全升级", "增强安全性", 0xFFFF0000);
        
        private final String name;
        private final String description;
        private final int color;
        
        UpgradeType(String name, String description, int color) {
            this.name = name;
            this.description = description;
            this.color = color;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public int getColor() {
            return color;
        }
    }
    
    // 升级信息类
    public static class UpgradeInfo {
        private final UpgradeType type;
        private int level;
        private final int maxLevel;
        
        public UpgradeInfo(UpgradeType type, int maxLevel) {
            this.type = type;
            this.level = 0;
            this.maxLevel = maxLevel;
        }
        
        public UpgradeType getType() {
            return type;
        }
        
        public int getLevel() {
            return level;
        }
        
        public void setLevel(int level) {
            this.level = Math.max(0, Math.min(level, maxLevel));
        }
        
        public int getMaxLevel() {
            return maxLevel;
        }
        
        public boolean canInstall() {
            return level < maxLevel;
        }
        
        public boolean canRemove() {
            return level > 0;
        }
    }
    
    // UI元素
    private final List<UpgradeSlot> upgradeSlots = new ArrayList<>();
    private final List<UpgradeInfo> installedUpgrades = new ArrayList<>();
    private UpgradeInfo selectedUpgrade = null;
    
    // 槽位和界面元素
    private MachineSlot inputSlot;
    private MachineSlot outputSlot;
    private InfoPanel infoPanel;
    
    // 按钮
    private Button installButton;
    private Button removeButton;
    private Button closeButton;
    
    public UpgradeConfigWindow(BaseMachineScreen<?> screen, int x, int y) {
        super(screen, x, y, 200, 180);
        initializeUpgrades();
        createUIElements();
    }
    
    /**
     * 初始化升级类型
     */
    private void initializeUpgrades() {
        for (UpgradeType type : UpgradeType.values()) {
            installedUpgrades.add(new UpgradeInfo(type, 4)); // 最大等级为4
        }
    }
    
    /**
     * 创建UI元素
     */
    private void createUIElements() {
        // 创建升级槽位（左侧）
        int startX = 10;
        int startY = 25;
        for (int i = 0; i < installedUpgrades.size(); i++) {
            UpgradeInfo info = installedUpgrades.get(i);
            UpgradeSlot slot = new UpgradeSlot(screen, startX, startY + i * 22, info);
            upgradeSlots.add(slot);
            addChild(slot);
        }
        
        // 创建信息面板（右侧）
        infoPanel = new InfoPanel(screen, 80, 25, 110, 100);
        addChild(infoPanel);
        
        // 创建输入/输出槽位
        inputSlot = new MachineSlot(screen, 80, 130, MachineSlot.SlotType.UPGRADE);
        outputSlot = new MachineSlot(screen, 140, 130, MachineSlot.SlotType.UPGRADE);
        addChild(inputSlot);
        addChild(outputSlot);
        
        // 创建按钮
        installButton = new Button(screen, 80, 155, 50, 15, "安装", this::onInstall);
        removeButton = new Button(screen, 140, 155, 50, 15, "移除", this::onRemove);
        closeButton = new Button(screen, 160, 5, 20, 10, "X", this::onClose);
        
        addChild(installButton);
        addChild(removeButton);
        addChild(closeButton);
        
        updateButtonStates();
    }
    
    @Override
    protected Component getTitle() {
        return Component.literal("升级配置");
    }
    
    /**
     * 安装升级
     */
    private void onInstall() {
        if (selectedUpgrade != null && selectedUpgrade.canInstall()) {
            ItemStack inputStack = inputSlot.getStack();
            if (!inputStack.isEmpty() && isValidUpgradeItem(inputStack, selectedUpgrade.getType())) {
                selectedUpgrade.setLevel(selectedUpgrade.getLevel() + 1);
                inputStack.shrink(1);
                updateButtonStates();
                updateInfoPanel();
            }
        }
    }
    
    /**
     * 移除升级
     */
    private void onRemove() {
        if (selectedUpgrade != null && selectedUpgrade.canRemove()) {
            selectedUpgrade.setLevel(selectedUpgrade.getLevel() - 1);
            
            // 将升级物品放入输出槽
            ItemStack outputStack = createUpgradeItem(selectedUpgrade.getType());
            if (outputSlot.isEmpty()) {
                outputSlot.setStack(outputStack);
            } else if (outputSlot.getStack().is(outputStack.getItem())) {
                outputSlot.getStack().grow(1);
            }
            
            updateButtonStates();
            updateInfoPanel();
        }
    }
    
    /**
     * 关闭窗口
     */
    private void onClose() {
        close();
    }
    
    /**
     * 检查是否为有效的升级物品
     */
    private boolean isValidUpgradeItem(ItemStack stack, UpgradeType type) {
        // 这里应该根据实际的升级物品来判断
        String itemName = stack.getItem().toString().toLowerCase();
        return itemName.contains(type.name().toLowerCase());
    }
    
    /**
     * 创建升级物品
     */
    private ItemStack createUpgradeItem(UpgradeType type) {
        // 这里应该根据升级类型创建相应的物品
        return ItemStack.EMPTY; // 临时返回空栈
    }
    
    /**
     * 更新按钮状态
     */
    private void updateButtonStates() {
        installButton.setActive(selectedUpgrade != null && selectedUpgrade.canInstall() && !inputSlot.isEmpty());
        removeButton.setActive(selectedUpgrade != null && selectedUpgrade.canRemove());
    }
    
    /**
     * 更新信息面板
     */
    private void updateInfoPanel() {
        if (selectedUpgrade != null) {
            infoPanel.setUpgradeInfo(selectedUpgrade);
        } else {
            infoPanel.clearInfo();
        }
    }
    
    /**
     * 选择升级
     */
    public void selectUpgrade(UpgradeInfo info) {
        this.selectedUpgrade = info;
        updateButtonStates();
        updateInfoPanel();
        
        // 高亮选中的槽位
        for (UpgradeSlot slot : upgradeSlots) {
            slot.setSelected(slot.getUpgradeInfo() == info);
        }
    }
    
    /**
     * 升级槽位类
     */
    private static class UpgradeSlot extends MachineElement {
        private final UpgradeInfo upgradeInfo;
        private boolean selected = false;
        
        public UpgradeSlot(BaseMachineScreen<?> screen, int x, int y, UpgradeInfo upgradeInfo) {
            super(screen, x, y, 60, 20);
            this.upgradeInfo = upgradeInfo;
        }
        
        @Override
        public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // 渲染背景
            int bgColor = selected ? 0xFF404040 : (isMouseOver(mouseX, mouseY) ? 0xFF606060 : 0xFF303030);
            guiGraphics.fill(x, y, x + width, y + height, bgColor);
            
            // 渲染边框
            int borderColor = upgradeInfo.getType().getColor();
            guiGraphics.fill(x, y, x + width, y + 1, borderColor);
            guiGraphics.fill(x, y + height - 1, x + width, y + height, borderColor);
            guiGraphics.fill(x, y, x + 1, y + height, borderColor);
            guiGraphics.fill(x + width - 1, y, x + width, y + height, borderColor);
            
            // 渲染升级名称和等级
            String text = upgradeInfo.getType().getName() + " Lv." + upgradeInfo.getLevel();
            guiGraphics.drawString(screen.font, text, x + 5, y + 6, 0xFFFFFF);
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isMouseOver(mouseX, mouseY) && button == 0) {
                ((UpgradeConfigWindow) screen).selectUpgrade(upgradeInfo);
                return true;
            }
            return false;
        }
        
        public UpgradeInfo getUpgradeInfo() {
            return upgradeInfo;
        }
        
        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }
    
    /**
     * 信息面板类
     */
    private static class InfoPanel extends MachineElement {
        private UpgradeInfo currentInfo = null;
        
        public InfoPanel(BaseMachineScreen<?> screen, int x, int y, int width, int height) {
            super(screen, x, y, width, height);
        }
        
        @Override
        public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // 渲染面板背景
            guiGraphics.fill(x, y, x + width, y + height, 0xE0000000);
            
            // 渲染边框
            guiGraphics.fill(x, y, x + width, y + 1, 0xFFFFFFFF);
            guiGraphics.fill(x, y + height - 1, x + width, y + height, 0xFFFFFFFF);
            guiGraphics.fill(x, y, x + 1, y + height, 0xFFFFFFFF);
            guiGraphics.fill(x + width - 1, y, x + width, y + height, 0xFFFFFFFF);
            
            // 渲染信息
            if (currentInfo != null) {
                renderUpgradeInfo(guiGraphics);
            } else {
                renderNoSelection(guiGraphics);
            }
        }
        
        private void renderUpgradeInfo(GuiGraphics guiGraphics) {
            int textY = y + 5;
            int textX = x + 5;
            
            // 升级名称
            String name = currentInfo.getType().getName() + " (等级 " + currentInfo.getLevel() + "/" + currentInfo.getMaxLevel() + ")";
            guiGraphics.drawString(screen.font, name, textX, textY, 0xFFFFFF);
            textY += 12;
            
            // 升级描述
            String description = currentInfo.getType().getDescription();
            List<String> wrappedLines = wrapText(description, width - 10);
            for (String line : wrappedLines) {
                guiGraphics.drawString(screen.font, line, textX, textY, 0xCCCCCC);
                textY += 10;
            }
            
            textY += 5;
            
            // 效果信息
            String effectInfo = getEffectInfo();
            List<String> effectLines = wrapText(effectInfo, width - 10);
            for (String line : effectLines) {
                guiGraphics.drawString(screen.font, line, textX, textY, 0x00FF00);
                textY += 10;
            }
        }
        
        private void renderNoSelection(GuiGraphics guiGraphics) {
            String text = "请选择一个升级查看详细信息";
            int textWidth = screen.font.width(text);
            int textX = x + (width - textWidth) / 2;
            int textY = y + (height - 9) / 2;
            guiGraphics.drawString(screen.font, text, textX, textY, 0x888888);
        }
        
        private String getEffectInfo() {
            if (currentInfo == null) return "";
            
            return switch (currentInfo.getType()) {
                case SPEED -> "处理速度 +" + (currentInfo.getLevel() * 25) + "%";
                case ENERGY -> "能量消耗 -" + (currentInfo.getLevel() * 20) + "%";
                case EFFICIENCY -> "处理效率 +" + (currentInfo.getLevel() * 15) + "%";
                case CAPACITY -> "内部缓存 +" + (currentInfo.getLevel() * 50) + "%";
                case SECURITY -> "安全等级 +" + currentInfo.getLevel();
            };
        }
        
        private List<String> wrapText(String text, int maxWidth) {
            List<String> lines = new ArrayList<>();
            String[] words = text.split(" ");
            StringBuilder currentLine = new StringBuilder();
            
            for (String word : words) {
                String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
                if (screen.font.width(testLine) <= maxWidth) {
                    currentLine = new StringBuilder(testLine);
                } else {
                    if (currentLine.length() > 0) {
                        lines.add(currentLine.toString());
                    }
                    currentLine = new StringBuilder(word);
                }
            }
            
            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
            
            return lines;
        }
        
        public void setUpgradeInfo(UpgradeInfo info) {
            this.currentInfo = info;
        }
        
        public void clearInfo() {
            this.currentInfo = null;
        }
    }
    
    /**
     * 按钮类
     */
    private static class Button extends MachineElement {
        private final String text;
        private final Runnable onClick;
        
        public Button(BaseMachineScreen<?> screen, int x, int y, int width, int height, String text, Runnable onClick) {
            super(screen, x, y, width, height);
            this.text = text;
            this.onClick = onClick;
        }
        
        @Override
        public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // 渲染按钮背景
            int bgColor = active ? (isMouseOver(mouseX, mouseY) ? 0xFF808080 : 0xFF606060) : 0xFF404040;
            guiGraphics.fill(x, y, x + width, y + height, bgColor);
            
            // 渲染按钮边框
            int borderColor = active ? 0xFFFFFFFF : 0xFF888888;
            guiGraphics.fill(x, y, x + width, y + 1, borderColor);
            guiGraphics.fill(x, y + height - 1, x + width, y + height, borderColor);
            guiGraphics.fill(x, y, x + 1, y + height, borderColor);
            guiGraphics.fill(x + width - 1, y, x + width, y + height, borderColor);
            
            // 渲染按钮文字
            int textWidth = screen.font.width(text);
            int textX = x + (width - textWidth) / 2;
            int textY = y + (height - 9) / 2;
            int textColor = active ? 0xFFFFFF : 0x888888;
            guiGraphics.drawString(screen.font, text, textX, textY, textColor);
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (active && isMouseOver(mouseX, mouseY) && button == 0) {
                onClick.run();
                return true;
            }
            return false;
        }
    }
}