package org.lingZero.modularization_defend.nodegraph.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.lingZero.modularization_defend.Item.ProgramStorageCardItem;
import org.lingZero.modularization_defend.modularization_defend;

import javax.annotation.Nullable;

/**
 * 客户端→服务端数据包：将编辑后的图数据保存回程序存储卡。
 */
public record SaveCardGraphPacket(@Nullable CompoundTag graphData, boolean mainHand) implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(modularization_defend.MODID, "save_card_graph");

    public static final Type<SaveCardGraphPacket> TYPE = new Type<>(ID);

    /** CompoundTag 的手动编解码器（避免依赖不存在的 ByteBufCodecs.COMPOUND_TAG） */
    private static final StreamCodec<RegistryFriendlyByteBuf, CompoundTag> TAG_CODEC = StreamCodec.of(
            (buf, tag) -> buf.writeNbt(tag),
            buf -> buf.readNbt()
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SaveCardGraphPacket> STREAM_CODEC =
            StreamCodec.composite(
                    TAG_CODEC, SaveCardGraphPacket::graphData,
                    ByteBufCodecs.BOOL, SaveCardGraphPacket::mainHand,
                    SaveCardGraphPacket::new
            );

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** 服务端处理器：将图数据写入玩家手中卡片的 CUSTOM_DATA 组件 */
    public static void handleServer(SaveCardGraphPacket payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (payload.graphData() == null) return;
            Player player = ctx.player();
            InteractionHand hand = payload.mainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof ProgramStorageCardItem) {
                ProgramStorageCardItem.setGraphTag(stack, payload.graphData());
            }
        });
    }
}
