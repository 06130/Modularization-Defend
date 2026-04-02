# ApricityUI 框架 - 快速参考卡

## 🚀 常用 API

### UIManager
```java
UIManager ui = UIManager.getInstance();
ui.openUI("path");           // 打开 UI
ui.closeCurrentUI();         // 关闭当前 UI
ui.applyTheme("dark");       // 切换主题
ui.setDebugMode(true);       // 启用调试
```

### ThemeManager
```java
ThemeManager tm = ui.getThemeManager();
tm.applyTheme("tech");       // 应用主题
tm.getAvailableThemes();     // 获取所有主题
```

### 创建组件
```java
// 按钮
Button btn = new Button(ui, "id", "文本");
btn.setPrimaryStyle();
btn.addClickListener(e -> action());

// 面板
Panel panel = new Panel(ui, "id", "标题");
panel.addChild(component);

// 输入框
Input input = new Input(ui, "id");
input.setPlaceholder("提示...");
input.getValue();
```

### 布局模板
```java
// 垂直布局
VerticalLayout vl = new VerticalLayout(ui, spacing);
vl.createLayout(doc);
vl.addComponent(comp);

// 水平布局
HorizontalLayout hl = new HorizontalLayout(ui, spacing);

// 网格布局
GridLayout gl = new GridLayout(ui, cols, rows, gap);
gl.addComponent(comp, col, row);
```

### 动画工具
```java
AnimationUtil.fadeIn(el, 300);      // 淡入
AnimationUtil.fadeOut(el, 200);     // 淡出
AnimationUtil.scaleIn(el, 250);     // 缩放进入
AnimationUtil.rotate(el, 500, 360); // 旋转
```

---

## 📐 CSS 变量（主题）

```css
/* 背景色 */
--bg-color
--panel-bg
--panel-header-bg
--footer-bg

/* 文字颜色 */
--text-color
--heading-color
--label-color
--footer-text

/* 边框和输入 */
--border-color
--input-border
```

---

## 🎯 Screen 模板

```java
public class MyScreen extends BaseApricityScreen {
    public MyScreen(ApricityContainerMenu menu, Component title) {
        super(menu, title, "modid:path/index.html");
    }
    
    @Override
    protected void initializeUI() {
        loadHTML(uiPath);
        setUIPersistent(true);
        
        Document doc = getCurrentDocument();
        if (doc == null) return;
        
        // 创建组件...
    }
}
```

---

## 📝 HTML 模板

```html
<!DOCTYPE html>
<html>
<head>
    <title>标题</title>
    <link rel="stylesheet" href="modid:path/style.css"/>
</head>
<body>
    <div id="app-container">
        <header class="header"><h1>标题</h1></header>
        <main class="content"><!-- 内容 --></main>
        <footer class="footer"><!-- 页脚 --></footer>
    </div>
    <script src="modid:path/script.js"></script>
</body>
</html>
```

---

## 🔧 调试命令

- **END** - 热重载资源
- **F12** - 调试台（如果支持）
- **日志** - 查看 `[DebugReload]` 消息

---

## 💡 提示

1. ✅ 使用 `setUIPersistent(true)` 保持 UI
2. ✅ 组件用完调用 `destroy()` 清理
3. ✅ CSS 使用变量支持主题切换
4. ✅ 动画时长 200-500ms 最佳
5. ✅ 复杂逻辑放 Java，UI 逻辑放 JS

---

*打印此卡片以便快速查阅！*
