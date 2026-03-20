package org.lingZero.modularization_defend.AgreementCoreNetwork.api;

import net.minecraft.nbt.CompoundTag;
import org.lingZero.modularization_defend.AgreementCoreNetwork.impl.ConnectionPointImpl;

/**
 * 连接点接口
 * 表示节点上的具体连接位置
 * 
 * @author Modularization Defend Team
 */
public interface IConnectionPoint extends Comparable<IConnectionPoint> {
    
    /**
     * 获取连接点位置
     * @return 方块位置
     */
    net.minecraft.core.BlockPos position();
    
    /**
     * 获取连接点 ID（用于区分同一方块上的不同连接点）
     * @return 连接点 ID
     */
    int getId();
    
    /**
     * 创建 NBT 标签
     * @return NBT 数据
     */
    CompoundTag createTag();
    
    /**
     * 从 NBT 读取
     * @param tag NBT 标签
     * @return 连接点
     */
    static IConnectionPoint fromTag(CompoundTag tag) {
        return new ConnectionPointImpl(tag);
    }
}
