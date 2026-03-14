package org.lingZero.modularization_defend;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
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
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.lingZero.modularization_defend.Data.ModBlockTagsProvider;
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
    
    /**
     * 监听玩家右键点击方块事件 - 在放置前检测多方块结构
     */
    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        // 只处理服务器端
        if (event.getLevel().isClientSide()) {
            return;
        }
            
        // 只处理玩家手持 ElectricityRepeater 的情况
        var itemStack = event.getItemStack();
        if (itemStack.getItem() == ModBlocks.ELECTRICITY_REPEATER_BLOCK.get().asItem()) {
            BlockPos pos = event.getPos();
            net.minecraft.world.level.Level level = (net.minecraft.world.level.Level) event.getLevel();
            net.minecraft.world.entity.player.Player player = event.getEntity();
                
            // 获取点击的面
            net.minecraft.core.Direction face = event.getFace();
            if (face == null) {
                return;
            }
                
            // 计算实际放置位置（在点击方块的旁边）
            BlockPos controllerPos = pos.relative(face);
                
            // 检查以放置位置为底座的 2x2x6 区域是否有阻挡
            if (!canFormMultiblock(level, controllerPos)) {
                // 有阻挡，阻止放置并显示提示
                // 完全取消事件，原版不会消耗物品
                event.setCanceled(true);
                event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
                        
                String message = "§c无法放置：结构区域内有其他方块阻挡！";
                player.displayClientMessage(net.minecraft.network.chat.Component.literal(message), true);
                    
                // 重要：手动同步物品栏到客户端
                player.containerMenu.sendAllDataToRemote();
            } else {
                // 检测通过！取消原版放置逻辑，手动一次性放置所有方块
                event.setCanceled(true);
                event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
                    
                // 消耗一个物品
                if (!player.isCreative()) {
                    itemStack.shrink(1);
                }
                    
                // 一次性放置 2x2x6 的所有方块（包括主方块）
                placeEntireMultiblock(level, controllerPos);
            }
        }
    }
    
    /**
     * 一次性放置整个 2x2x6 多方块结构
     */
    private void placeEntireMultiblock(net.minecraft.world.level.Level level, BlockPos controllerPos) {
        // 遍历 2x2x6 的所有位置
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 2; x++) {
                for (int z = 0; z < 2; z++) {
                    BlockPos checkPos = controllerPos.offset(x, y, z);
                    
                    // 放置方块（使用 UPDATE_ALL 确保客户端同步）
                    BlockState stateToPlace = ModBlocks.ELECTRICITY_REPEATER_BLOCK.get().defaultBlockState();
                    level.setBlock(checkPos, stateToPlace, net.minecraft.world.level.block.Block.UPDATE_ALL);
                    
                    // 设置控制器标志
                    net.minecraft.world.level.block.entity.BlockEntity blockEntity = level.getBlockEntity(checkPos);
                    if (blockEntity instanceof org.lingZero.modularization_defend.Blocks.BlockEntity.ElectricityRepeaterBlockEntity repeater) {
                        // 第一个位置是控制器，其余不是
                        repeater.setController(x == 0 && y == 0 && z == 0);
                    }
                }
            }
        }
        
        // 通知控制器更新多方块数据
        net.minecraft.world.level.block.entity.BlockEntity controllerBE = level.getBlockEntity(controllerPos);
        if (controllerBE instanceof org.lingZero.modularization_defend.Blocks.BlockEntity.ElectricityRepeaterBlockEntity repeater) {
            // 初始化多方块数据并验证
            repeater.initializeMultiblock();
        }
    }
    
    /**
     * 检查指定位置是否可以形成 2x2x6 多方块结构
     */
    private boolean canFormMultiblock(net.minecraft.world.level.LevelAccessor level, BlockPos controllerPos) {
        // 检查 2x2x6 的所有位置
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 2; x++) {
                for (int z = 0; z < 2; z++) {
                    BlockPos checkPos = controllerPos.offset(x, y, z);
                        
                    // 检查该位置是否为空或可替换
                    if (!level.isEmptyBlock(checkPos)) {
                        net.minecraft.world.level.block.state.BlockState state = level.getBlockState(checkPos);
                        if (!state.canBeReplaced()) {
                            return false; // 有阻挡方块
                        }
                    }
                }
            }
        }
        return true; // 没有阻挡
    }
        
    // 你可以使用 EventBusSubscriber 自动注册此类中所有用 @SubscribeEvent 注解的静态方法
    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }
}
