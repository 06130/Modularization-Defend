# ApricityUI 可扩展 GUI 框架 - 项目总结

## 📦 项目概述

这是一个基于 **ApricityUI** 构建的完整、可扩展的 GUI 开发框架，专为 Minecraft NeoForge 1.21.1 模组设计。框架采用模块化架构，提供了从基础管理到高级组件的全套解决方案。

---

## 🎯 核心目标

- ✅ **低门槛** - 使用 HTML+CSS+JS 三剑客，AI 辅助开发友好
- ✅ **高扩展** - 清晰的分层架构，易于添加新功能
- ✅ **可复用** - 丰富的组件库和布局模板
- ✅ **美观性** - 内置多种主题和动画效果
- ✅ **热重载** - 开发过程中实时预览效果

---

## 📁 文件结构

```
src/main/java/org/lingZero/modularization_defend/ApricityUI/
├── Core/
│   └── UIManager.java              # UI 核心管理器
├── Screen/
│   ├── BaseApricityScreen.java     # Screen 基类
│   └── Example/
│       └── ExampleScreen.java      # 示例 Screen
├── Component/
│   ├── UIComponent.java            # 组件基类
│   ├── Button.java                 # 按钮组件
│   ├── Panel.java                  # 面板组件
│   └── Input.java                  # 输入框组件
├── Layout/
│   ├── LayoutTemplate.java         # 布局模板基类
│   ├── VerticalLayout.java         # 垂直布局
│   ├── HorizontalLayout.java       # 水平布局
│   └── GridLayout.java             # 网格布局
├── Theme/
│   └── ThemeManager.java           # 主题管理器
├── Animation/
│   └── AnimationUtil.java          # 动画工具类
└── DefendCoreUI.java               # 主入口类（待完善）

src/main/resources/assets/modularization_defend/apricity/example/
├── index.html                      # 示例 HTML
├── style.css                       # 示例样式
└── script.js                       # 示例脚本
```

---

## 🏗️ 架构设计

### 四层架构

```
┌─────────────────────────────────────────┐
│         Screen Layer (屏幕层)            │
│  • BaseApricityScreen - Screen 基类      │
│  • ExampleScreen - 示例实现              │
│  作用：提供 Minecraft Screen 集成         │
└─────────────────────────────────────────┘
              ↓ 继承/使用
┌─────────────────────────────────────────┐
│       Component Layer (组件层)           │
│  • UIComponent - 组件基类                │
│  • Button, Panel, Input - 具体组件       │
│  作用：提供可复用的 UI 元素                │
└─────────────────────────────────────────┘
              ↓ 使用
┌─────────────────────────────────────────┐
│        Layout Layer (布局层)             │
│  • LayoutTemplate - 布局基类             │
│  • VerticalLayout, HorizontalLayout     │
│  • GridLayout                           │
│  作用：提供灵活的布局方案                 │
└─────────────────────────────────────────┘
              ↓ 使用
┌─────────────────────────────────────────┐
│         Core Layer (核心层)              │
│  • UIManager - UI 管理器                 │
│  • ThemeManager - 主题管理器             │
│  作用：核心管理和资源调度                 │
└─────────────────────────────────────────┘
```

### 辅助系统

- **Animation** - 动画工具类（淡入淡出、缩放、滑动等）
- **Theme** - 主题系统（亮色、暗色、科技等）

---

## 🚀 快速开始

### 1. 创建自定义 Screen

```java
public class MyScreen extends BaseApricityScreen {
    public MyScreen(ApricityContainerMenu menu, Component title) {
        super(menu, title, "modularization_defend:my_ui/index.html");
    }
    
    @Override
    protected void initializeUI() {
        loadHTML(uiPath);
        
        // 使用组件创建 UI
        Button btn = new Button(uiManager, "btn1", "点击我");
        // ... 更多逻辑
    }
}
```

### 2. 注册并打开界面

```java
// 注册 MenuType
public static final DeferredHolder<MenuType<?>, MenuType<ApricityContainerMenu>> MY_MENU =
    ModMenuTypes.MENUS.register("my_menu", () -> IMenuTypeExtension.create(ApricityContainerMenu::new));

// 注册 Screen（客户端）
@SubscribeEvent
public static void registerMenuScreens(RegisterMenuScreensEvent event) {
    event.register(MY_MENU.get(), MyScreen::new);
}

// 打开界面
player.openMenu(this);
```

---

## ✨ 核心功能

### 1. UI 管理器 (UIManager)

```java
UIManager uiManager = UIManager.getInstance();

// 打开/关闭 UI
Document doc = uiManager.openUI("modularization_defend:ui.html");
uiManager.closeCurrentUI();

// 主题切换
uiManager.applyTheme("dark");

// 调试模式
uiManager.setDebugMode(true);
```

### 2. 主题系统 (ThemeManager)

内置三种主题：
- **light** - 明亮简洁
- **dark** - 护眼暗色
- **tech** - 科技感十足

```java
ThemeManager themeManager = uiManager.getThemeManager();
themeManager.applyTheme("tech");
```

