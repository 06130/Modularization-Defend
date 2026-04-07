package org.lingZero.modularization_defend.Register;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.lingZero.modularization_defend.Blocks.Multiblock.AgreementCore.AgreementCoreMultiblockDef;
import org.lingZero.modularization_defend.Blocks.Multiblock.newMultiblock.IMultiblockStructure;
import org.lingZero.modularization_defend.ModularizationDefend;
import org.lingZero.modularization_defend.util.DebugLogger;

import java.util.List;

/**
 * 多方块结构注册表
 * 
 * 设计目标：
 * 1. 使用 NeoForge 的 DeferredRegister 机制注册多方块结构
 * 2. 避免在放置时重复解析结构（通过缓存机制）
 * 3. 与 AbstractMultiblock 框架无缝集成
 * <p>
 * 架构说明：
 * - 每个 AbstractMultiblock 子类（如 AgreementCoreMultiblockDef）定义自己的结构
 * - 结构定义在首次访问时自动缓存到 BlockEntity 中
 * - 本注册表提供全局查询接口，支持通过资源位置获取结构
 * <p>
 * 使用方法：
 * 1. 创建新的多方块类继承 AbstractMultiblock
 * 2. 在 createStructure() 方法中定义结构
 * 3. 在本类中添加注册条目（参考 AGREEMENT_CORE_STRUCTURE）
 * 4. 结构会自动缓存并可通过 getStructure() 方法获取
 */
public class ModMultiblockStructures {
    
