# CLAUDE.md

从现在开始，请始终使用简体中文回答问题，但代码中的函数名保持英文风格，注释请写中文。

## 构建与运行

任务完成后，不要构建测试。

- **Build**: `./gradlew build`
- **Run client**: `./gradlew runClient`
- **Run server**: `./gradlew runServer`
- **Run data generator**: `./gradlew runData`
- **Run gametests**: `./gradlew runGameTestServer`

需要 JDK 21。暂无测试套件 (`src/test` 为空)。

## 项目概况

**Minecraft NeoForge 1.21.1** mod（modid: `modularization_defend`），**类明日方舟塔防系统**。依赖：GeckoLib（动画）、LDLib2（GUI/节点图框架）、Curios（饰品槽）、GuideME（文档）、Photon（粒子效果）。

### 核心玩法流程

```
[关卡编辑器] 设计者搭建地形+门+控制器, 编写节点图 → 保存为关卡文件
    ↓
[作战控制台] 玩家选择关卡+词条+编队 → 启动作战
    ↓
[关卡维度] 加载关卡 → 玩家部署干员 → 自动战斗 → 胜利/失败
```

## 架构

### 关卡系统 (`level/`)

- **`LevelControllerBlockEntity`** — 关卡运行时宿主，实现 `ILevelHost`。管理关卡状态机（IDLE → RUNNING → VICTORY/DEFEAT），每 tick 驱动 `LevelGraphRuntime`，追踪生成的实体（漏怪判定 + AI 重绑），处理延迟/间隔波次等异步任务恢复。持有节点图 NBT（GZIP 压缩落盘，网络同步）。
- **`LevelGraphRuntime`** — 服务端运行时核心。从 NBT 惰性构建 `TurretLogicGraph` + `NodeGraphEvaluator`，每 tick 注入外部上下文（时间/波次/敌人数/波次清空/漏怪数），维护有状态节点（计时器/数据存储），边沿检测触发事件节点执行链。
- **`LevelControllerBlock`** — 控制器方块。右键切换关卡（IDLE→启动 / 运行/终态→复位），潜行右键打开嵌入式节点图编辑器。
- **`LevelState`** — 关卡状态枚举：`IDLE`, `RUNNING`, `VICTORY`, `DEFEAT`。
- **`DoorRegistry`** — 门 ID 查询工具（当前为静态，计划迁移为 `CombatSession` 实例级）。

### 关卡编辑器 (`Block/example/LevelEditor*.java`)

- **`LevelEditorBlock`** — 256x256x256 可建造区域，客户端线框渲染边界。右键打开 LDLib2 UI（保存/加载/清空）。
- **`LevelEditorBlockEntity`** — 实现 `IBoundingBlock`。`saveZone()`/`loadZone()`/`clearZone()` 待实现。保存时将方块序列化为 StructureTemplate NBT，扫描门 BE + 控制器 BE 提取元数据和节点图。
- **`LevelEditorUI`** — 基于 LDLib2 XML 的编辑器面板。

### 作战控制台 (`Block/example/CombatConsole*.java`)

- **`CombatConsoleBlock`** — 2 格高多方块结构。作战大厅入口：关卡列表、模式选择（危机合约/探索/联合作战）、词条配置、干员编队（12 人）、单人/多人组队。
- **`CombatConsoleBlockEntity`** — 目前为存根（仅 tick 计数器）。
- **`CombatConsoleUI`** — 基于 LDLib2 XML 的前端（`combat_console.xml`）。

### 节点图系统 (`nodegraph/`)

#### 图定义与编辑器

- **`TurretLogicGraph`** — `Graph` 子类。支持类型：BOOL/INT/LONG/FLOAT/DOUBLE/STRING + 3 个自定义枚举类型（`MathFunction`/`CompareFunction`/`ConversionTarget`）。
- **`TurretLogicEditor`** — 基于 LDLib2 `Editor` 的图资源编辑器。带资源面板，支持外部图文件的 CRUD 和拖拽为外部子图。通过 `/nodegraph editor` 或 `PlayerUIMenuType` 打开。
- **`TurretLogicGraphResource`** — 注册 `.turret_logic.nbt` 文件格式。提供 built-in/global/mod 专属三个文件夹 provider。
- **`FocusingGraphResourceProviderContainer`** — 修复 LDLib2 标签页自动聚焦 + 双击已打开资源切换标签页的交互问题。
- **`NodeGraphCommand`** — `/nodegraph` 命令（调试用）：无参→演示图，`empty`→空白图，`editor`→资源编辑器。

