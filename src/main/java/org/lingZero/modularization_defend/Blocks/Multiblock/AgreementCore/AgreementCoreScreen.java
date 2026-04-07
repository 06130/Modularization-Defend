package org.lingZero.modularization_defend.Blocks.Multiblock.AgreementCore;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 协议核心 GUI 屏幕
 * 使用新的 400x230 尺寸设计
 */
public class AgreementCoreScreen extends AbstractContainerScreen<AgreementCoreMenu> {
    
    // GUI 纹理（使用专用的协议核心纹理）
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath("modularization_defend", "textures/gui/agreement_core_screen.png");
    
    // GUI 尺寸（与示例项目一致）
    private static final int GUI_WIDTH = 400;
    private static final int GUI_HEIGHT = 230;
    
    // 纹理图片实际尺寸（用于 UV 映射）
    private static final int TEXTURE_WIDTH = GUI_WIDTH;
    private static final int TEXTURE_HEIGHT = GUI_HEIGHT;
    
    // 信息面板位置（右侧）
    private static final int INFO_PANEL_X = 240;
    private static final int INFO_PANEL_Y = 11;
    
    // 颜色
    private static final int TEXT_GREEN = 0x00AA00;           // 绿色文字
    private static final int TEXT_DARK_GRAY = 0x404040;       // 深灰色文字
    
    // 信息面板缩放比例 (0.7 = 70%)
    private static final float INFO_TEXT_SCALE = 0.7f;
    
    public AgreementCoreScreen(AgreementCoreMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        // 调整物品栏标签位置（适应新 GUI 尺寸）
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
    }
    
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 渲染标题
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, TEXT_DARK_GRAY, false);

        // 渲染信息面板文本
        renderInfoText(guiGraphics);
    }
    
    @Override
    public boolean keyPressed(int key, int b, int c) {
        if (key == 256) {
            this.minecraft.player.closeContainer();
            return true;
        }
        return super.keyPressed(key, b, c);
    }
    
    /**
     * 渲染信息面板文本（支持缩放）
     */
    private void renderInfoText(GuiGraphics guiGraphics) {
        PoseStack poseStack = guiGraphics.pose();
        
        // 保存当前的变换矩阵
        poseStack.pushPose();
        
        // 应用缩放
        poseStack.scale(INFO_TEXT_SCALE, INFO_TEXT_SCALE, 1.0f);
        
        // 计算缩放后的偏移量，确保文本从正确位置开始渲染
        int scaledX = (int) (INFO_PANEL_X / INFO_TEXT_SCALE);
        int scaledY = (int) (INFO_PANEL_Y / INFO_TEXT_SCALE);
        int lineHeight = (int) (9 / INFO_TEXT_SCALE); // 行距也按比例缩放
        
        // 显示多方块状态
        String statusText = menu.getData().get(2) == 1 ? "§a已连接" : "§c协议源石离线";
        guiGraphics.drawString(font, "§e协议网络：" + statusText, scaledX, scaledY, TEXT_GREEN, false);
        
        // 显示能量信息
        int energy = menu.getData().get(0);
        int maxEnergy = menu.getData().get(1);
        guiGraphics.drawString(font, "§b上线设备：" + energy + " / " + maxEnergy, scaledX, scaledY + lineHeight, TEXT_GREEN, false);
        
        // 显示负载功率（预留）
        guiGraphics.drawString(font, "§6负载功率：0 W", scaledX, scaledY + lineHeight * 2, TEXT_GREEN, false);
        
        // 显示上网功率（预留）
        guiGraphics.drawString(font, "§a上网功率：0 W", scaledX, scaledY + lineHeight * 3, TEXT_GREEN, false);

        // 显示能源矩阵容量（预留）
        guiGraphics.drawString(font, "§e能源矩阵容量：0 Wh", scaledX, scaledY + lineHeight * 4, TEXT_GREEN, false);
        // 恢复变换矩阵
        poseStack.popPose();
    }
}
