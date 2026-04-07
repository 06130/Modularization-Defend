package org.lingZero.modularization_defend.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;
import org.lingZero.modularization_defend.ModularizationDefend;
import org.lingZero.modularization_defend.ldlibUI.DefendCoreContainerMenu;
import org.lingZero.modularization_defend.util.DebugLogger;

/**
 * 客户端请求打开 DefendCore GUI 的网络消息
 */
public record OpenDefendCoreGUIMessage() implements CustomPacketPayload {
    
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
     * 在服务端处理该消息，打开 GUI
     */
    public static void handle(OpenDefendCoreGUIMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            DebugLogger.info("收到打开 DefendCore GUI 的请求，玩家: " + player.getName().getString());
            
            // 在服务端打开容器菜单
            player.openMenu(new MenuProvider() {
                @Override
                public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                    return new DefendCoreContainerMenu(containerId, playerInventory);
                }
                
                @Override
                public net.minecraft.network.chat.Component getDisplayName() {
                    return net.minecraft.network.chat.Component.translatable("gui.modularization_defend.defend_core");
                }
            });
        });
    }
}
