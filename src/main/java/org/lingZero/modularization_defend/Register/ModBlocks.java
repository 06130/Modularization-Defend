package org.lingZero.modularization_defend.Register;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.modularization_defend.Blocks.Multiblock.AgreementCore.AgreementCoreMultiblock;
import org.lingZero.modularization_defend.Blocks.Multiblock.ElectricityRepeater.ElectricityRepeaterMultiblock;
import org.lingZero.modularization_defend.Blocks.UpgradeCraftingTableBlock;
import org.lingZero.modularization_defend.ModularizationDefend;


public class ModBlocks {
    // 创建延迟注册表
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(ModularizationDefend.MODID);

    public static final DeferredBlock<ElectricityRepeaterMultiblock> ELECTRICITY_REPEATER_BLOCK =
            BLOCKS.register("electricity_repeater_block", () -> new ElectricityRepeaterMultiblock(BlockBehaviour.Properties.of()
                    .strength(3.0f, 6.0f)      // 硬度等级与抗爆等级
                    .sound(SoundType.STONE)                                // 挖掘时的声音类型
                    .requiresCorrectToolForDrops()                        // 需要正确的工具（镐）破坏并掉落物品
                    .noOcclusion()                                        // 不遮挡光线和相邻方块的面

            ));
    
    public static final DeferredBlock<AgreementCoreMultiblock> AGREEMENT_CORE_BLOCK =
            BLOCKS.register("agreement_core_block", () -> new AgreementCoreMultiblock(BlockBehaviour.Properties.of()
                    .strength(5.0f, 10.0f)     // 更高的硬度等级与抗爆等级
                    .sound(SoundType.METAL)                            // 挖掘时的声音类型（金属）
                    .requiresCorrectToolForDrops()                    // 需要正确的工具（镐）破坏并掉落物品
                    .noOcclusion()                                    // 不遮挡光线和相邻方块的面

            ));

    public static final DeferredBlock<UpgradeCraftingTableBlock> UPGRADE_CRAFTING_TABLE =
            BLOCKS.register("upgrade_crafting_table", () -> new UpgradeCraftingTableBlock(BlockBehaviour.Properties.of()
                    .strength(3.0f, 6.0f)      // 硬度等级与抗爆等级
                    .sound(SoundType.WOOD)                             // 挖掘时的声音类型（木质）
                    .requiresCorrectToolForDrops()                    // 需要正确的工具（斧）破坏并掉落物品
            ));

    /**
     * 方块标签，用于数据生成器
     */
    public static final TagKey<net.minecraft.world.level.block.Block> MINEABLE_WITH_PICKAXE =
            BlockTags.create(ResourceLocation.fromNamespaceAndPath(ModularizationDefend.MODID, "mineable/pickaxe"));
}
