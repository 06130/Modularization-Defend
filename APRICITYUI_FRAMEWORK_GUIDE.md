# ApricityUI 可扩展 GUI 框架使用指南

## 📚 目录

1. [框架概述](#框架概述)
2. [快速开始](#快速开始)
3. [核心组件](#核心组件)
4. [使用示例](#使用示例)
5. [最佳实践](#最佳实践)

---

## 框架概述

本框架基于 ApricityUI 构建，提供了一套完整、可扩展的 GUI 开发解决方案。

### 核心特性

- ✅ **模块化设计** - 清晰的分层架构，易于理解和维护
- ✅ **可复用组件** - 按钮、面板、输入框等常用组件
- ✅ **主题系统** - 支持亮色、暗色、科技等多种主题
- ✅ **布局模板** - 垂直、水平、网格等多种布局
- ✅ **动画效果** - 淡入淡出、缩放、滑动等丰富动画
- ✅ **热重载支持** - 开发过程中可实时预览效果

### 架构分层

```
┌─────────────────────────────────┐
│     Screen Layer (屏幕层)        │
│   - BaseApricityScreen          │
│   - ExampleScreen               │
└─────────────────────────────────┘
           ↓
┌─────────────────────────────────┐
│   Component Layer (组件层)       │
│   - Button, Panel, Input        │
│   - UIComponent (基类)          │
└─────────────────────────────────┘
           ↓
┌─────────────────────────────────┐
│    Layout Layer (布局层)         │
│   - VerticalLayout              │
│   - HorizontalLayout            │
│   - GridLayout                  │
└─────────────────────────────────┘
           ↓
┌─────────────────────────────────┐
│     Core Layer (核心层)          │
│   - UIManager                   │
│   - ThemeManager                │
└─────────────────────────────────┘
```

---

## 快速开始

### 1. 创建自定义 Screen

继承 `BaseApricityScreen` 并重写 `initializeUI()` 方法：

```java
public class MyScreen extends BaseApricityScreen {
    
    public MyScreen(ApricityContainerMenu menu, Component title) {
        super(menu, title, "modularization_defend:my_ui/index.html");
    }
    
    @Override
    protected void initializeUI() {
        loadHTML(uiPath);
        
        // 在这里添加你的 UI 逻辑
        Document document = getCurrentDocument();
        if (document == null) return;
        
        // 创建组件...
    }
}
```

### 2. 注册 MenuType

在你的主类或注册类中注册菜单类型：

```java
public static final DeferredHolder<MenuType<?>, MenuType<ApricityContainerMenu>> MY_MENU =
    ModMenuTypes.MENUS.register("my_menu", () -> IMenuTypeExtension.create(ApricityContainerMenu::new));
```

### 3. 注册 Screen

在客户端事件处理器中注册：

```java
@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(MY_MENU.get(), MyScreen::new);
    }
}
```

### 4. 打开界面

```java
// 在方块实体或其他地方
Menus.setBlockEntityMenu(this, MY_MENU.get());
player.openMenu(this);
```

---

## 核心组件

### UIManager - UI 管理器

管理所有 UI 实例的核心类：

```java
UIManager uiManager = UIManager.getInstance();

// 打开 UI
Document doc = uiManager.openUI("modularization_defend:my_ui.html");

// 切换主题
uiManager.applyTheme("dark");

// 刷新 UI
uiManager.refreshAllUI();

// 启用调试模式
uiManager.setDebugMode(true);
```

### ThemeManager - 主题管理器

管理 UI 主题的加载和切换：

```java
ThemeManager themeManager = uiManager.getThemeManager();

// 应用主题
themeManager.applyTheme("tech");

// 获取可用主题
String[] themes = themeManager.getAvailableThemes();

// 注册自定义主题
Theme customTheme = new Theme("custom");
customTheme.setColor("background", "#ff0000");
themeManager.registerTheme(customTheme);
```

### UIComponent - 组件基类

所有可复用组件的基类，提供通用功能：

```java
// 设置可见性
component.setVisible(true);

// 设置启用状态
component.setEnabled(false);

// 添加 CSS 类
component.addClass("my-class");

// 设置样式
component.setStyle("color", "#ff0000");
```

### Button - 按钮组件

```java
Button button = new Button(uiManager, "btnId", "点击我");
button.setPrimaryStyle();  // 主要样式
button.setSize("large");   // 大尺寸

// 添加点击事件
button.addClickListener(event -> {
    System.out.println("按钮被点击了！");
});

// 添加到布局
layout.addComponent(button);
```

### Panel - 面板组件

```java
Panel panel = new Panel(uiManager, "panelId", "我的面板");
panel.setCollapsible();  // 可折叠

// 添加子组件
panel.addChild(component1);
panel.addChild(component2);

// 清空所有子组件
panel.clearChildren();
```

### Input - 输入框组件

```java
Input input = new Input(uiManager, "inputId");
input.setPlaceholder("请输入...");
input.setType("text");

// 添加值变化监听
input.addInputListener(event -> {
    String value = input.getValue();
    System.out.println("输入：" + value);
});

// 添加改变事件监听（失去焦点）
input.addChangeListener(event -> {
    System.out.println("值已改变");
});
```

### Layout Templates - 布局模板

**垂直布局：**
```java
VerticalLayout layout = new VerticalLayout(uiManager, 10);
layout.createLayout(document);
layout.addComponent(component1);
layout.addComponent(component2);
```

**水平布局：**
```java
HorizontalLayout layout = new HorizontalLayout(uiManager, 15);
layout.createLayout(document);
layout.addComponent(component1);
layout.addComponent(component2);
```

**网格布局：**
```java
GridLayout layout = new GridLayout(uiManager, 3, 2, 10);
layout.createLayout(document);
layout.addComponent(component1, 1, 1);  // 第 1 列，第 1 行
layout.addComponent(component2, 2, 1);  // 第 2 列，第 1 行
```

### AnimationUtil - 动画工具

```java
// 淡入
AnimationUtil.fadeIn(element, 300);

// 淡出
AnimationUtil.fadeOut(element, 200);

// 缩放进入
AnimationUtil.scaleIn(element, 250);

// 滑动进入
AnimationUtil.slideInLeft(element, 300, 50);

// 旋转
AnimationUtil.rotate(element, 500, 360);
```

---

## 使用示例

### 完整示例：创建配置界面

```java
public class ConfigScreen extends BaseApricityScreen {
    
    private Input nameInput;
    private Input valueInput;
    private Button saveButton;
    private Button cancelButton;
    private Panel mainPanel;
    
    public ConfigScreen(ApricityContainerMenu menu, Component title) {
        super(menu, title, "modularization_defend:config/index.html");
    }
    
    @Override
    protected void initializeUI() {
        loadHTML(uiPath);
        setUIPersistent(true);
        
        Document document = getCurrentDocument();
        if (document == null) return;
        
        // 创建主面板
        mainPanel = new Panel(uiManager, "mainPanel", "配置面板");
        
        // 创建垂直布局
        VerticalLayout layout = new VerticalLayout(uiManager, 15);
        layout.createLayout(document);
        
        // 名称输入框
        nameInput = new Input(uiManager, "nameInput");
        nameInput.setPlaceholder("配置名称...");
        layout.addComponent(nameInput);
        
        // 数值输入框
        valueInput = new Input(uiManager, "valueInput");
        valueInput.setPlaceholder("配置值...");
        valueInput.setType("number");
        layout.addComponent(valueInput);
        
        // 保存按钮
        saveButton = new Button(uiManager, "saveButton", "保存");
        saveButton.setPrimaryStyle();
        saveButton.addClickListener(event -> onSaveClick());
        layout.addComponent(saveButton);
        
        // 取消按钮
        cancelButton = new Button(uiManager, "cancelButton", "取消");
        cancelButton.setSecondaryStyle();
        cancelButton.addClickListener(event -> onClose());
        layout.addComponent(cancelButton);
        
        // 组装 UI
        mainPanel.addChild(layout.getRootElement());
        
        Element body = document.body;
        if (body != null) {
            Element panelElement = mainPanel.createElement(document);
            body.appendChild(panelElement);
            mainPanel.initialize();
        }
        
        // 应用动画
        AnimationUtil.fadeIn(mainPanel.getElement(), 300);
    }
    
    private void onSaveClick() {
        String name = nameInput.getValue();
        String value = valueInput.getValue();
        
        // 保存配置逻辑
        System.out.println("保存配置：" + name + " = " + value);
        
        // 播放反馈动画
        AnimationUtil.bounce(saveButton.getElement(), 200);
    }
}
```

### HTML 资源示例

```html
<!DOCTYPE html>
<html>
<head>
    <title>配置界面</title>
    <link rel="stylesheet" href="modularization_defend:config/style.css"/>
</head>
<body>
    <div id="app-container">
        <header class="header">
            <h1>配置管理</h1>
        </header>
        
        <main class="content">
            <!-- 内容将由 Java 代码动态生成 -->
        </main>
        
        <footer class="footer">
            <p>Config Screen © 2026</p>
        </footer>
    </div>
    
    <script src="modularization_defend:config/script.js"></script>
</body>
</html>
```

---

## 最佳实践

### 1. 资源组织

建议的资源目录结构：
```
assets/modid/apricity/
├── common/           # 通用样式和脚本
│   ├── style.css
│   └── script.js
├── config/           # 配置界面
│   ├── index.html
│   ├── style.css
│   └── script.js
└── machine/          # 机器界面
    ├── index.html
    ├── style.css
    └── script.js
```

### 2. 性能优化

- 使用 `setUIPersistent(true)` 避免重复加载
- 合理控制动画时长（建议 200-500ms）
- 及时清理不用的组件（调用 `destroy()`）

### 3. 代码规范

- 组件 ID 使用有意义的命名（如 `saveButton`, `nameInput`）
- CSS 类名使用连字符分隔（如 `.btn-primary`）
- JavaScript 代码保持简洁，复杂逻辑放在 Java 端

### 4. 主题适配

使用 CSS 变量以支持主题切换：
```css
.panel {
    background: var(--panel-bg, #fff);
    color: var(--text-color, #333);
}
```

### 5. 调试技巧

启用调试模式查看详细日志：
```java
UIManager.getInstance().setDebugMode(true);
```

按 END 键热重载资源（开发环境）。

---

## 常见问题

**Q: 如何发送网络包？**
A: 在 JavaScript 中使用 `ApricityUI.sendPacket()` 或在 Java 端通过 Screen 的网络通道发送。

**Q: 如何实现多页面导航？**
A: 使用 `UIManager.openUI()` 打开不同页面，配合 `UIManager.back()` 实现返回。

**Q: 动画卡顿怎么办？**
A: 减少同时进行的动画数量，使用 CSS transition 代替复杂的 JS 动画。

---

## 总结

本框架提供了完整的 ApricityUI 开发解决方案，从基础的 UI 管理到高级的组件系统，从简单的布局到复杂的动画效果。遵循本指南的最佳实践，你可以快速开发出美观、高效的游戏界面。

祝你开发愉快！🎉
