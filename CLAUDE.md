# CLAUDE.md

从现在开始，请始终使用简体中文回答问题，但代码中的函数名保持英文风格，注释请写中文。

## 构建与运行
    任务完成后，不要构建测试
- **Build**: `./gradlew build`
- **Run client**: `./gradlew runClient`
- **Run server**: `./gradlew runServer`
- **Run data generator**: `./gradlew runData`
- **Run gametests**: `./gradlew runGameTestServer`

需要 JDK 21。暂无测试套件 (`src/test` 为空)。

## 项目概况

**Minecraft NeoForge 1.21.1** mod（modid: `modularization_defend`），模块化炮塔防御系统。依赖：GeckoLib（动画）、LDLib2（GUI 框架）、Curios（饰品槽）、GuideME（文档）、Photon（粒子效果）。

## 架构

### 模块化炮塔架构

炮塔系统采用 **组合优于继承** 模式：

- **`BaseTurretBlockEntity`** — 抽象 BE，定义炮塔生命周期。持有 3 格物品栏（核心、制导控制器、目标过滤器）和 `TurretFireSystem` 实例。子类需实现：
  - `acquireTarget()` → 返回 `EntityTracker`
  - `onFire(Entity target)` → 对锁定目标执行攻击
  - Tick 流程：`tick()` → `fireSystem.tick()`（搜索+射击）→ `onCustomTick()`（可选钩子，用于粒子/动画）

- **`TurretFireSystem`** — 封装所有火控逻辑：目标搜索冷却、锁定、目标丢失后重新捕获、射速控制。不关心具体攻击机制，委托给 `BaseTurretBlockEntity.performFire()`

- **`BaseTurretBlock`** — 抽象方块，处理右键交互（安装/替换炮塔核心、制导控制器、目标过滤器）。提供 `dismantleStructure()` 用于拆除结构

- **具体实现**（如 `BasicBulletTurretV1BlockEntity`）将配置值（射速、射程、搜索高度）接入火控系统，通过实体追踪框架和 `TurretCoreItem.executeAttack()` 实现 `acquireTarget()` / `onFire()`

### 多方块系统

炮塔可以是多方块结构。`AffiliateBlock` / `AffiliateBlockEntity` 是"代理"方块：
1. 通过 NBT 存储指向主 `BaseTurretBlock` 位置的引用
2. 将 `ItemHandler` 能力查询重定向到主方块实体（带一次性缓存）
3. 对附属方块的右键点击转发到 `BaseTurretBlock.handleInteraction()`

结构尺寸由方块类的 `getStructureWidth()` / `getStructureHeight()` 定义。`TurretRemovalTool` 负责受控拆除。

### 实体追踪框架 (`entity/EntityTrace/`)

独立的实体查找与追踪系统：

- **`EntitySearchUtil`** — 静态工具类，性能优化的实体查询（float 距离计算、AABB 预筛选）。支持"最近"和"全部"模式，以及限制 10 个候选的"快速模式"
- **`EntityTracker`** — 有状态追踪器，锁定 UUID，缓存实体引用，通过 `TrackingState` 枚举报告状态（UNLOCKED → TRACKING → LOST → INVALID）。支持重新锁定和目标丢失回调
- **`IEntitySearch`** / **`IEntityTracker`** — 提供静态工厂方法和追踪器契约的接口
- **`EntityFilter`** — `Predicate<Entity>` 函数式接口，`EntityFilters` 中提供常见分类工厂方法（敌对/中立/友好生物、玩家、按实体 ID）

### 数据组件系统 (`DataComponents/`)

三种数据组件类型使用 NeoForge 现代数据组件 API（基于 Codec，持久化 + 网络同步）：

| 组件 | 关键字段 | 使用者 |
| --- | --- | --- |
| `TurretCoreData` | harmLevel, energyLevel, turretType, energy/maxEnergy | 炮塔核心物品 |
| `DefendCoreData` | harmLevel, energyExpendLevel, energyMax/Current, shieldCapacity/Active, 5 个升级等级 | 玩家穿戴的 DefendCore 饰品 |
| `TargetFilterData` | filterType (HOSTILE/NEUTRAL/FRIENDLY/PLAYER/ENTITY_ID), 可选 entityId | 目标过滤器物品 |

