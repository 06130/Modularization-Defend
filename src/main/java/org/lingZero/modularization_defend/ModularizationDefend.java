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
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.lingZero.modularization_defend.Register.ModBlockEntities;
import org.lingZero.modularization_defend.Register.ModBlocks;
import org.lingZero.modularization_defend.Register.ModCreativeTabs;
import org.lingZero.modularization_defend.Register.ModItems;
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

        // 按正确顺序注册延迟注册表
        // 1. 先注册 Blocks（因为其他注册表可能依赖它）
        ModBlocks.BLOCKS.register(modEventBus);
        
        // 2. 注册 Items
        ModItems.ITEMS.register(modEventBus);
        
        // 3. 注册 BlockEntities（可能依赖 Blocks）
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        
        // 4. 注册 CreativeTabs（依赖 Items 和 Blocks）
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);

        // 将我们自己注册到服务器和其他我们感兴趣的游戏事件中。
        // 注意，只有当我们希望*这个*类（modularization_defend）直接响应事件时才有必要这样做。
        // 如果此类中没有 @SubscribeEvent 注解的方法（如下面的 onServerStarting()），则不要添加此行。
        NeoForge.EVENT_BUS.register(this);

        // 注册我们的模组的 ModConfigSpec，以便 FML 可以为我们创建和加载配置文件
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
    private void commonSetup(final FMLCommonSetupEvent event) {
        // 一些通用设置代码
        LOGGER.info("HELLO FROM COMMON SETUP");
        if (Config.logDirtBlock) LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);
        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }
    // 你可以使用 SubscribeEvent 让事件总线发现要调用的方法
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // 服务器启动时执行某些操作
        LOGGER.info("HELLO from server starting");
    }
    // 你可以使用 EventBusSubscriber 自动注册此类中所有用 @SubscribeEvent 注解的静态方法
    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // 一些客户端设置代码
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
