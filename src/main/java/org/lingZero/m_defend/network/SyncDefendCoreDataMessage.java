package org.lingZero.m_defend.network;

import com.sighs.apricityui.ApricityUI;
import com.sighs.apricityui.init.Document;
import com.sighs.apricityui.init.Element;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.lingZero.m_defend.DataComponents.DefendCoreData;
import org.lingZero.m_defend.ModularizationDefend;
import org.lingZero.m_defend.util.DebugLogger;

/**
 * 服务端→客户端：同步 DefendCore 数据到 GUI
 */
public record SyncDefendCoreDataMessage(DefendCoreData data) implements CustomPacketPayload {

    private static final String TEMPLATE_PATH = "defend_core/index.html";

    public static final Type<SyncDefendCoreDataMessage> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ModularizationDefend.MODID, "sync_defend_core_data")
    );

    public static final StreamCodec<FriendlyByteBuf, SyncDefendCoreDataMessage> STREAM_CODEC =
            StreamCodec.composite(
                    DefendCoreData.STREAM_CODEC,
                    SyncDefendCoreDataMessage::data,
                    SyncDefendCoreDataMessage::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 客户端处理器：将数据同步到 ApricityUI Document 的 DOM 元素
     */
    public static void handle(SyncDefendCoreDataMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().player == null) return;

            DefendCoreData data = message.data();
            DebugLogger.info("客户端收到 DefendCore 同步数据: energy={}/{}, shield={}/{}, active={}",
                    data.energyCurrent(), data.energyMax(),
                    data.shieldCapacity(), data.shieldMax(),
                    data.shieldActive());

            // 可能有网络延迟，Document 还未创建，先尝试立即应用
            if (!applyToDocument(data)) {
                // Document 还未就绪，等待下一次 tick 再试
                PendingDataHolder.pending = data;
            }
        });
    }

    /**
     * 尝试将数据应用到 Document。成功返回 true，Document 不存在返回 false。
     */
    static boolean applyToDocument(DefendCoreData data) {
        var docs = ApricityUI.getDocument(TEMPLATE_PATH);
        if (docs.isEmpty()) return false;

        Document doc = docs.get(0);
        if (doc == null) return false;

        // 能量条
        var energyFill = doc.getElementById("energy-bar-fill");
        var energyText = doc.getElementById("energy-text");
        if (energyFill != null) {
            long cur = data.energyCurrent();
            long max = data.energyMax();
            double pct = max > 0 ? Math.min(100.0, (double) cur / max * 100.0) : 0.0;
            energyFill.setAttribute("style", String.format("width: %.1f%%;", pct));
        }
        if (energyText != null) {
            setElementText(energyText, formatEnergy(data.energyCurrent()) + " / " + formatEnergy(data.energyMax()) + " FE");
        }

        // 护盾条
        var shieldFill = doc.getElementById("shield-bar-fill");
        var shieldText = doc.getElementById("shield-text");
        if (shieldFill != null) {
            long cur = data.shieldCapacity();
            long max = data.shieldMax();
            double pct = max > 0 ? Math.min(100.0, (double) cur / max * 100.0) : 0.0;
            shieldFill.setAttribute("style", String.format("width: %.1f%%;", pct));
        }
        if (shieldText != null) {
            setElementText(shieldText, formatEnergy(data.shieldCapacity()) + " / " + formatEnergy(data.shieldMax()));
        }

        // 状态行（<span> 元素，不会有子span问题，但统一用 setElementText）
        setElementText(doc.getElementById("val-harm"), String.valueOf(data.harmLevel()));
        setElementText(doc.getElementById("val-efficiency"), String.valueOf(data.energyExpendLevel()));
        setElementText(doc.getElementById("val-shield"), data.shieldActive() ? "Active" : "Inactive");
        setElementText(doc.getElementById("val-core"), data.fortressCore());

        return true;
    }

    /**
     * 设置元素文本。处理 ApricityUI 在 init 时将 &lt;div&gt; 内文本移到子 &lt;span&gt; 的行为。
     * 对 &lt;div&gt; 元素：更新子 &lt;span&gt; 的 innerText（渲染实际走的是子节点文本）。
     * 对 &lt;span&gt; 等无子节点的元素：直接更新 innerText。
     */
    private static void setElementText(Element el, String text) {
        if (el == null) return;
        // ApricityUI init 时会将 <div> 的文本内容移到第一个子 <span> 中
        // 因此实际渲染文本在子 <span> 上，需更新子节点
        if (!el.children.isEmpty()) {
            el.children.get(0).innerText = text;
        } else {
            el.innerText = text;
        }
    }

    private static String formatEnergy(long value) {
        if (value >= 1_000_000) return String.format("%.1fM", value / 1_000_000.0);
        if (value >= 1_000) return String.format("%.1fK", value / 1_000.0);
        return String.valueOf(value);
    }

    /**
     * 持有待应用的数据，当 Document 还没准备好时暂存于此。
     * 由客户端 tick 处理器在下一帧尝试应用。
     */
    public static class PendingDataHolder {
        static DefendCoreData pending;

        public static void tick() {
            if (pending == null) return;
            if (applyToDocument(pending)) {
                pending = null;
            }
        }
    }
}