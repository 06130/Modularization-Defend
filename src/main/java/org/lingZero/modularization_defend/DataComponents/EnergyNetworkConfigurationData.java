package org.lingZero.modularization_defend.DataComponents;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * 电网配置工具的数据组件
 * 用于存储两次点击的坐标信息
 */
public class EnergyNetworkConfigurationData {
    // Codec 用于持久化（保存到磁盘）
    public static final Codec<EnergyNetworkConfigurationData> CODEC = null; // 暂时设为 null，实际使用时可以添加
    
    // StreamCodec 用于网络同步
    public static final StreamCodec<ByteBuf, EnergyNetworkConfigurationData> STREAM_CODEC = StreamCodec.ofMember(
            EnergyNetworkConfigurationData::writeToPacket,
            EnergyNetworkConfigurationData::readFromPacket
    );

    @Nullable
    public final BlockPos firstClick;
    @Nullable
    public final BlockPos secondClick;

    public EnergyNetworkConfigurationData(@Nullable BlockPos firstClick, @Nullable BlockPos secondClick) {
        this.firstClick = firstClick != null ? firstClick.immutable() : null;
        this.secondClick = secondClick != null ? secondClick.immutable() : null;
    }

    private void writeToPacket(ByteBuf buf) {
        boolean hasFirst = firstClick != null;
        buf.writeBoolean(hasFirst);
        if (hasFirst) {
            buf.writeInt(firstClick.getX());
            buf.writeInt(firstClick.getY());
            buf.writeInt(firstClick.getZ());
        }

        boolean hasSecond = secondClick != null;
        buf.writeBoolean(hasSecond);
        if (hasSecond) {
            buf.writeInt(secondClick.getX());
            buf.writeInt(secondClick.getY());
            buf.writeInt(secondClick.getZ());
        }
    }

    private static EnergyNetworkConfigurationData readFromPacket(ByteBuf buf) {
        BlockPos firstClick = null;
        if (buf.readBoolean()) {
            firstClick = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        }

        BlockPos secondClick = null;
        if (buf.readBoolean()) {
            secondClick = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        }

        return new EnergyNetworkConfigurationData(firstClick, secondClick);
    }

    public EnergyNetworkConfigurationData withFirstClick(BlockPos pos) {
        return new EnergyNetworkConfigurationData(pos.immutable(), null);
    }

    public EnergyNetworkConfigurationData withSecondClick(BlockPos pos) {
        return new EnergyNetworkConfigurationData(this.firstClick, pos.immutable());
    }

    public boolean hasFirstClick() {
        return firstClick != null;
    }

    public boolean hasSecondClick() {
        return secondClick != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof EnergyNetworkConfigurationData)) return false;
        EnergyNetworkConfigurationData other = (EnergyNetworkConfigurationData) obj;
        return Objects.equals(firstClick, other.firstClick) && 
               Objects.equals(secondClick, other.secondClick);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstClick, secondClick);
    }
}
