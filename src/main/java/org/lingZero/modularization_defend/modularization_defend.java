package org.lingZero.modularization_defend;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.lingZero.modularization_defend.Block.ModBlockEntities;
import org.lingZero.modularization_defend.Block.ModBlocks;
import org.lingZero.modularization_defend.Block.render.bluedoor.BlueDoorRenderer;
import org.lingZero.modularization_defend.Block.render.reddoor.RedDoorRenderer;
import org.lingZero.modularization_defend.Block.render.LevelEditorRenderer;
import org.lingZero.modularization_defend.CreativeTab.ModCreativeTabs;
import org.lingZero.modularization_defend.DataComponents.ModDataComponents;
import org.lingZero.modularization_defend.Event.EntitySelectorHandler;
import org.lingZero.modularization_defend.Item.ModItems;
import org.lingZero.modularization_defend.nodegraph.NodeGraphCommand;
import org.lingZero.modularization_defend.nodegraph.eval.NodeEvaluators;
import org.lingZero.modularization_defend.nodegraph.exec.LevelNodeExecutors;
import org.lingZero.modularization_defend.nodegraph.network.OpenGraphEditorPacket;
import org.lingZero.modularization_defend.nodegraph.network.SaveCardGraphPacket;
import org.lingZero.modularization_defend.nodegraph.network.SaveControllerGraphPacket;
import org.lingZero.modularization_defend.trait.ModTraits;
import org.lingZero.modularization_defend.trait.TraitCommand;
import org.lingZero.modularization_defend.util.DebugDumpComponentsCommand;
import org.slf4j.Logger;

// 这里的 MODID 值需要与 META-INF/neoforge.mods.toml 中的条目一致
@Mod(modularization_defend.MODID)
public class modularization_defend {
    public static final String MODID = "modularization_defend";
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * 模组构造器——模组加载时最先执行的代码。
     * FML 会自动识别 IEventBus、ModContainer 等参数类型并注入。
     */
    public modularization_defend(IEventBus modEventBus, ModContainer modContainer) {
        // 注册通用设置（commonSetup）到模组事件总线
        modEventBus.addListener(this::commonSetup);

        // 分别向模组事件总线注册方块、物品、BlockEntity、数据组件和创造模式标签的延迟注册表
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        // 创建自定义词条注册表并注册词条延迟注册表
        modEventBus.addListener(ModTraits::onNewRegistry);
        ModTraits.TRAITS.register(modEventBus);
        ModTraits.ATTACHMENT_TYPES.register(modEventBus);

        // 将本类和 EntitySelectorHandler 注册到 NeoForge 事件总线
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(EntitySelectorHandler.class);

        // 注册创造模式标签内容填充事件
        modEventBus.addListener(ModCreativeTabs::addCreative);

        // 注册模组配置文件
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // 注册网络数据包处理器
        modEventBus.addListener(RegisterPayloadHandlersEvent.class, event -> {
            PayloadRegistrar registrar = event.registrar("1");
            registrar.playToClient(
                    OpenGraphEditorPacket.TYPE,
                    OpenGraphEditorPacket.STREAM_CODEC,
                    OpenGraphEditorPacket::handleClient
            );
            registrar.playToServer(
                    SaveCardGraphPacket.TYPE,
                    SaveCardGraphPacket.STREAM_CODEC,
                    SaveCardGraphPacket::handleServer
            );
            registrar.playToServer(
                    SaveControllerGraphPacket.TYPE,
                    SaveControllerGraphPacket.STREAM_CODEC,
                    SaveControllerGraphPacket::handleServer
            );
        });

        // 注册节点图求值器与关卡执行流节点
        NodeEvaluators.init();
        LevelNodeExecutors.init();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        DebugDumpComponentsCommand.register(event.getDispatcher());
        TraitCommand.register(event.getDispatcher());
        NodeGraphCommand.register(event.getDispatcher());
    }

    // 自动注册内部所有 @SubscribeEvent 静态方法（仅客户端侧）
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }

        /**
         * 注册本模组的BlockEntity渲染器。
         * 使用GeckoLib的GeoBlockRenderer渲染门的动画模型。
         */
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(
                    ModBlockEntities.BLUE_DOOR.get(),
                    context -> new BlueDoorRenderer()
            );
            event.registerBlockEntityRenderer(
                    ModBlockEntities.RED_DOOR.get(),
                    context -> new RedDoorRenderer()
            );
            event.registerBlockEntityRenderer(
                    ModBlockEntities.LEVEL_EDITOR.get(),
                    context -> new LevelEditorRenderer(context)
            );
        }
    }
}
