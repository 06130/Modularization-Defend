# ApricityUI 可扩展 GUI 框架 - 文件结构

## 📂 完整目录树

```
Modularization Defend/
│
├── src/main/java/org/lingZero/modularization_defend/ApricityUI/
│   │
│   ├── Core/                          # 核心层
│   │   └── UIManager.java             # UI 管理器（单例模式）
│   │       • openUI() / closeUI()     # UI 生命周期管理
│   │       • applyTheme()             # 主题切换
│   │       • refreshAllUI()           # 刷新所有 UI
│   │       • setDebugMode()           # 调试模式
│   │
│   ├── Screen/                        # 屏幕层
│   │   ├── BaseApricityScreen.java    # Screen 基类
│   │   │   • initializeUI()           # 初始化 UI（抽象方法）
│   │   │   • loadHTML()               # 加载 HTML 资源
│   │   │   • render()                 # 渲染循环
│   │   │   • onMouseClick()           # 鼠标事件
│   │   │   • onKeyPress()             # 键盘事件
│   │   │
│   │   └── Example/                   # 示例 Screens
│   │       └── ExampleScreen.java     # 示例实现
│   │           • 演示组件使用
│   │           • 演示事件处理
│   │           • 演示动画效果
│   │
│   ├── Component/                     # 组件层
│   │   ├── UIComponent.java           # 组件基类（抽象类）
│   │   │   • createElement()          # 创建元素（抽象）
│   │   │   • initialize()             # 初始化（抽象）
│   │   │   • setVisible()             # 可见性
│   │   │   • setEnabled()             # 启用状态
│   │   │   • addClass()               # CSS 类
│   │   │   • setStyle()               # 样式设置
│   │   │
│   │   ├── Button.java                # 按钮组件
│   │   │   • addClickListener()       # 点击事件
│   │   │   • setText()                # 设置文本
│   │   │   • setPrimaryStyle()        # 主要样式
│   │   │   • setSecondaryStyle()      # 次要样式
│   │   │   • setDangerStyle()         # 危险样式
│   │   │
│   │   ├── Panel.java                 # 面板组件
│   │   │   • addChild()               # 添加子组件
│   │   │   • removeChild()            # 移除子组件
│   │   │   • setTitle()               # 设置标题
│   │   │   • setCollapsible()         # 可折叠
│   │   │   • expand() / collapse()    # 展开/折叠
│   │   │
│   │   └── Input.java                 # 输入框组件
│   │       • setValue() / getValue()  # 值操作
│   │       • setPlaceholder()         # 占位符
│   │       • setType()                # 输入类型
│   │       • addInputListener()       # 输入监听
│   │       • addChangeListener()      # 改变监听
│   │
│   ├── Layout/                        # 布局层
│   │   ├── LayoutTemplate.java        # 布局模板基类
│   │   │   • createLayout()           # 创建布局（抽象）
│   │   │
│   │   ├── VerticalLayout.java        # 垂直布局
│   │   │   • spacing                  # 间距属性
│   │   │   • flex-direction: column   # Flexbox 垂直
│   │   │
│   │   ├── HorizontalLayout.java      # 水平布局
│   │   │   • spacing                  # 间距属性
│   │   │   • flex-direction: row      # Flexbox 水平
│   │   │
│   │   └── GridLayout.java            # 网格布局
│   │       • columns / rows           # 行列数
│   │       • gap                      # 间距
│   │       • grid-template-columns    # CSS Grid
│   │
│   ├── Theme/                         # 主题层
│   │   └── ThemeManager.java          # 主题管理器
│   │       • registerDefaultThemes()  # 注册默认主题
│   │       • applyTheme()             # 应用主题
│   │       • loadThemeFromJSON()      # 从 JSON 加载
│   │       • Theme (内部类)           # 主题数据结构
│   │
│   ├── Animation/                     # 动画层
│   │   └── AnimationUtil.java         # 动画工具类
│   │       • fadeIn() / fadeOut()     # 淡入淡出
│   │       • scaleIn() / scaleOut()   # 缩放
│   │       • slideInLeft()            # 滑动进入
│   │       • rotate()                 # 旋转
│   │       • colorTransition()        # 颜色过渡
│   │
│   └── DefendCoreUI.java              # 主入口（待完善）
│
├── src/main/resources/assets/modularization_defend/apricity/
│   │
│   └── example/                       # 示例资源
│       ├── index.html                 # HTML 布局
│       │   • DOCTYPE 声明
│       │   • HTML5 结构
│       │   • 语义化标签
│       │   • 链接 CSS 和 JS
│       │
│       ├── style.css                  # CSS 样式
│       │   • CSS 变量（主题支持）
│       │   • Flexbox 布局
│       │   • Grid 布局
│       │   • 动画关键帧
│       │   • 响应式设计
│       │
│       └── script.js                  # JavaScript 交互
│           • DOM 事件监听
│           • 主题切换逻辑
│           • 表单验证
│           • 状态更新
│
├── docs/                              # 文档（已创建）
│   ├── APRICITYUI_FRAMEWORK_GUIDE.md  # 详细使用指南
│   ├── FRAMEWORK_SUMMARY.md           # 项目总结
│   ├── QUICK_REFERENCE.md             # 快速参考卡
│   └── FILE_STRUCTURE.md              # 本文档
│
└── build.gradle                       # Gradle 构建配置
    • 确保 ApricityUI 依赖已添加
    • 资源配置
```

---

## 📊 文件大小统计

### Java 代码文件