#### 数据流求值引擎 (`nodegraph/eval/`)

- **`NodeGraphEvaluator`** — 递归遍历节点图连线求值。支持端口缓存（防重复计算）、循环检测（求值中节点返回 null）、外部输入注入（`externalInputs`）。通过静态 `EVALUATORS` 注册表按 `@NodeAttribute.name()` 分发求值。
- **`NodeEvaluators`** — 为所有数据流节点注册求值逻辑。初始化时调用 `init()`。

#### 执行流引擎 (`nodegraph/exec/`)

- **`LevelExecEngine`** — 执行流引擎。维护条件注册表和执行器注册表。沿 `exec`→`then` 连线遍历动作链，深度上限 64。支持异步恢复和边沿检测触发。
- **`ILevelHost`** — 关卡宿主契约接口：`spawnWave`, `scheduleResume`, `scheduleIntervalWave`, `broadcastMessage`, `giveReward`, `setLevelResult`, `resetLevelFromGraph`。
- **`LevelActionNode`** — 动作节点基类。持有 `exec`（输入）+ `then`（输出）执行流端口，子类可覆写为多出口。
- **`LevelEventNode`** — 事件节点基类。持有 `then` 执行输出端口，由引擎每 tick 检查触发条件。

### 节点类型速查

#### 数据流节点 (`nodegraph/node/`)

| 节点 | 功能 |
|---|---|
| `TargetCountNode` | 输出常量整数值 |
| `ThresholdGateNode` | 输入值 > 阈值 → 输出布尔 |
| `AndGateNode` / `OrGateNode` / `NotNode` / `XorNode` | 逻辑门，动态输入端口 |
| `DecimalToBinaryNode` / `BinaryToDecimalNode` | 进制转换 |
| `MathFunctionNode` | 二元/一元数学运算（ADD/SUBTRACT/MULTIPLY/DIVIDE/MODULO/POWER/SQRT/ABS/NEGATE/MIN/MAX） |
| `ComparatorNode` | 比较器（GREATER_THAN/LESS_THAN/GREATER_EQUAL/LESS_EQUAL/EQUAL/NOT_EQUAL） |
| `TimerNode` | 计时器（含 autoReset/强制归零），状态由 `LevelGraphRuntime` 维护 |
| `DataStoreNode` | 数据存储（初始值/写入使能/存储值），状态由 `LevelGraphRuntime` 维护 |
| `BaseConverterNode` 及 `conv/` 子类 | 类型转换（Float→/Int→/Bool→/String→ 到目标类型） |

#### 关卡执行流节点 (`nodegraph/node/level/`)

**事件节点**（执行链起点，边沿触发）：
- `OnLevelStartNode` — 关卡开始
- `OnWaveClearedNode` — 波次清空
- `OnConditionNode` — 数据流条件为真时触发
- `OnLeakLimitNode` — 漏怪达到阈值

**动作节点**（执行链中间/末端）：
- `SpawnWaveNode` — 在指定红门生成一波敌人
- `IntervalSpawnWaveNode` — 间隔生成波次（异步：全部生成完毕后恢复链）
- `DelayNode` — 延迟 N 秒后恢复执行链（异步）
- `BranchNode` — 条件分支（True/False 两个执行出口）
- `SendMessageNode` — 向附近玩家广播消息
- `GiveRewardNode` — 给予物品奖励
- `SetLevelResultNode` — 判定关卡胜利/失败
- `ResetLevelNode` — 复位关卡（可选自动重开）

**查询节点**（数据流，读取关卡状态）：
- `LevelTimeNode` — 关卡运行秒数
- `AliveEnemyCountNode` — 存活敌人数
- `CurrentWaveNode` — 当前波次
- `LeakCountNode` — 累计漏怪数