均使用 `RecordCodecBuilder` 序列化，同时支持 `CODEC`（磁盘）和 `STREAM_CODEC`（网络，通过 `ByteBufCodecs.fromCodec`）。

### 弹射物系统 (`entity/Projectile.java`)

继承 Minecraft `Projectile` 的抽象基类：
- 平滑客户端插值（管理 `deltaMovementOld` 防止首帧闪烁）
- 可选制导追踪（`handleHoming()`，线性插值转向）
- 碰撞检测（`ProjectileUtil.getHitResultOnMoveVector`）
- 子类钩子：`onHitEntity()`、`onHitBlock()`、`onClientTick()`、`onServerTick()`

### 配置系统 (`config/`)

三个模块以独立 TOML 文件加载于 `config/modularization_defend/`：
- **`GeneralConfig`** — 最大连接距离、心跳间隔、炮塔核心升级上限
- **`TurretConfig`** — 每种炮塔类型的参数（如 `BasicBulletV1Stats`: fireRate, damage, range, energyCapacity, searchHeight）
- **`DebugConfig`** — 启用标志 + 日志级别（TRACE/DEBUG/INFO/WARN/ERROR）

配置值通过 `Config` 类的静态 getter 访问。

### 网络层 (`network/`)

使用 NeoForge `PayloadRegistrar`（协议版本 "1"）。当前只有一个数据包：`OpenDefendCoreGUIMessage`（客户端→服务端，play 阶段）。

### UI (`ldlibUI/`)

DefendCore GUI 基于 LDLib2 的 `ModularUI` 系统构建。`DefendCoreContainerMenu` 是服务端容器，`DefendCoreGUI` 构建 `ModularUI` 组件树，`NewUIScreen` 将其包装在 `AbstractContainerScreen` 中。GUI 通过快捷键注册打开（默认：G）。

### 注册与事件

所有注册使用 NeoForge `DeferredRegister` 系统，在 `ModularizationDefend` 构造函数中声明。事件处理器分为：
- **Mod bus** (`@EventBusSubscriber(bus = Mod)`) — Config（配置加载/重载、玩家加入欢迎消息）
- **Forge bus** — `RightClickItemHandler`、`ClientKeyInputHandler`、`EntityIdFilter`
- **客户端初始化** — `ModularizationDefend` 中的 `ClientModEvents` 内部类（渲染器、屏幕）

### 关键包速查

| 包 | 用途 |
| --- | --- |
| `Register/` | 所有延迟注册（方块、物品、BE、实体、菜单、数据组件、创造标签页、快捷键、能力） |
| `Blocks/MultiblockFrame/` | 抽象炮塔框架（基础 BE、基础方块、附属方块、火控系统） |
| `Blocks/Multiblock/` | 具体炮塔实现 |
| `Items/TurretCore/` | 炮塔核心物品（抽象 `TurretCoreItem` + 具体类型） |
| `Items/TargetFilter/` | 目标过滤器物品（抽象 `BaseTargetFilter` + 各类型过滤器） |
| `Items/GuidanceControl/` | 制导控制组件 |
| `entity/EntityTrace/` | 实体搜索与追踪框架 |
| `entity/` | 弹射物实体（抽象 `Projectile`、`LaserProjectile`） |
| `config/` | 配置规范类（NeoForge `ModConfigSpec`） |
| `network/` | 网络数据包注册与消息处理 |
| `ldlibUI/` | 基于 LDLib2 的 GUI 界面与容器 |
| `util/` | 调试日志（分片输出）、调试命令、能量格式化、时间工具 |

## ApricityUI 界面生成

生成 Minecraft ApricityUI 界面时，**必须先加载 `/apricity-ui` skill**，它包含完整的标签、CSS、JS 能力边界与约束规则。不要在未加载该 skill 的情况下直接生成 ApricityUI 代码。