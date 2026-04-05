# 机器GUI框架使用说明

这个GUI框架提供了类似于Mekanism的机器界面功能，包含可拖动的悬浮窗、升级配置等特性。

## 框架结构

### 核心类

1. **BaseMachineScreen** - 机器GUI的基础类
   - 提供窗口管理功能
   - 支持拖拽操作
   - 处理元素渲染和交互

2. **MachineElement** - 所有GUI元素的基类
   - 提供基本的渲染和交互功能
   - 支持工具提示
   - 可扩展的tick更新

3. **MachineWindow** - 可拖动的悬浮窗口
   - 支持窗口拖拽
   - 提供子元素管理
   - 支持固定(pin)功能

4. **MachineSlot** - 物品槽位元素
   - 支持不同类型的槽位（输入、输出、能量、升级等）
   - 提供物品渲染和交互
   - 悬停效果

5. **UpgradeConfigWindow** - 升级配置窗口
   - 提供升级管理界面
   - 支持升级安装/移除
   - 显示升级效果和描述

## 使用示例

### 创建机器GUI

```java
public class MyMachineScreen extends BaseMachineScreen<MyContainer> {
    
    private UpgradeConfigWindow upgradeWindow;
    
    public MyMachineScreen(MyContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.dynamicSlots = true;
    }
    
    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        
        // 添加配置按钮
        ConfigButton upgradeButton = new ConfigButton(this, 
            leftPos + imageWidth + 5, leftPos + 10, 
            20, 20, Component.literal("↑"), 
            ConfigButton.Type.UPGRADE, this::onUpgradeClicked);
        
        addRenderableWidget(upgradeButton);
    }
    
    private void onUpgradeClicked() {
        if (upgradeWindow == null || !upgradeWindow.isVisible()) {
            upgradeWindow = new UpgradeConfigWindow(this, 
                leftPos + imageWidth + 30, topPos + 10);
            addWindow(upgradeWindow);
        } else {
            removeWindow(upgradeWindow);
            upgradeWindow = null;
        }
    }
}
```

### 自定义GUI元素

```java
public class MyCustomElement extends MachineElement {
    
    public MyCustomElement(BaseMachineScreen<?> screen, int x, int y, int width, int height) {
        super(screen, x, y, width, height);
    }
    
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 渲染背景
        guiGraphics.fill(x, y, x + width, y + height, 0xFF404040);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY)) {
            // 处理点击事件
            return true;
        }
        return false;
    }
}
```

### 创建自定义窗口

```java
public class MyConfigWindow extends MachineWindow {
    
    public MyConfigWindow(BaseMachineScreen<?> screen, int x, int y) {
        super(screen, x, y, 200, 150);
        
        // 添加窗口元素
        MachineSlot slot = new MachineSlot(screen, x + 10, y + 30);
        addChild(slot);
        
        // 添加按钮
        Button closeButton = new Button(screen, x + width - 30, y + 5, 20, 10, "X", this::close);
        addChild(closeButton);
    }
    
    @Override
    protected Component getTitle() {
        return Component.literal("我的配置窗口");
    }
}
```

## 特性说明

### 1. 窗口系统
- 支持多窗口同时显示
- 窗口可拖动和固定
- 窗口层级管理（最后点击的窗口在最上层）
- ESC键关闭未固定的窗口

### 2. 升级系统
- 支持多种升级类型（速度、能量、效率、容量、安全）
- 升级等级系统（最大4级）
- 升级效果显示
- 升级物品管理

### 3. 槽位系统
- 多种槽位类型（普通、输入、输出、能量、升级）
- 不同颜色标识
- 物品渲染和数量显示
- 悬停工具提示

### 4. 交互系统
- 鼠标点击、拖拽、滚轮支持
- 键盘输入支持
- 元素焦点管理
- 工具提示显示

## 纹理资源

框架使用以下纹理资源（需要在资源包中提供）：

- `modularization_defend:textures/gui/base.png` - GUI背景纹理
- `modularization_defend:textures/gui/shadow.png` - 窗口阴影纹理
- `modularization_defend:textures/gui/blur.png` - 模糊效果纹理

## 扩展建议

1. **添加新的升级类型**：扩展 `UpgradeConfigWindow.UpgradeType` 枚举
2. **自定义槽位行为**：继承 `MachineSlot` 类并重写相关方法
3. **创建新的窗口类型**：继承 `MachineWindow` 类
4. **添加能量显示**：创建能量条元素类
5. **实现进度条**：创建进度显示元素类

## 注意事项

1. 所有GUI元素都应该继承自 `MachineElement`
2. 窗口应该通过 `addWindow()` 和 `removeWindow()` 方法管理
3. 使用 `addRenderableWidget()` 添加可渲染元素
4. 确保在 `addGuiElements()` 方法中添加所有GUI元素
5. 纹理资源需要正确放置在资源包中

这个框架提供了创建复杂机器GUI的基础，可以根据具体需求进行扩展和定制。