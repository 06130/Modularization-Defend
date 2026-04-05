package org.lingZero.modularization_defend.ldlibUI;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.inventory.InventorySlots;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;


/**
 * 用于创建和显示用户界面
 */
public class DefendCoreGUI {
    /**
     * 创建模块化UI界面（由ModularUIContainerMenu调用）
     * @param player 玩家实例，用于关联UI
     * @return 返回创建的ModularUI实例
     */
    public static ModularUI createModularUI(Player player) {

        // 创建根元素
        var root = new UIElement();

        //标题
        var label = new Label().setText(Component.translatable("item.modularization_defend.defend_core"));
        root.addChild(label);

        //按钮
        var button = new Button().setText("Click Me!");
        root.addChild(button);

        // 物品栏
        var inv = new InventorySlots();
        root.addChild(inv);
        
        // 创建UI
        var ui = UI.of(root, StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.MC));

        return ModularUI.of(ui, player);
    }
}
