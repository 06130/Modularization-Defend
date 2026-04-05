package org.lingZero.modularization_defend.MachineGUI;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lingZero.modularization_defend.DataComponents.DefendCoreData;
import org.lingZero.modularization_defend.Items.DefendCore;
import org.lingZero.modularization_defend.MachineGUI.Frame.BaseMachineScreen;
import org.lingZero.modularization_defend.MachineGUI.Frame.SimpleUpgradeWindow;
import org.lingZero.modularization_defend.util.EnergydataConversion;
import top.theillusivec4.curios.api.CuriosApi;


public class DefendCoreItemScreen extends BaseMachineScreen<AbstractContainerMenu> {
    
    // 升级配置窗口
    private SimpleUpgradeWindow upgradeWindow;
    
    public DefendCoreItemScreen(AbstractContainerMenu menu, Inventory playerInventory, Component title) {
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
        // 优先从 Curios 饰品槽获取 DefendCore
        ItemStack stack = getEquippedDefendCore();
        
        if (stack.getItem() instanceof DefendCore) {
            DefendCoreData data = DefendCore.getData(stack);
            
            // 使用单位转换格式化能量显示
            String currentEnergyFormatted = EnergydataConversion.convertToHumanReadable(data.energyCurrent());
            String maxEnergyFormatted = EnergydataConversion.convertToHumanReadable(data.energyMax());
            
            // 渲染能量信息
            String energyText = "能量: " + currentEnergyFormatted + " / " + maxEnergyFormatted;
            guiGraphics.drawString(font, energyText, 8, 70, 0x404040, false);
            
            // 渲染升级信息摘要
            String upgradeInfo = String.format("升级: 速%d 能%d 效%d 容%d 安%d",
                data.speedUpgradeLevel(),
                data.energyUpgradeLevel(),
                data.efficiencyUpgradeLevel(),
                data.capacityUpgradeLevel(),
                data.securityUpgradeLevel()
            );
            guiGraphics.drawString(font, upgradeInfo, 8, 80, 0x404040, false);
        } else {
            guiGraphics.drawString(font, "未装备核心", 8, 70, 0xFF0000, false);
        }
    }
    
    /**
     * 获取玩家装备的 DefendCore（优先从 Curios 饰品槽）
     */
    private ItemStack getEquippedDefendCore() {
        final ItemStack[] result = {ItemStack.EMPTY};
        
        CuriosApi.getCuriosInventory(minecraft.player).ifPresent(handler -> {
            var curios = handler.findCurios(item -> item.getItem() instanceof DefendCore);
            if (!curios.isEmpty()) {
                result[0] = curios.getFirst().stack();
            }
        });
        
        return result[0];
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