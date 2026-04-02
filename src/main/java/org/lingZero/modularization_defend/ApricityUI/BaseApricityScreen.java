package org.lingZero.modularization_defend.ApricityUI;

import com.sighs.apricityui.init.Document;
import com.sighs.apricityui.init.Element;
import com.sighs.apricityui.instance.ApricityContainerMenu;
import com.sighs.apricityui.instance.ApricityContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.lingZero.modularization_defend.util.DebugLogger;

/**
 * ApricityUI Screen 基类
 * 
 * 提供通用的 UI 操作方法，所有使用 ApricityUI 的 Screen 都应继承此类
 */
public abstract class BaseApricityScreen extends ApricityContainerScreen {
    
    protected Document document;
    protected final String uiPath;
    
    public BaseApricityScreen(String uiPath, ApricityContainerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.uiPath = uiPath;
    }
    
    @Override
    protected void init() {
        super.init();
        
        if (document == null) {
            // 创建 Document
            this.document = Document.create(uiPath);
            DebugLogger.info("Created UI document: {}", uiPath);
        }
        
        if (document != null) {
            // 调用子类的初始化方法
            initializeUI();
        }
    }
    
    /**
     * 初始化 UI，子类重写此方法
     */
    protected abstract void initializeUI();
    
    @Override
    public void removed() {
        super.removed();
        // 清理 Document
        if (document != null) {
            document.remove();
            DebugLogger.info("Removed UI document: {}", uiPath);
        }
    }
    
    /**
     * 通过 ID 查找元素
     * @param id 元素 ID
     * @return Element 实例
     */
    protected Element findElementById(String id) {
        if (document == null || id == null) return null;
        return document.getElementById(id);
    }
    
    /**
     * 设置元素文本
     * @param element 目标元素
     * @param text 文本内容
     */
    protected void setElementText(Element element, String text) {
        if (element != null) {
            element.innerText = text;
        }
    }
    
    /**
     * 给元素添加 CSS 类
     * @param element 目标元素
     * @param className CSS 类名
     */
    protected void addClass(Element element, String className) {
        if (element != null) {
            String existingClass = element.getAttribute("class");
            if (existingClass == null || existingClass.isBlank()) {
                element.setAttribute("class", className);
            } else {
                element.setAttribute("class", existingClass + " " + className);
            }
        }
    }
    
    /**
     * 移除元素的 CSS 类
     * @param element 目标元素
     * @param className CSS 类名
     */
    protected void removeClass(Element element, String className) {
        if (element != null) {
            String existingClass = element.getAttribute("class");
            if (existingClass != null && !existingClass.isBlank()) {
                String[] classes = existingClass.split("\\s+");
                StringBuilder newClasses = new StringBuilder();
                for (String cls : classes) {
                    if (!cls.equals(className)) {
                        if (newClasses.length() > 0) {
                            newClasses.append(" ");
                        }
                        newClasses.append(cls);
                    }
                }
                element.setAttribute("class", newClasses.toString().trim());
            }
        }
    }
    
    /**
     * 设置元素样式
     * @param element 目标元素
     * @param property 样式属性
     * @param value 样式值
     */
    protected void setStyle(Element element, String property, String value) {
        if (element != null) {
            String currentStyle = element.getAttribute("style");
            if (currentStyle == null) currentStyle = "";
            element.setAttribute("style", currentStyle + " " + property + ": " + value + ";");
        }
    }
    
    /**
     * 显示元素
     * @param element 目标元素
     */
    protected void show(Element element) {
        setStyle(element, "display", "block");
    }
    
    /**
     * 隐藏元素
     * @param element 目标元素
     */
    protected void hide(Element element) {
        setStyle(element, "display", "none");
    }
    
    /**
     * 切换元素可见性
     * @param element 目标元素
     * @return 新的可见状态
     */
    protected boolean toggleVisibility(Element element) {
        if (element != null) {
            String display = element.getComputedStyle().display;
            if ("none".equals(display)) {
                show(element);
                return true;
            } else {
                hide(element);
                return false;
            }
        }
        return false;
    }
    
    /**
     * 刷新 UI
     */
    protected void refreshUI() {
        if (document != null) {
            document.refresh();
            DebugLogger.info("Refreshed UI: {}", uiPath);
        }
    }
}
