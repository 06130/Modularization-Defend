package org.lingZero.modularization_defend;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
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
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

// 这里的值应该与 META-INF/neoforge.mods.toml 文件中的条目匹配
@Mod(modularization_defend.MODID)
public class modularization_defend {
    public static final String MODID = "modularization_defend";
    // 直接引用 slf4j 日志器
    private static final Logger LOGGER = LogUtils.getLogger();
    // 创建一个延迟注册表来持有方块，所有方块都将在 "modularization_defend" 命名空间下注册
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // 创建一个延迟注册表来持有物品，所有物品都将在 "modularization_defend" 命名空间下注册
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // 创建一个延迟注册表来持有创造模式标签，所有标签都将在 "modularization_defend" 命名空间下注册
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // 创建一个新方块，ID 为 "modularization_defend:example_block"，结合命名空间和路径
    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
    // 创建一个新方块物品，ID 为 "modularization_defend:example_block"，结合命名空间和路径
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);
    // 创建一个创造模式标签，ID 为 "modularization_defend:example_tab"，用于示例物品，放置在战斗标签之后
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.modularization_defend")).withTabsBefore(CreativeModeTabs.COMBAT).icon(() -> EXAMPLE_BLOCK_ITEM.get().getDefaultInstance()).displayItems((parameters, output) -> {
        output.accept(EXAMPLE_BLOCK_ITEM.get()); // 将示例物品添加到标签中。对于你自己的标签，这种方法比事件更受青睐
    }).build());

    // 模组类的构造函数是模组加载时运行的第一段代码。
    // FML 会自动识别某些参数类型（如 IEventBus 或 ModContainer）并自动传入它们
    public modularization_defend(IEventBus modEventBus, ModContainer modContainer) {
        // 注册通用设置方法以供模组加载时调用
        modEventBus.addListener(this::commonSetup);

        // 将延迟注册表注册到模组事件总线，这样方块就会被注册
        BLOCKS.register(modEventBus);
        // 将延迟注册表注册到模组事件总线，这样物品就会被注册
        ITEMS.register(modEventBus);
        // 将延迟注册表注册到模组事件总线，这样标签就会被注册
        CREATIVE_MODE_TABS.register(modEventBus);

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

    // 将示例方块物品添加到建筑方块标签

    // 你可以使用 SubscribeEvent 让事件总线发现要调用的方法
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // 服务器启动时执行某些操作
        LOGGER.info("HELLO from server starting");
    }

    // 你可以使用 EventBusSubscriber 自动注册此类中所有用 @SubscribeEvent 注解的静态方法
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // 一些客户端设置代码
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