#### 外部上下文 Key（`LevelGraphRuntime` 每 tick 注入到 `NodeGraphEvaluator`）

| Key | 类型 | 含义 |
|---|---|---|
| `level:running` | Boolean | 关卡运行中 |
| `level:time` | Float | 运行秒数 |
| `level:wave` | Integer | 当前波次 |
| `level:enemies` | Integer | 存活敌人数 |
| `level:waveCleared` | Boolean | 波次是否清空 |
| `level:leaks` | Integer | 累计漏怪数 |
| `timer:elapsed:<nodeUid>` | Float | 计时器节点已过秒数 |
| `store:<nodeUid>` | Float | 数据存储节点当前值 |

### 门系统 (`Block/example/{Red,Blue}Door*`)

- **红门（RedDoor）** — 出怪口。含 `doorId`，敌人在此生成并移向蓝门。
- **蓝门（BlueDoor）** — 防守点。敌人进入其结构 AABB 即触发漏怪判定。
- 门 ID 通过手持工具右键设置。控制器通过 `DoorRegistry.findDoor()` 按 ID 查找（64 格范围）。

### 可部署区 (`Block/DeployableZone*`, `Block/example/DeployableZoneHigh*`)

- **`DeployableZoneBlock`**（地面）/ **`DeployableZoneHighBlock`**（高台）— 干员部署位置标记。每格限放一个干员。

### 敌人 AI (`level/MoveToBlueDoorGoal.java`)

- 生成后清空原生 AI（仇恨/行为），仅保留前往目标蓝门位置的寻路 Goal。

### 弹射物系统 (`entity/Projectile.java`)

继承 Minecraft `Projectile` 的抽象基类：
- 平滑客户端插值（`deltaMovementOld` 防首帧闪烁）
- 可选制导追踪（`handleHoming()`）
- 碰撞检测（`ProjectileUtil.getHitResultOnMoveVector`）
- 子类钩子：`onHitEntity()`、`onHitBlock()`、`onClientTick()`、`onServerTick()`

### 实体追踪框架 (`entity/EntityTrace/`)

- **`EntitySearchUtil`** — 性能优化的实体查询（float 距离计算、AABB 预筛选），支持"最近"/"全部"/"快速模式"（限 10 候选）
- **`EntityTracker`** — 有状态追踪器（UNLOCKED → TRACKING → LOST → INVALID）
- **`EntityFilter`** — `Predicate<Entity>` 函数式接口

### 数据组件系统 (`DataComponents/`)

三种 NeoForge 数据组件（Codec 持久化 + 网络同步）：

| 组件 | 关键字段 | 使用者 |
|---|---|---|
| `TurretCoreData` | harmLevel, energyLevel, turretType, energy/maxEnergy | 炮塔核心物品 |
| `DefendCoreData` | harmLevel, energyExpendLevel, energyMax/Current, shieldCapacity/Active, 5 个升级等级 | DefendCore 饰品 |
| `TargetFilterData` | filterType, 可选 entityId | 目标过滤器物品 |

### 词条系统 (`trait/`)

- **`ModTraits`** — 自定义词条注册表（`DeferredRegister`），含 `ATTACHMENT_TYPES` 用于实体数据附着。
- **`TraitCommand`** — `/trait` 命令行工具（赋予/移除/列出词条）。
- 词条在关卡中混合生效：系统层直接修改实体属性（如加攻击力词条等级） + 注入节点图外部上下文供创作者在图中响应。

### 多方块占位系统 (`Block/bounding/`)

- `BoundingBlock` / `BoundingBlockEntity` — 透明占位块，用于实现大于 1x1x1 的多方块结构（如 CombatConsole 的 2 格高柱体）。
- `BoundingHelper` — 放置/拆除占位方块的辅助方法。
- `IBoundingBlock` — 占位方块接口，部分方块的 BE 实现此接口以接收代理调用。

### 配置系统 (`config/`)

