package org.lingZero.modularization_defend.nodegraph.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.lingZero.modularization_defend.modularization_defend;
import org.lingZero.modularization_defend.nodegraph.GraphEditorOpener;

/**
 * 服务端→客户端数据包：通知客户端打开节点图编辑器。
 */
public record OpenGraphEditorPacket(boolean empty) implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(modularization_defend.MODID, "open_graph_editor");

    public static final Type<OpenGraphEditorPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenGraphEditorPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, OpenGraphEditorPacket::empty,
                    OpenGraphEditorPacket::new
            );

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** 客户端处理器 */
    public static void handleClient(OpenGraphEditorPacket payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (payload.empty()) {
                GraphEditorOpener.openEmptyTurretLogicEditor();
            } else {
                GraphEditorOpener.openTurretLogicEditor();
            }
        });
    }
}
