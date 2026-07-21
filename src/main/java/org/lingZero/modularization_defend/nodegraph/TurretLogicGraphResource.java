package org.lingZero.modularization_defend.nodegraph;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.editor.resource.FileResourceProvider;
import com.lowdragmc.lowdraglib2.editor.resource.IResourceProvider;
import com.lowdragmc.lowdraglib2.editor.resource.ResourceInstance;
import com.lowdragmc.lowdraglib2.editor.ui.resource.ResourceProviderContainer;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.nodegraphtookit.editor.GraphResource;
import net.minecraft.nbt.CompoundTag;

import java.io.File;

/**
 * 炮塔逻辑图的编辑器资源——注册后可在 LDLib2 编辑器资源面板中管理图文件。
 *
 * <p>文件以 {@code .turret_logic.nbt} 后缀保存。除 LDLib2 默认的 built-in 与
 * global 文件夹外，额外提供 mod 专属的 {@code modularization_defend/graphs}
 * 文件夹 provider 作为图资产的默认存放位置。</p>
 *
 * <p>编辑器会自动处理图的打开、保存、脏状态跟踪与外部子图解析。</p>
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

    @Override
    public void buildBuiltin(ResourceInstance<CompoundTag> resourceInstance) {
        // 保留默认的 built-in 与 global 文件夹 provider
        super.buildBuiltin(resourceInstance);
        // mod 专属图资产文件夹：<assets>/modularization_defend/graphs
        var graphsDir = new File(LDLib2.getAssetsDir(), "modularization_defend/graphs");
        var provider = new FileResourceProvider<>(resourceInstance, graphsDir);
        provider.setName("graphs");
        resourceInstance.addBuiltinProvider(provider);
    }

    @Override
    public ResourceProviderContainer<CompoundTag> createResourceProviderContainer(IResourceProvider<CompoundTag> provider) {
        return new FocusingGraphResourceProviderContainer(this, provider);
    }
}
