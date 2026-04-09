package org.lingZero.m_defend;

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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.lingZero.m_defend.Blocks.MultiblockFrame.AffiliateBlock;
import org.lingZero.m_defend.Data.ModBlockTagsProvider;
import org.lingZero.m_defend.Items.TargetFilter.EntityIdFilter;
import org.lingZero.m_defend.Register.*;
import org.lingZero.m_defend.util.DebugCommand;
import org.lingZero.m_defend.util.DebugLogger;
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
        
        // 注册能力（Capability）
        modEventBus.addListener(this::registerCapabilities);
        
        // 注册数据生成器
        modEventBus.addListener(this::gatherData);

        // 注册延迟注册表
        ModDataComponents.REGISTRAR.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        // 注册按键绑定（仅客户端）
        ModKeyBindings.register(modEventBus);
            
        DebugLogger.info("所有注册表完成");
    
        // 将我们自己注册到服务器和其他我们感兴趣的游戏事件中。
        // 注意，只有当我们希望*这个*类（modularization_defend）直接响应事件时才有必要这样做。
        // 如果此类中没有 @SubscribeEvent 注解的方法（如下面的 onServerStarting()），则不要添加此行。
        NeoForge.EVENT_BUS.register(this);
                
        // 注册调试命令
        NeoForge.EVENT_BUS.addListener(DebugCommand::register);
        
        // 注册实体ID过滤器事件处理器
        NeoForge.EVENT_BUS.register(EntityIdFilter.class);

        // 注册 ModConfigSpec
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        DebugLogger.info("配置注册完成");
        DebugLogger.info("===== ModularizationDefend 初始化完成 =====");
    }
    private void commonSetup(final FMLCommonSetupEvent event) {
        // 一些通用设置代码
    }
    
    /**
     * 注册方块实体的能力（Capability）
     * 使炮塔能够与其他 MOD 的物流系统交互
     */
    private void registerCapabilities(final RegisterCapabilitiesEvent event) {
        // 为所有继承 BaseTurretBlockEntity 的方块实体注册物品处理器能力
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.TURRET1_BLOCK_ENTITY.get(),
            (blockEntity, context) -> blockEntity.getCapability(Capabilities.ItemHandler.BLOCK, context)
        );
        
        // 为附属方块注册物品处理器能力（重定向到主方块）
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.AFFILIATE_BLOCK_ENTITY.get(),
            (blockEntity, context) -> blockEntity.getCapability(Capabilities.ItemHandler.BLOCK, context)
        );
        
        DebugLogger.info("已注册炮塔方块的 ItemHandler 能力");
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
            // 注册 DefendCore GUI Screen
            event.register(
                ModMenuTypes.DEFEND_CORE_MENU.get(),
                org.lingZero.m_defend.ldlibUI.NewUIScreen::new
            );
        }
        
        /**
         * 注册方块实体渲染器
         */
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            // 注册简化激光子弹实体渲染器
            event.registerEntityRenderer(
                ModEntities.SIMPLE_LASER_PROJECTILE.get(),
                org.lingZero.m_defend.Client.Render.SimpleLaserProjectileRenderer::new
            );
        }
        
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // 客户端初始化逻辑
            AffiliateBlock.initClient();
        }
    }
}
