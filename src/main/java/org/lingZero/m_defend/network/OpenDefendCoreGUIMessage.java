package org.lingZero.m_defend.network;

import com.sighs.apricityui.ApricityUI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.lingZero.m_defend.DataComponents.DefendCoreData;
import org.lingZero.m_defend.Items.DefendCore;
import org.lingZero.m_defend.ModularizationDefend;
import org.lingZero.m_defend.util.DebugLogger;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * 客户端请求打开 DefendCore GUI 的网络消息
 * 使用 ApricityUI 引擎渲染菜单，并同步玩家 DefendCore 数据到客户端
 */
public record OpenDefendCoreGUIMessage() implements CustomPacketPayload {

    private static final String TEMPLATE_PATH = "defend_core/index.html";

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
     * 在服务端使用 ApricityUI 打开 DefendCore 菜单，并同步能量/护盾数据到客户端
     */
    public static void handle(OpenDefendCoreGUIMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            DebugLogger.info("收到打开 DefendCore GUI 的请求，使用 ApricityUI 引擎，玩家: {}", player.getName().getString());

            // 打开 GUI
            ApricityUI.openScreen(player, TEMPLATE_PATH, ApricityUI.bind().build());

            // 读取玩家 DefendCore 数据并发送给客户端
            CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                var curios = handler.findCurios(item -> item.getItem() instanceof DefendCore);
                if (!curios.isEmpty()) {
                    var stack = curios.getFirst().stack();
                    DefendCoreData data = DefendCore.getData(stack);
                    DebugLogger.info("同步 DefendCore 数据到客户端: energy={}/{}, shield={}/{}, active={}",
                            data.energyCurrent(), data.energyMax(),
                            data.shieldCapacity(), data.shieldMax(),
                            data.shieldActive());
                    PacketDistributor.sendToPlayer(player, new SyncDefendCoreDataMessage(data));
                }
            });
        });
    }
}