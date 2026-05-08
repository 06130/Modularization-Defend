package org.lingZero.m_defend.network;

import com.sighs.apricityui.ApricityUI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.lingZero.m_defend.ModularizationDefend;
import org.lingZero.m_defend.util.DebugLogger;

/**
 * 客户端请求打开 DefendCore GUI 的网络消息
 * 使用 ApricityUI 引擎渲染菜单
 */
public record OpenDefendCoreGUIMessage() implements CustomPacketPayload {

    private static final String TEMPLATE_PATH = ModularizationDefend.MODID + ":defend_core/index.html";

    public static final Type<OpenDefendCoreGUIMessage> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(ModularizationDefend.MODID, "open_defend_core_gui")
    );

    public static final StreamCodec<FriendlyByteBuf, OpenDefendCoreGUIMessage> STREAM_CODEC =
        StreamCodec.unit(new OpenDefendCoreGUIMessage());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 在服务端使用 ApricityUI 打开 DefendCore 菜单
     */
    public static void handle(OpenDefendCoreGUIMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            DebugLogger.info("收到打开 DefendCore GUI 的请求，使用 ApricityUI 引擎，玩家: {}", player.getName().getString());
            ApricityUI.openScreen(player, TEMPLATE_PATH, ApricityUI.bind().build());
        });
    }
}