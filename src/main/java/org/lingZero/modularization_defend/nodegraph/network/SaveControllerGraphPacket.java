package org.lingZero.modularization_defend.nodegraph.network;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.lingZero.modularization_defend.level.LevelControllerBlockEntity;
import org.lingZero.modularization_defend.modularization_defend;

import javax.annotation.Nullable;

/**
 * 客户端→服务端数据包：将编辑后的图数据保存到关卡控制器方块。
 */
public record SaveControllerGraphPacket(BlockPos pos, @Nullable CompoundTag graphData) implements CustomPacketPayload {

    /** 保存操作允许的最大交互距离平方（防止越权修改远处方块） */
    private static final double MAX_DISTANCE_SQ = 64.0;

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(modularization_defend.MODID, "save_controller_graph");

    public static final Type<SaveControllerGraphPacket> TYPE = new Type<>(ID);

    private static final StreamCodec<RegistryFriendlyByteBuf, CompoundTag> TAG_CODEC = StreamCodec.of(
            (buf, tag) -> buf.writeNbt(tag),
            buf -> buf.readNbt()
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SaveControllerGraphPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, SaveControllerGraphPacket::pos,
                    TAG_CODEC, SaveControllerGraphPacket::graphData,
                    SaveControllerGraphPacket::new
            );

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** 服务端处理器：校验距离后将图数据写入关卡控制器 */
    public static void handleServer(SaveControllerGraphPacket payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (payload.graphData() == null) return;
            Player player = ctx.player();
            if (player.distanceToSqr(payload.pos().getCenter()) > MAX_DISTANCE_SQ) return;
            if (player.level().getBlockEntity(payload.pos()) instanceof LevelControllerBlockEntity be) {
                be.setGraph(payload.graphData());
            }
        });
    }
}
