package org.lingZero.modularization_defend;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
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
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.lingZero.modularization_defend.Data.ModBlockTagsProvider;
import org.lingZero.modularization_defend.Register.ModBlockEntities;
import org.lingZero.modularization_defend.Register.ModBlocks;
import org.lingZero.modularization_defend.Register.ModCreativeTabs;
import org.lingZero.modularization_defend.Register.ModItems;
import org.lingZero.modularization_defend.Renderer.ElectricityRepeaterRenderer;
import org.slf4j.Logger;

@Mod(ModularizationDefend.MODID)
public class ModularizationDefend {
    public static final String MODID = "modularization_defend";
    private static final Logger LOGGER = LogUtils.getLogger();

    // 模组类的构造函数是模组加载时运行的第一段代码。
    // FML 会自动识别某些参数类型（如 IEventBus 或 ModContainer）并自动传入它们
    public ModularizationDefend(IEventBus modEventBus, ModContainer modContainer) {
        // 注册通用设置方法以供模组加载时调用
        modEventBus.addListener(this::commonSetup);
        
        // 注册数据生成器
        modEventBus.addListener(this::gatherData);

        // 注册延迟注册表
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);

        // 将我们自己注册到服务器和其他我们感兴趣的游戏事件中。
        // 注意，只有当我们希望*这个*类（modularization_defend）直接响应事件时才有必要这样做。
        // 如果此类中没有 @SubscribeEvent 注解的方法（如下面的 onServerStarting()），则不要添加此行。
        NeoForge.EVENT_BUS.register(this);

        // 注册ModConfigSpec
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
    private void commonSetup(final FMLCommonSetupEvent event) {
        // 一些通用设置代码
        LOGGER.info("Max connection distance: {}", Config.maxConnectionDistance);
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
            
    }
    
    // 你可以使用 EventBusSubscriber 自动注册此类中所有用 @SubscribeEvent 注解的静态方法
    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
        
        /**
         * 注册方块实体渲染器
         */
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            // 注册电力中继器渲染器
            event.registerBlockEntityRenderer(ModBlockEntities.Electricity_Repeater_BLOCK_ENTITY.get(), ElectricityRepeaterRenderer::new);
        }
    }
}
