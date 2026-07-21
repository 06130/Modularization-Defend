package org.lingZero.modularization_defend.nodegraph;

import com.lowdragmc.lowdraglib2.editor.resource.Resources;
import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.editor.ui.EditorWindow;
import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lingZero.modularization_defend.modularization_defend;

/**
 * 炮塔逻辑图资源编辑器——带资源面板的完整 LDLib2 Editor。
 *
 * <p>资源面板中的 turret_logic 标签页支持将节点图作为外部文件资产管理：
 * 新建、双击编辑、保存、重命名、复制、删除，以及拖拽到已打开图中作为外部子图。
 * 文件保存在 built-in global 文件夹或 mod 专属 graphs 文件夹（见
 * {@link TurretLogicGraphResource#buildBuiltin}）。</p>
 *
 * <p>通过 {@code /nodegraph editor} 打开。</p>
 */
public class TurretLogicEditor extends Editor {

    public static final ResourceLocation WINDOW_ID =
            ResourceLocation.fromNamespaceAndPath(modularization_defend.MODID, "graph_editor");

    public TurretLogicEditor() {
        // 移除默认布局中空置的左侧窗口（与 LDLib2 UIEditor 一致），让中央编辑区更明显
        this.leftWindow.setDisplay(false);
        this.leftWindow.getParentWindow().removeSplitWindow(this.leftWindow);
        initResources();
    }

    private void initResources() {
        this.resourceView.clear();
        this.resourceView.loadResources(Resources.of(TurretLogicGraphResource.INSTANCE));
    }

    @Override
    @NotNull
    protected Editor createNewEditorInstance() {
        return new TurretLogicEditor();
    }

    /** 注册编辑器窗口的 PlayerUI 菜单（模组初始化时调用一次） */
    public static void registerWindow() {
        PlayerUIMenuType.register(WINDOW_ID, ignored -> player -> {
            if (player.level().isClientSide) {
                return new ModularUI(UI.of(EditorWindow.open(WINDOW_ID, TurretLogicEditor::new)))
                        .shouldCloseOnEsc(false)
                        .shouldCloseOnKeyInventory(false);
            }
            return new ModularUI(UI.empty());
        });
    }
}
