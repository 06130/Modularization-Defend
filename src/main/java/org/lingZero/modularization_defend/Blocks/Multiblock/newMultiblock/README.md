# 多方块系统使用指南

## 概述

这是一个基于 NeoForge 1.21.1 的多方块系统，提供了快速创建和管理多方块结构的功能。

## 已实现的功能模块

### 1. IMultiblockStructure - 多方块结构接口
定义了多方块结构的基本信息和验证方法。

### 2. MultiblockStructure - 多方块结构实现
使用构建器模式创建多方块结构，支持：
- 多层结构定义（aisle 方法）
- 字符映射方块（where 方法）
- 主方块标记（setMaster 方法）
- 自定义验证逻辑

### 3. MultiblockNbtManager - NBT 数据管理
负责管理多方块的 NBT 数据，包括：
- 主方块坐标存储
- 结构名称和 ID
- 成型状态标记
- 数据读写操作

### 4. MultiblockPlacer - 多方块放置
处理多方块的放置流程：
- 检测结构完整性
- 检查放置空间
- 放置所有结构方块
- 设置 NBT 数据

### 5. MultiblockBreaker - 多方块破坏
处理多方块的破坏逻辑：
- 自动识别主方块和结构方块
- 通过 NBT 重定向到主方块
- 破坏整个结构

### 6. MultiblockGuiHandler - GUI 处理
处理右键打开 GUI 的逻辑：
- 自动重定向到主方块
- 支持自定义 GUI 提供者
- 可扩展其他功能

### 7. AbstractMultiblock - 抽象基类（核心）
提供快速创建多方块的基础功能，整合了以上所有模块。

## 使用示例

### 基础示例

```java
public class MyMultiblock extends AbstractMultiblock {
    
    public MyMultiblock() {
        super("my_multiblock");
    }
    
    @Override
    protected IMultiblockStructure createStructure() {
        return MultiblockStructure.start("MyMultiblock")
                // 定义第一层
                .aisle(
                    "BBB",
                    "BBB",
                    "BBB"
                )
                // 定义第二层
                .aisle(
                    "BBB",
                    "BYB",  // Y 是主方块位置
                    "BBB"
                )
                // 定义字符对应的方块
                .where('B', RegisterBlocks.MULTIBLOCK_PART.get())
                .where('Y', (level, pos, state) -> {
                    // 自定义验证逻辑
                    return true;
                })
                .setMaster('Y')
                .build();
    }
    
    @Override
    protected Block getMasterBlock() {
        return RegisterBlocks.MULTIBLOCK_MASTER.get();
    }
    
    @Override
    protected Block getPartBlock() {
        return RegisterBlocks.MULTIBLOCK_PART.get();
    }
}
```

### 复杂结构示例（可变高度）

```java
public class LargeMultiblock extends AbstractMultiblock {
    
    public LargeMultiblock() {
        super("large_multiblock");
    }
    
    @Override
    protected IMultiblockStructure createStructure() {
        return MultiblockStructure.start("LargeMultiblock")
                // 第一层 - 5x5 底座
                .aisle(
                    "BBBBB",
                    "BBBBB",
                    "BBBBB",
                    "BBBBB",
                    "BBBBB"
                )
                // 第二到五层 - 3x3
                .aisle(" BBB ", " BBB ", " BBB ")
                .aisle(" BBB ", " BBB ", " BBB ")
                .aisle(" BBB ", " BBB ", " BBB ")
                .aisle(" BBB ", " BBB ", " BBB ")
                .where('B', getPartBlock())
                .setMaster('Y')
                .build();
    }
    
    // ... 其他实现
}
```

## 集成到方块中

### 主方块实现

```java
public class MultiblockMasterBlock extends Block implements EntityBlock {
    
    private final AbstractMultiblock multiblock;
    
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, 
                                Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            // 调用多方块的 GUI 处理方法
            multiblock.onRightClickGui(level, player, hand, pos);
        }
        return InteractionResult.SUCCESS;
    }
    
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, 
                        BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            // 调用多方块的破坏处理方法
            multiblock.onBreak(level, pos, null);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
```

### 结构方块实现

```java
public class MultiblockPartBlock extends Block implements EntityBlock {
    
    private final AbstractMultiblock multiblock;
    
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, 
                                Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            // 自动重定向到主方块
            multiblock.onRightClickGui(level, player, hand, pos);
        }
        return InteractionResult.SUCCESS;
    }
    
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, 
                        BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            // 自动重定向到主方块并破坏
            multiblock.onBreak(level, pos, null);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
```

## 注意事项

1. **所有结构方块都需要实现 EntityBlock**，因为需要存储 NBT 数据
2. **主方块坐标是相对于结构原点的**，需要正确计算偏移量
3. **结构验证会检查所有位置**，确保空间足够且无阻碍
4. **NBT 数据管理很重要**，确保在方块放置和破坏时正确处理
5. **GUI 功能需要自行实现**，MultiblockGuiHandler 提供了框架

## 扩展功能

### 自定义 GUI 提供者

```java
MultiblockGuiHandler.setGuiProvider(new MultiblockGuiHandler.GuiProvider() {
    @Override
    public boolean openGui(Level level, Player player, BlockPos masterPos) {
        // 实现你的 GUI 打开逻辑
        return true;
    }
    
    @Override
    public String getGuiTitle(Level level, BlockPos masterPos) {
        return "我的多方块 GUI";
    }
});
```

### 自定义验证逻辑

```java
.where('Y', (level, pos, state) -> {
    // 添加额外的验证条件
    return state.getBlock() instanceof MultiblockMasterBlock;
})
```

## 文件结构

```
newMultiblock/
├── IMultiblockStructure.java      # 结构接口
├── MultiblockStructure.java       # 结构实现
├── MultiblockNbtManager.java      # NBT 管理
├── MultiblockPlacer.java          # 放置处理
├── MultiblockBreaker.java         # 破坏处理
├── MultiblockGuiHandler.java      # GUI 处理
├── AbstractMultiblock.java        # 抽象基类（核心）
├── ExampleMultiblock.java         # 使用示例
└── TODO.txt                       # 任务清单（已完成）
```

## 下一步

1. 实现具体的主方块和结构方块
2. 实现 GUI 系统
3. 添加配方和处理逻辑
4. 测试和优化性能