支持自定义主题：
```java
Theme custom = new Theme("custom");
custom.setColor("background", "#ff0000");
themeManager.registerTheme(custom);
```

### 3. 可复用组件

**按钮组件：**
```java
Button btn = new Button(uiManager, "saveBtn", "保存");
btn.setPrimaryStyle();
btn.addClickListener(e -> onSave());
```

**面板组件：**
```java
Panel panel = new Panel(uiManager, "mainPanel", "主面板");
panel.setCollapsible();
panel.addChild(component);
```

**输入框组件：**
```java
Input input = new Input(uiManager, "nameInput");
input.setPlaceholder("请输入...");
input.addInputListener(e -> onValueChange(input.getValue()));
```

### 4. 布局模板

**垂直布局：**
```java
VerticalLayout layout = new VerticalLayout(uiManager, 10);
layout.addComponent(btn1);
layout.addComponent(btn2);
```

**网格布局：**
```java
GridLayout layout = new GridLayout(uiManager, 3, 3, 15);
layout.addComponent(comp, 2, 2);  // 第 2 列第 2 行
```

### 5. 动画效果

```java
// 淡入淡出
AnimationUtil.fadeIn(element, 300);
AnimationUtil.fadeOut(element, 200);

// 缩放旋转
AnimationUtil.scaleIn(element, 250);
AnimationUtil.rotate(element, 500, 360);

// 滑动效果
AnimationUtil.slideInLeft(element, 300, 50);
```

---

## 🎨 资源文件

### HTML 结构

```html
<!DOCTYPE html>
<html>
<head>
    <title>我的界面</title>
    <link rel="stylesheet" href="modularization_defend:my_ui/style.css"/>
</head>
<body>
    <div id="app-container">
        <header class="header">
            <h1>标题</h1>
        </header>
        <main class="content">
            <!-- 内容区域 -->
        </main>
    </div>
    <script src="modularization_defend:my_ui/script.js"></script>
</body>
</html>
```

### CSS 样式（支持主题变量）

```css
.panel {
    background: var(--panel-bg, #fff);
    color: var(--text-color, #333);
    transition: all 0.3s ease;
}
```

### JavaScript 交互

```javascript
document.addEventListener('load', function() {
    const btn = document.getElementById('myBtn');
    btn.addEventListener('mousedown', function(event) {
        console.log('按钮被点击！');
    });
});
```

---

## 📊 特性对比

| 特性 | 原版 GUI | ApricityUI 框架 |
|------|---------|----------------|
| 开发语言 | Java | HTML+CSS+JS |
| 上手难度 | 中等 | 简单 |
| AI 辅助 | 一般 | ⭐⭐⭐⭐⭐ |
| 热重载 | ❌ | ✅ |
| 主题系统 | 需手写 | ✅ 内置 |
| 动画效果 | 复杂 | ✅ 简单 API |
| 布局灵活度 | 低 | ⭐⭐⭐⭐⭐ |
| 调试效率 | 慢 | ⭐⭐⭐⭐⭐ |

---

## 💡 最佳实践

### 1. 资源组织

```
assets/modid/apricity/
├── common/          # 共享资源
├── config/          # 配置界面
├── machine/         # 机器界面
└── gui/             # 其他 GUI
```

### 2. 性能优化

- 使用 `setUIPersistent(true)` 避免重复加载
- 动画时长控制在 200-500ms
- 及时调用 `component.destroy()` 清理组件

### 3. 代码规范

- 组件 ID 使用有意义的命名
- CSS 类名使用连字符（`.btn-primary`）
- 复杂逻辑放在 Java 端，UI 逻辑放在 JS

### 4. 主题适配

始终使用 CSS 变量：
```css
/* ✅ 推荐 */
color: var(--text-color, #333);

/* ❌ 不推荐 */
color: #333;
```

---

## 🔧 调试技巧

1. **启用调试模式**
   ```java
   UIManager.getInstance().setDebugMode(true);
   ```

2. **热重载资源**
   - 开发环境下按 END 键
   - 查看日志中的 `[DebugReload]` 消息

3. **查看截图**
   - 调试模式下每秒自动截图
   - 路径：`run/screenshots/aui/`

---

## 📝 示例代码

完整的示例请参见：
- `ExampleScreen.java` - Java 端示例
- `example/index.html` - HTML 示例
- `example/style.css` - CSS 示例
- `example/script.js` - JavaScript 示例

---

## 🎓 学习路线

1. **入门** - 阅读 `APRICITYUI_FRAMEWORK_GUIDE.md`
2. **实践** - 修改 `ExampleScreen` 进行实验
3. **进阶** - 创建自定义组件和布局
4. **精通** - 深入理解 ApricityUI 源码

---

## 🤝 贡献与反馈

欢迎提出问题和改进建议！

---

## 📄 许可证

本框架遵循与主项目相同的许可证。

---

## 🎉 结语

ApricityUI 可扩展 GUI 框架旨在让 Minecraft 模组界面开发变得简单、高效、有趣。无论是新手还是老手，都能快速上手并创造出令人惊艳的界面。

**Happy Coding! 🚀**

---

*最后更新：2026-03-28*
