package org.lingZero.modularization_defend.nodegraph;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.nodegraphtookit.editor.GraphResource;

/**
 * 炮塔逻辑图的编辑器资源——注册后可在 LDLib2 编辑器资源面板中管理图文件。
 *
 * <p>编辑器会自动处理图的打开、保存、脏状态跟踪等功能。</p>
 */
public class TurretLogicGraphResource extends GraphResource<TurretLogicGraph> {

    public static final TurretLogicGraphResource INSTANCE = new TurretLogicGraphResource();

    @Override
    public TurretLogicGraph createGraph() {
        return new TurretLogicGraph();
    }

    @Override
    public IGuiTexture getIcon() {
        return Icons.WIDGET_CUSTOM;
    }

    @Override
    public String getName() {
        return "turret_logic";
    }
}
