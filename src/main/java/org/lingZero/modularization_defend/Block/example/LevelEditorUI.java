package org.lingZero.modularization_defend.Block.example;

import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType.BlockUIHolder;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.utils.XmlUtils;
import net.minecraft.resources.ResourceLocation;

/**
 * 关卡编辑器 UI — 基于 LDLib2 XML 定义。
 * XML 文件位于 {@code assets/modularization_defend/ui/level_editor.xml}，
 * 运行时复制到 {@code run/ldlib2/} 目录下可启用热重载调试。
 */
public final class LevelEditorUI {

    private static final ResourceLocation XML_ID =
            ResourceLocation.parse("modularization_defend:ui/level_editor.xml");

    private LevelEditorUI() {}

    /** 从 XML 加载 UI 定义并创建 ModularUI */
    public static ModularUI createUI(BlockUIHolder holder) {
        var xml = XmlUtils.loadXml(XML_ID);
        if (xml != null) {
            var ui = UI.of(xml);

            // 保存按钮
            ui.select("#btn-save").forEach(el -> {
                if (el instanceof Button btn) {
                    btn.setOnServerClick(event -> {
                        if (holder.player.level().getBlockEntity(holder.pos) instanceof LevelEditorBlockEntity be) {
                            be.saveZone(holder.player);
                        }
                    });
                }
            });

            // 加载按钮
            ui.select("#btn-load").forEach(el -> {
                if (el instanceof Button btn) {
                    btn.setOnServerClick(event -> {
                        if (holder.player.level().getBlockEntity(holder.pos) instanceof LevelEditorBlockEntity be) {
                            be.loadZone(holder.player);
                        }
                    });
                }
            });

            // 清空按钮
            ui.select("#btn-clear").forEach(el -> {
                if (el instanceof Button btn) {
                    btn.setOnServerClick(event -> {
                        if (holder.player.level().getBlockEntity(holder.pos) instanceof LevelEditorBlockEntity be) {
                            be.clearZone(holder.player);
                        }
                    });
                }
            });

            return ModularUI.of(ui, holder.player);
        }
        return ModularUI.of(UI.empty(), holder.player);
    }
}
