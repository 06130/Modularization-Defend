package org.lingZero.modularization_defend.Block.example;

import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType.BlockUIHolder;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.utils.XmlUtils;
import net.minecraft.resources.ResourceLocation;

/**
 * 作战控制台 UI — 基于 LDLib2 XML 定义。
 * XML 文件位于 {@code assets/modularization_defend/ui/combat_console.xml}，
 * 运行时复制到 {@code run/ldlib2/} 目录下可启用热重载调试。
 */
public final class CombatConsoleUI {

    private static final ResourceLocation XML_ID =
            ResourceLocation.parse("modularization_defend:ui/combat_console.xml");

    private CombatConsoleUI() {}

    /** 从 XML 加载 UI 定义并创建 ModularUI */
    public static ModularUI createUI(BlockUIHolder holder) {
        var xml = XmlUtils.loadXml(XML_ID);
        if (xml != null) {
            return ModularUI.of(UI.of(xml), holder.player);
        }
        // 回退：空 UI
        return ModularUI.of(UI.empty(), holder.player);
    }
}
