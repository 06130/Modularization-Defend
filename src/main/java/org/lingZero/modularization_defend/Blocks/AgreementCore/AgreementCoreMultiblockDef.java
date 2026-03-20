package org.lingZero.modularization_defend.Blocks.AgreementCore;

import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.lingZero.modularization_defend.Blocks.Multiblock.newMultiblock.AbstractMultiblock;
import org.lingZero.modularization_defend.Blocks.Multiblock.newMultiblock.IMultiblockStructure;
import org.lingZero.modularization_defend.Blocks.Multiblock.newMultiblock.MultiblockStructure;
import org.lingZero.modularization_defend.Register.ModBlocks;

/**
 * 协议核心多方块定义
 * 使用新的 Multiblock 框架实现
 * 结构：5x5x1 底座 + 3x3x12 柱体，总高度 13
 */
public class AgreementCoreMultiblockDef extends AbstractMultiblock {
    
    private static final AgreementCoreMultiblockDef INSTANCE = new AgreementCoreMultiblockDef();
    
    public AgreementCoreMultiblockDef() {
        super("agreement_core");
    }
    
    /**
     * 获取单例实例
     */
    @NotNull
    public static AgreementCoreMultiblockDef getInstance() {
        return INSTANCE;
    }
    
    /**
     * 创建并返回多方块结构（静态方法，用于注册表）
     * @return 解析完成的多方块结构
     */
    @NotNull
    public static IMultiblockStructure createStructureInstance() {
        // 创建 5x5x1 底座 + 3x3x12 柱体的结构
        return MultiblockStructure.start("AgreementCore")
                // 第 1 层（底座）- 5x5
                .aisle(
                    "BBBBB",
                    "BBBBB",
                    "BBYBB",
                    "BBBBB",
                    "BBBBB"
                )
                // 第 2-12 层 - 3x3 柱体（注意：每行都必须是 5 个字符，使用空格填充外围）
                // 这些层只需要中央 3x3 区域有方块，外围用空格填充
                .aisle(
                    "     ",
                    " BBB ",
                    " BBB ",
                    " BBB ",
                    "     "
                )
                .aisle(
                    "     ",
                    " BBB ",
                    " BBB ",
                    " BBB ",
                    "     "
                )
                .aisle(
                    "     ",
                    " BBB ",
                    " BBB ",
                    " BBB ",
                    "     "
                )
                .aisle(
                    "     ",
                    " BBB ",
                    " BBB ",
                    " BBB ",
                    "     "
                )
                .aisle(
                    "     ",
                    " BBB ",
                    " BBB ",
                    " BBB ",
                    "     "
                )
                .aisle(
                    "     ",
                    " BBB ",
                    " BBB ",
                    " BBB ",
                    "     "
                )
                .aisle(
                    "     ",
                    " BBB ",
                    " BBB ",
                    " BBB ",
                    "     "
                )
                .aisle(
                    "     ",
                    " BBB ",
                    " BBB ",
                    " BBB ",
                    "     "
                )
                .aisle(
                    "     ",
                    " BBB ",
                    " BBB ",
                    " BBB ",
                    "     "
                )
                .aisle(
                    "     ",
                    " BBB ",
                    " BBB ",
                    " BBB ",
                    "     "
                )
                // 第 13 层（顶部）- 3x3
                .aisle(
                    "     ",
                    " BBB ",
                    " BBB ",
                    " BBB ",
                    "     "
                )
                // 定义字符对应的方块
                .where('Y', getMasterBlockStatic())  // Y 代表主方块（控制器）
                .where('B', getPartBlockStatic())    // B 代表其他结构方块
                // 设置主方块位置（底座中央）
                .setMaster('Y')
                .build();
    }
    
    @Override
    @NotNull
    protected IMultiblockStructure createStructure() {
        // 实例方法调用静态方法
        return createStructureInstance();
    }
    
    @Override
    @NotNull
    protected Block getMasterBlock() {
        // 返回主方块（控制器方块）
        return ModBlocks.AGREEMENT_CORE_BLOCK.get();
    }
    
    @Override
    @NotNull
    protected Block getPartBlock() {
        // 返回结构方块（与主方块相同）
        return ModBlocks.AGREEMENT_CORE_BLOCK.get();
    }
    
    /**
     * 获取主方块（静态方法，用于注册表初始化）
     */
    @NotNull
    private static Block getMasterBlockStatic() {
        return ModBlocks.AGREEMENT_CORE_BLOCK.get();
    }
    
    /**
     * 获取结构方块（静态方法，用于注册表初始化）
     */
    @NotNull
    private static Block getPartBlockStatic() {
        return ModBlocks.AGREEMENT_CORE_BLOCK.get();
    }
    
    /**
     * 获取结构尺寸信息
     */
    public int getWidth() {
        return 5;  // X 轴宽度（底座）
    }
    
    public int getHeight() {
        return 13; // Y 轴高度（1+12）
    }
    
    public int getDepth() {
        return 5;  // Z 轴深度（底座）
    }
}
