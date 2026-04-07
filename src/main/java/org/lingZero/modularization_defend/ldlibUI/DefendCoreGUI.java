package org.lingZero.modularization_defend.ldlibUI;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.inventory.InventorySlots;
import net.minecraft.world.entity.player.Player;



/**
 * 用于创建和显示用户界面
 */
public class DefendCoreGUI {
    /**
     * 创建模块化UI界面
     * @param player 玩家实例，用于关联UI
     * @return 返回创建的ModularUI实例
     */
    public static ModularUI createModularUI(Player player) {
        // create a root element
        var root = new UIElement();
        root.addChildren(
                new InventorySlots()
                        .apply(slot -> slot.slotStyle(style -> style.showItemTooltips(true)))

        );
        root.addClass("panel_bg");

        var ui = UI.of(root);
        return ModularUI.of(ui);
    }
}
