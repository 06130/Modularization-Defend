package org.lingZero.modularization_defend;

import com.mojang.logging.LogUtils;
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
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.lingZero.modularization_defend.Blocks.Multiblock.AgreementCore.AgreementCoreMultiblockDef;
import org.lingZero.modularization_defend.Blocks.Multiblock.AgreementCore.AgreementCoreScreen;
import org.lingZero.modularization_defend.Blocks.Multiblock.ElectricityRepeater.ElectricityRepeaterScreen;
import org.lingZero.modularization_defend.Data.ModBlockTagsProvider;
import org.lingZero.modularization_defend.Event.MultiblockEvents;
import org.lingZero.modularization_defend.GeoModel.Renderer.AgreementCoreRenderer;
import org.lingZero.modularization_defend.GeoModel.Renderer.ElectricityRepeaterRenderer;
import org.lingZero.modularization_defend.Register.*;
import org.lingZero.modularization_defend.util.DebugCommand;
import org.lingZero.modularization_defend.util.DebugLogger;
import org.slf4j.Logger;

@Mod(ModularizationDefend.MODID)
public class ModularizationDefend {
    public static final String MODID = "modularization_defend";
    public static final String MODVERSION = "§cA-0.1.2";
    private static final Logger LOGGER = LogUtils.getLogger();

    // 模组类的构造函数是模组加载时运行的第一段代码。
    // FML 会自动识别某些参数类型（如 IEventBus 或 ModContainer）并自动传入它们
    public ModularizationDefend(IEventBus modEventBus, ModContainer modContainer) {
        // 初始化调试日志系统
        DebugLogger.init();
        DebugLogger.info("===== ModularizationDefend 初始化日志系统 =====");
        
        // 注册通用设置方法以供模组加载时调用
        modEventBus.addListener(this::commonSetup);
        
        // 注册数据生成器
        modEventBus.addListener(this::gatherData);

        // 注册延迟注册表
        ModDataComponents.REGISTRAR.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        // 注册按键绑定（仅客户端）
        ModKeyBindings.register(modEventBus);
        // 注册多方块结构注册表（使用 DeferredRegister 机制，避免重复解析）
        ModMultiblockStructures.register(modEventBus);
            
        DebugLogger.info("所有注册表完成");
    
        // 将我们自己注册到服务器和其他我们感兴趣的游戏事件中。
        // 注意，只有当我们希望*这个*类（modularization_defend）直接响应事件时才有必要这样做。
        // 如果此类中没有 @SubscribeEvent 注解的方法（如下面的 onServerStarting()），则不要添加此行。
        NeoForge.EVENT_BUS.register(this);
                
        // 注册调试命令
        NeoForge.EVENT_BUS.addListener(DebugCommand::register);
        
        // 注册多方块到事件处理器
        MultiblockEvents.registerMultiblock(AgreementCoreMultiblockDef.getInstance());
        DebugLogger.info("多方块事件注册完成");
    
        // 注册 ModConfigSpec
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        DebugLogger.info("配置注册完成");
        DebugLogger.info("===== ModularizationDefend 初始化完成 =====");
    }
    private void commonSetup(final FMLCommonSetupEvent event) {
        // 一些通用设置代码
    }
    
    private void gatherData(final GatherDataEvent event) {
        var gen = event.getGenerator();
        var packOutput = gen.getPackOutput();
        var lookupProvider = event.getLookupProvider();
        var existingFileHelper = event.getExistingFileHelper();
        
        // 注册方块标签数据生成器
        gen.addProvider(event.includeServer(), new ModBlockTagsProvider(packOutput, lookupProvider, existingFileHelper));
    }
    
    // 你可以使用 SubscribeEvent 让事件总线发现要调用的方法
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // 服务器启动时的初始化逻辑
        // 注意：新框架不需要 MultiblockManager，结构数据由 BlockEntity 自己管理
    }
    
    // 你可以使用 EventBusSubscriber 自动注册此类中所有用 @SubscribeEvent 注解的静态方法
    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        /**
         * 注册 GUI Screen
         */
        @SubscribeEvent
        public static void registerMenuScreens(RegisterMenuScreensEvent event) {
            event.register(
                ModMenuTypes.ELECTRICITY_REPEATER_MENU.get(),
                ElectricityRepeaterScreen::new
            );
            // 注册协议核心 GUI Screen
            event.register(
                ModMenuTypes.AGREEMENT_CORE_MENU.get(),
                AgreementCoreScreen::new
            );
            // 注册 DefendCore GUI Screen
            event.register(
                ModMenuTypes.DEFEND_CORE_MENU.get(),
                org.lingZero.modularization_defend.ldlibUI.NewUIScreen::new
            );
        }
        
        /**
         * 注册方块实体渲染器
         */
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            // 注册电力中继器渲染器
            event.registerBlockEntityRenderer(ModBlockEntities.Electricity_Repeater_BLOCK_ENTITY.get(), ElectricityRepeaterRenderer::new);
            // 注册协议核心渲染器
            event.registerBlockEntityRenderer(ModBlockEntities.AGREEMENT_CORE_BLOCK_ENTITY.get(), AgreementCoreRenderer::new);
        }
        
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // 客户端初始化逻辑
        }
    }
}