    /**
     * 创建自定义注册表的 ResourceKey
     * 注意：这只是一个逻辑上的注册表，实际存储使用 Map
     * 作用：为多方块结构提供唯一的命名空间标识
     */
    private static final ResourceKey<Registry<IMultiblockStructure>> MULTIBLOCK_STRUCTURE_REGISTRY_KEY = 
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(ModularizationDefend.MODID, "multiblock_structures"));
    
    /**
     * 多方块结构延迟注册表
     * 使用 NeoForge 的 DeferredRegister 机制
     * 特点：
     * - 只在首次访问时才解析结构（懒加载）
     * - 解析结果会被缓存，后续访问直接使用缓存
     * - 线程安全，支持并发访问
     */
    public static final DeferredRegister<IMultiblockStructure> MULTIBLOCK_STRUCTURES = 
            DeferredRegister.create(MULTIBLOCK_STRUCTURE_REGISTRY_KEY, ModularizationDefend.MODID);
    
    /**
     * 注册协议核心多方块结构
     * <p>
     * 技术要点：
     * 1. 使用 Lambda 表达式 () -> AgreementCoreMultiblockDef.createStructureInstance()
     *    - 直接调用静态方法创建结构实例
     *    - 在注册时立即解析（非懒加载）
     * 2. 避免重复定义结构
     *    - 结构定义已经在 AgreementCoreMultiblockDef.createStructureInstance() 中
     *    - 这里只是引用已定义的结构，不重复创建
     */
    public static final DeferredHolder<IMultiblockStructure, IMultiblockStructure> AGREEMENT_CORE_STRUCTURE = 
            MULTIBLOCK_STRUCTURES.register("agreement_core", () -> {
                // 立即解析并创建结构实例
                IMultiblockStructure structure = AgreementCoreMultiblockDef.createStructureInstance();
                DebugLogger.info("多方块结构已解析并注册：agreement_core");
                return structure;
            });
    
    /**
     * 注册到模组事件总线
     * 在主模类的构造函数中调用
     * <p>
     * 调用时机：
     * - 模组初始化阶段（FMLCommonSetupEvent 之前）
     * - 所有注册表必须在游戏加载早期完成注册
     * <p>
     * 内部流程：
     * 1. DeferredRegister 监听 RegisterEvent 事件
     * 2. 在事件触发时立即解析所有 Supplier 并创建结构实例
     * 3. 创建后的实例会被缓存，不会重复创建
     * 4. RegisterEvent 结束后，注册表被"冻结"，不允许再添加新条目
     * <p>
     * NeoForge 冻结机制：
     * - 参考 DeferredRegister.addEntries() 方法（位于 .lib/NeoForge-1.21.1/src/main/java/net/neoforged/neoforge/registries/DeferredRegister.java）
     * - seenRegisterEvent 标志位在 RegisterEvent 后设为 true
     * - 此后调用 register() 会抛出 IllegalStateException
     * - 确保注册表内容不可变，线程安全
     *
     * @param modEventBus 模组事件总线，由 FML 自动注入
     */
    public static void register(IEventBus modEventBus) {
        // 注册事件监听器，在注册完成后输出调试信息
        modEventBus.addListener((RegisterEvent event) -> {
            if (event.getRegistryKey().equals(MULTIBLOCK_STRUCTURE_REGISTRY_KEY)) {
                // 注册完成后，输出所有已注册的多方块结构信息
                DebugLogger.info("===== 多方块结构注册表已冻结 =====");
                DebugLogger.info("已注册的多方块结构数量：" + MULTIBLOCK_STRUCTURES.getEntries().size());
                        
                for (var holder : MULTIBLOCK_STRUCTURES.getEntries()) {
                    ResourceLocation id = holder.getId();
                    IMultiblockStructure structure = holder.get();
                    DebugLogger.info("  - ID: " + id);
                    DebugLogger.info("    结构名称：" + (structure != null ? structure.getName() : "null"));
                    DebugLogger.info("    主方块位置：" + (structure != null ? structure.getMasterPosition() : "null"));
                    DebugLogger.info("    总层数：" + (structure != null ? structure.getLayers().size() : "null"));
                            
                    // 输出每层的尺寸信息
                    if (structure != null && structure.getLayers() != null) {
                        int layerCount = 0;
                        for (var layerEntry : structure.getLayers().entrySet()) {
                            List<String> rows = layerEntry.getValue();
                            DebugLogger.info("      第 " + layerCount++ + " 层 (y=" + layerEntry.getKey() + "): " + rows.size() + " 行，每行宽度：" + rows.stream().map(String::length).toList());
                        }
                    }
                }
                DebugLogger.info("===================================");
            }
        });
                
        // 注册注册表本身
        MULTIBLOCK_STRUCTURES.register(modEventBus);
    }
    
    /**
     * 通过资源位置获取多方块结构
     * <p>
     * 工作原理：
     * 1. 遍历所有已注册的结构持有者（DeferredHolder）
     * 2. 匹配资源位置 ID
     * 3. 返回匹配的结构实例（从缓存中获取）
     * <p>
     * 性能特点：
     * - 结构在注册时已立即解析并缓存（非懒加载）
     * - 所有访问直接从缓存返回，时间复杂度 O(1)
     * - 遍历查找的时间复杂度 O(n)，n 为注册的结构数量
     * <p>
     * 线程安全性：
     * - 注册完成后注册表会被冻结（NeoForge 机制）
     * - RegisterEvent 触发后不允许再添加新条目
     * - 确保结构定义不可变，支持并发访问
     *
     * @param location 资源位置（格式：“命名空间：路径”）
     *                 例如："modularization_defend:agreement_core"
     * @return 多方块结构实例，如果不存在则返回 null
     * <p>
     * @see #getStructure(String) 简化版本，自动添加命名空间
     */
    public static IMultiblockStructure getStructure(ResourceLocation location) {
        for (var holder : MULTIBLOCK_STRUCTURES.getEntries()) {
            if (holder.getId().equals(location)) {
                return holder.get();  // 从缓存中获取
            }
        }
        return null;
    }
    
    /**
     * 通过命名空间路径获取多方块结构（简化版本）
     * <p>
     * 便捷方法：
     * - 自动添加模组命名空间前缀
     * - 例如："agreement_core" → "modularization_defend:agreement_core"
     * <p>
     * 使用场景：
     * - 在代码中快速获取已知 ID 的结构
     * - 不需要完整的资源位置字符串时
     * 
     * @param path 路径（自动添加命名空间）
     *             例如："agreement_core"
     * @return 多方块结构实例，如果不存在则返回 null
     * 
     * @see #getStructure(ResourceLocation) 完整版本，支持自定义命名空间
     */
    public static IMultiblockStructure getStructure(String path) {
        return getStructure(ResourceLocation.fromNamespaceAndPath(ModularizationDefend.MODID, path));
    }
}
