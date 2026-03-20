# AgreementCoreNetwork 网络系统实现

## 架构概述

本网络系统采用了**分层、模块化**的设计，将传输层与管理层完全分离，确保系统的可扩展性和可维护性。

## 核心特性

### 1. **传输层分离** ✅
- `ITransmitter<T>` - 通用传输器接口，支持泛型数据类型
- `ITransferData` - 传输数据接口，定义了能量、流体、物品、数据四种类型
- 可轻松扩展新的传输类型（如：流体传输、物品传输、数据传输等）

### 2. **接口分离** ✅
- `INetworkNode` - 基础网络节点接口
- `INetworkConnector` - 网络连接器接口（实现此接口的方块可以连接网络）
- `INetworkConnection` - 网络连接接口
- `ICableType` - 线缆类型接口
- `IEnergyConnector` - 能量专用连接器接口

### 3. **网络管理** ✅
- `GlobalNetwork` - 全局网络，管理整个维度的所有网络
- `LocalNetwork` - 局部网络，表示一个连通的子网络
- 自动处理网络的合并与分裂

### 4. **简化设计** ✅
- **无碰撞检测** - 移除了复杂的物理碰撞计算
- **无过载计算** - 不包含电线烧毁机制
- **专注于传输** - 核心逻辑专注于数据传输本身

## 文件结构

```
AgreementCoreNetwork/
├── api/                          # 核心接口层
│   ├── INetworkNode.java        # 网络节点接口
│   ├── INetworkConnector.java   # 网络连接器接口
│   ├── INetworkConnection.java  # 网络连接接口
│   ├── IConnectionPoint.java    # 连接点接口
│   ├── ICableType.java          # 线缆类型接口
│   └── transfer/                # 传输层接口
│       ├── ITransferData.java   # 传输数据接口
│       ├── ITransmitter.java    # 传输器接口
│       └── EnergyTransferData.java # 能量传输数据
│
├── impl/                         # 实现类
│   ├── ConnectionPointImpl.java     # 连接点实现
│   └── NetworkConnectionImpl.java   # 网络连接实现
│
├── network/                      # 网络管理层
│   ├── GlobalNetwork.java       # 全局网络
│   ├── LocalNetwork.java        # 局部网络
│   └── NetworkSyncManager.java  # 网络同步管理器
│
├── transfer/                     # 传输实现
│   └── energy/                  # 能量传输
│       ├── EnergyTransmitter.java   # 能量传输器
│       └── IEnergyConnector.java    # 能量连接器接口
│
└── NetworkManager.java           # 网络管理器（便捷访问）
```

## 使用示例

### 创建能量网络

```java
// 在方块实体中实现 IEnergyConnector
public class MyEnergyBlock extends BlockEntity implements IEnergyConnector {
    
    @Override
    public boolean isSource(IConnectionPoint cp) {
        // 返回 true 表示这是电源
        return true;
    }
    
    @Override
    public boolean isSink(IConnectionPoint cp) {
        // 返回 true 表示这是用电器
        return false;
    }
    
    @Override
    public int getAvailableEnergy() {
        // 返回可用能量
        return 1000;
    }
    
    @Override
    public void insertEnergy(int amount) {
        // 处理能量注入
    }
}
```

### 注册传输器

```java
// 在网络初始化时注册能量传输器
Level level = ...;
GlobalNetwork network = NetworkManager.getOrCreateNetwork(level);
network.registerTransmitter(
    ITransferData.DataType.ENERGY,
    new EnergyTransmitter(level)
);
```

### 添加连接

```java
// 创建连接
IConnectionPoint pointA = new ConnectionPointImpl(posA, 0);
IConnectionPoint pointB = new ConnectionPointImpl(posB, 0);
INetworkConnection connection = new NetworkConnectionImpl(
    cableType,
    pointA,
    pointB,
    false
);

// 添加到网络
GlobalNetwork network = ...;
network.addConnection(connection);
```

## 扩展新传输类型

### 1. 创建数据传输类

```java
public class FluidTransferData implements ITransferData {
    private FluidStack fluid;
    
    @Override
    public DataType getType() {
        return DataType.FLUID;
    }
    
    // 实现其他方法...
}
```

### 2. 创建传输器

```java
public class FluidTransmitter implements ITransmitter<FluidTransferData> {
    @Override
    public DataType getDataType() {
        return DataType.FLUID;
    }
    
    @Override
    public void update(List<INetworkConnection> connections) {
        // 实现流体传输逻辑
    }
    
    // 实现其他方法...
}
```

### 3. 注册传输器

```java
localNet.registerTransmitter(
    ITransferData.DataType.FLUID,
    new FluidTransmitter(level)
);
```

## 设计优势

### 1. **高内聚低耦合**
- 传输逻辑与管理逻辑完全分离
- 各模块职责明确，易于维护

### 2. **开闭原则**
- 对扩展开放，对修改关闭
- 新增传输类型无需修改现有代码

### 3. **单一职责**
- 每个类只负责一项职责
- `GlobalNetwork` 负责全局管理
- `LocalNetwork` 负责局部连通性
- `ITransmitter` 负责具体传输逻辑

### 4. **依赖倒置**
- 依赖于抽象接口而非具体实现
- 便于单元测试和 Mock

## 性能优化

### 1. **分级存储**
- `GlobalNetwork` 存储所有网络引用
- `LocalNetwork` 只存储局部数据
- 减少内存占用

### 2. **按需更新**
- 只在网络变化时标记为 dirty
- 避免不必要的保存操作

### 3. **版本控制**
- 每个 `LocalNetwork` 有 version 字段
- 快速判断网络是否发生变化

## 后续工作

### TODO:
1. 完善 NBT 序列化/反序列化
2. 实现网络包同步到客户端
3. 实现具体的能量传输算法
4. 添加更多的传输类型（流体、物品等）
5. 实现代理节点创建逻辑
6. 添加网络统计和调试信息

## 注意事项

⚠️ **重要提示：**
- 当前实现已移除碰撞检测和过载计算
- 如需这些功能，请在相应的 Transmitter 中添加
- 保持接口简洁，具体逻辑在实现类中完成