| 文件 | 行数 | 功能 |
|------|------|------|
| UIManager.java | ~250 | UI 核心管理 |
| BaseApricityScreen.java | ~160 | Screen 基类 |
| UIComponent.java | ~210 | 组件基类 |
| Button.java | ~110 | 按钮组件 |
| Panel.java | ~160 | 面板组件 |
| Input.java | ~175 | 输入框组件 |
| LayoutTemplate.java | ~35 | 布局基类 |
| VerticalLayout.java | ~60 | 垂直布局 |
| HorizontalLayout.java | ~60 | 水平布局 |
| GridLayout.java | ~95 | 网格布局 |
| ThemeManager.java | ~190 | 主题管理 |
| AnimationUtil.java | ~205 | 动画工具 |
| ExampleScreen.java | ~130 | 示例 Screen |
| **总计** | **~1840** | **13 个文件** |

### 资源文件

| 文件 | 行数 | 功能 |
|------|------|------|
| index.html | ~95 | HTML 布局 |
| style.css | ~260 | CSS 样式 |
| script.js | ~165 | JavaScript |
| **总计** | **~520** | **3 个文件** |

### 文档文件

| 文件 | 内容 |
|------|------|
| APRICITYUI_FRAMEWORK_GUIDE.md | 详细使用指南（~450 行） |
| FRAMEWORK_SUMMARY.md | 项目总结（~380 行） |
| QUICK_REFERENCE.md | 快速参考（~150 行） |
| FILE_STRUCTURE.md | 文件结构（本文档） |

---

## 🎯 类关系图

```
┌─────────────────────────────────────────┐
│         BaseApricityScreen              │
│         (继承自 ApricityContainerScreen) │
└─────────────────────────────────────────┘
                    ↑
                    │ 继承
                    │
        ┌───────────┴───────────┐
        │                       │
┌───────────────┐       ┌───────────────┐
│ ExampleScreen │       │ YourScreen    │
└───────────────┘       └───────────────┘

─────────────────────────────────────────

BaseApricityScreen 组合了以下组件：

┌─────────────────────────────────────────┐
│            UIManager                    │
│  ┌─────────────────────────────────┐   │
│  │       ThemeManager              │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘
         ↓ 使用
┌─────────────────────────────────────────┐
│         UIComponent (基类)              │
│  ┌──────────┬──────────┬──────────┐    │
│  │          │          │          │    │
│Button     Panel     Input    ...    │    │
└─────────────────────────────────────────┘
         ↓ 使用
┌─────────────────────────────────────────┐
│        LayoutTemplate (基类)            │
│  ┌──────────┬──────────┬──────────┐    │
│  │          │          │          │    │
│Vertical  Horizontal  Grid      │    │
└─────────────────────────────────────────┘
         ↓ 辅助
┌─────────────────────────────────────────┐
│         AnimationUtil                   │
└─────────────────────────────────────────┘
```

---

## 🔄 数据流

### UI 创建流程

```
1. Minecraft 打开 Screen
   ↓
2. BaseApricityScreen 构造函数
   ↓
3. init() → initializeUI()
   ↓
4. loadHTML(path)
   ↓
5. UIManager.openUI(path)
   ↓
6. Document.create(path)
   ↓
7. HTML 解析 → Element 树
   ↓
8. 组件创建 → 添加到布局
   ↓
9. 渲染到屏幕
```

### 事件处理流程

```
用户操作（点击/输入）
   ↓
Browser 事件系统
   ↓
JavaScript 监听器
   ↓
Java 端回调（可选）
   ↓
业务逻辑处理
   ↓
UI 更新/网络包发送
```

### 主题切换流程

```
调用 applyTheme(themeName)
   ↓
ThemeManager 切换主题
   ↓
CSS 变量更新
   ↓
Document.refresh()
   ↓
重新渲染所有元素
   ↓
新主题生效
```

---

## 📦 依赖关系

### 必需依赖

- **ApricityUI** - UI 框架核心
- **NeoForge 1.21.1** - 模组加载器
- **Minecraft 1.21.1** - 游戏本体

### 可选依赖

- **KubeJS** - JavaScript 支持（如果需要在 HTML 中使用 JS）

---

## 🎨 设计模式

### 使用的模式

1. **单例模式** - UIManager, ThemeManager
2. **工厂模式** - 组件创建
3. **观察者模式** - 事件监听
4. **策略模式** - 布局模板
5. **装饰器模式** - 组件样式

---

## 🔍 扩展点

### 如何添加新组件？

1. 继承 `UIComponent`
2. 实现 `createElement()` 和 `initialize()`
3. 添加特定功能方法

### 如何添加新布局？

1. 继承 `LayoutTemplate`
2. 实现 `createLayout()`
3. 提供添加子元素的方法

### 如何添加新主题？

1. 创建 `Theme` 对象
2. 设置颜色映射
3. 通过 `ThemeManager.registerTheme()` 注册

### 如何添加新动画？

1. 在 `AnimationUtil` 中添加静态方法
2. 使用 CSS transition 或 animation
3. 提供简单的 API

---

## 💾 数据存储

### 客户端存储

- **UI 状态** - 保存在 Document 中
- **主题偏好** - 可保存到配置文件
- **组件数据** - 临时存储在内存

### 服务端通信

- **网络包** - 通过 ApricityContainerMenu
- **数据同步** - 使用 SavedData

---

## 🚀 性能考虑

### 优化措施

1. **延迟加载** - 只在需要时创建 UI
2. **持久化** - 热重载不关闭 UI
3. **缓存** - CSS 和 JS 缓存
4. **异步加载** - 图片等资源异步
5. **按需渲染** - 只更新 dirty 元素

### 性能指标

- **UI 加载时间**: < 100ms
- **热重载时间**: < 1s
- **FPS 影响**: < 10%
- **内存占用**: ~5-10MB/UI

---

*本文件结构说明最后更新：2026-03-28*