三个 TOML 模块，加载于 `config/modularization_defend/`：
- **`GeneralConfig`** — 通用设置
- **`TurretConfig`** — 炮塔参数
- **`DebugConfig`** — 调试开关 + 日志级别

### 网络层

使用 NeoForge `PayloadRegistrar`（协议版本 "1"）：
- `OpenGraphEditorPacket` — 服务端→客户端，打开节点图编辑器
- `SaveCardGraphPacket` — 客户端→服务端，保存程序存储卡图数据
- `SaveControllerGraphPacket` — 客户端→服务端，保存控制器图数据

### UI

基于 LDLib2 XML + `ModularUI` 框架：
- `LevelEditorUI` — 编辑器控制面板（保存/加载/清空）
- `CombatConsoleUI` — 作战大厅（模式选择、词条、编队）

### 注册与事件

所有注册使用 `DeferredRegister`。事件处理器：
- **Mod bus** — Config 加载/重载、玩家加入欢迎消息、数据包注册
- **NeoForge bus** — `EntitySelectorHandler`、`RegisterCommandsEvent`（`NodeGraphCommand`、`TraitCommand`、`DebugDumpComponentsCommand`）
- **客户端** — `ClientModEvents` 内部类（渲染器: BlueDoor, RedDoor, LevelEditor）

### 关键包速查

| 包 | 用途 |
|---|---|
| `level/` | 关卡系统（控制器、图运行时、门注册、关卡状态、敌人 AI） |
| `nodegraph/` | 节点图定义、编辑器、网络包 |
| `nodegraph/eval/` | 数据流求值引擎 + 节点求值器注册 |
| `nodegraph/exec/` | 执行流引擎 + 宿主接口 + 动作/事件节点基类 |
| `nodegraph/node/` | 数据流节点实现（逻辑门、数学、比较、计时、存储、转换） |
| `nodegraph/node/level/` | 关卡域节点实现（事件、动作、查询） |
| `nodegraph/network/` | 节点图网络数据包 |
| `Block/` | 方块 + BE 注册 + 渲染器 |
| `Block/example/` | 具体方块实现（门、可部署区、控制台、编辑器、控制器） |
| `Block/bounding/` | 多方块占位方块系统 |
| `Block/render/` | 方块实体渲染器（BlueDoor/RedDoor/LevelEditor） |
| `entity/` | 弹射物实体 |
| `entity/EntityTrace/` | 实体搜索与追踪框架 |
| `DataComponents/` | 数据组件定义 |
| `trait/` | 词条注册表 + 命令 + 实现 |
| `config/` | NeoForge `ModConfigSpec` 配置规范 |
| `util/` | 工具类（数据压缩、tick 计数器、调试命令） |

## 待开发模块

- **`CombatSessionManager`** + **`CombatSession`** — 全局作战会话管理器。固定槽位池 + BitSet 占用标记，关卡生命周期（分配→加载→运行→清理→释放）。
- **关卡维度** — 代码注册的超平坦维度（512x512 移动限制，不限高）。
- **关卡文件格式** — StructureTemplate NBT + 自定义元数据扩展根节点（关卡名/作者/尺寸/门映射/节点图）。
- **门注册重构** — 静态 `DoorRegistry` → `CombatSession` 实例级 `Map<Integer, BlockPos>`。
- **`LevelEditorBlockEntity`** 填充 — `saveZone()`/`loadZone()`/`clearZone()` 实现 + 保存/加载验证。
- **`CombatConsoleBlock`** 完善 — 关卡列表 UI（启动时扫描）、词条配置、编队管理、启动作战流程。

### 设计参考

关卡编辑器区域：256x256x256（可建造）→ 超平坦维度 512x512（移动限制）。关卡文件保存在 `config/modularization_defend/levels/`，启动时扫描。每个关卡一个控制器，一关一图。门 ID 是数字，在编辑器中通过手持工具右键设置。

## ApricityUI 界面生成

生成 Minecraft ApricityUI 界面时，**必须先加载 `/apricity-ui` skill**，它包含完整的标签、CSS、JS 能力边界与约束规则。不要在未加载该 skill 的情况下直接生成 ApricityUI 代码。
