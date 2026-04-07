package org.lingZero.modularization_defend.ldlibUI;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * DefendCore 的 GUI 屏幕，继承 AbstractContainerScreen 以支持容器菜单
 */
public class NewUIScreen extends AbstractContainerScreen<DefendCoreContainerMenu> {

    private final ModularUI modularUI;

    public NewUIScreen(DefendCoreContainerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.modularUI = DefendCoreGUI.createModularUI(playerInventory.player);
        
        // 设置图像尺寸（LDLib2 会自己处理）
        this.imageWidth = 176;
        this.imageHeight = 166;
    }
    
    @Override
    protected void init() {
        super.init();
        // 初始化 LDLib2 UI
        modularUI.setScreenAndInit(this);
        this.addRenderableWidget(modularUI.getWidget());
    }
    
    @Override
    public void removed() {
        super.removed();
    }
    
    @Override
    protected void renderBg(net.minecraft.client.gui.GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // LDLib2 会自己渲染背景，这里不需要做什么
    }
}
