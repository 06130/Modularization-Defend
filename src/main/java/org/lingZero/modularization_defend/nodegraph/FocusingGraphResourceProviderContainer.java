package org.lingZero.modularization_defend.nodegraph;

import com.lowdragmc.lowdraglib2.editor.resource.IResourcePath;
import com.lowdragmc.lowdraglib2.editor.resource.IResourceProvider;
import com.lowdragmc.lowdraglib2.nodegraphtookit.editor.GraphEditorView;
import com.lowdragmc.lowdraglib2.nodegraphtookit.editor.GraphResource;
import com.lowdragmc.lowdraglib2.nodegraphtookit.editor.GraphResourceProviderContainer;
import net.minecraft.nbt.CompoundTag;

/**
 * 带自动聚焦的图资源容器——包装 LDLib2 默认的编辑打开逻辑，解决两个交互问题：
 *
 * <ol>
 *   <li>双击已打开的资源时，父类会静默忽略；这里改为切换到它对应的标签页</li>
 *   <li>新打开的编辑视图标签页不会自动选中；这里打开后立即聚焦</li>
 * </ol>
 */
public class FocusingGraphResourceProviderContainer extends GraphResourceProviderContainer<TurretLogicGraph> {

    public FocusingGraphResourceProviderContainer(GraphResource<TurretLogicGraph> graphResource,
                                                  IResourceProvider<CompoundTag> provider) {
        super(graphResource, provider);
        var delegate = this.onEdit;
        setOnEdit((container, path) -> {
            // 已打开 → 直接聚焦其标签页
            if (focusOpenView(path)) return;
            if (delegate != null) delegate.accept(container, path);
            // 新打开 → 聚焦（父类 placeView 添加标签页后不会自动选中）
            focusOpenView(path);
        });
    }

    /** 在中央窗口查找根路径匹配的已打开编辑视图并选中其标签页，找到返回 true */
    private boolean focusOpenView(IResourcePath path) {
        var editor = getEditor();
        if (editor == null || editor.centerWindow == null) return false;
        var container = editor.centerWindow.getLeftTop();
        if (container == null) return false;
        // addView 将 View 本身作为 tab content 存入 tabContents
        for (var content : container.tabView.getTabContents().values()) {
            if (content instanceof GraphEditorView graphView && path.equals(graphView.getRootPath())) {
                container.selectView(graphView);
                return true;
            }
        }
        return false;
    }
}
