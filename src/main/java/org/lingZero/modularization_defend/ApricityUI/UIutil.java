package org.lingZero.modularization_defend.ApricityUI;

import com.sighs.apricityui.init.Document;
import net.minecraft.resources.ResourceLocation;
import org.lingZero.modularization_defend.ModularizationDefend;
import org.lingZero.modularization_defend.util.DebugLogger;

/**
 * ApricityUI 扩展核心工具类
 * 
 * 提供模组特定的 UI 工具方法和快捷访问
 * 
 * 注意：
 * - Screen 应继承自 BaseApricityScreen 以获得通用方法
 * - 本类仅提供静态工具方法，不管理 UI 状态
 */
public class UIutil {
    
    /**
     * 获取资源位置
     * @param path 资源路径（相对于 assets/modularization_defend/apricity/）
     * @return ResourceLocation
     */
    public static ResourceLocation getResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModularizationDefend.MODID, "apricity/" + path);
    }
    
    /**
     * 打开 UI 界面（独立使用，不关联 Screen）
     * @param path HTML 文件路径
     * @return Document 实例
     */
    public static Document openUI(String path) {
        DebugLogger.info("Opening UI: {}", path);
        return Document.create(path);
    }
    
    /**
     * 在世界中渲染 UI
     * @param path HTML 文件路径
     * @return Document 实例
     */
    public static Document openUIInWorld(String path) {
        DebugLogger.info("Opening UI in world: {}", path);
        return Document.createInWorld(path);
    }
    
    /**
     * 刷新所有 UI
     */
    public static void refreshAllUI() {
        DebugLogger.info("Refreshing all UI");
        Document.refreshAll();
    }
}
