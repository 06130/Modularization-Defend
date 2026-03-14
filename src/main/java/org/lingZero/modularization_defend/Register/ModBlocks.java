package org.lingZero.modularization_defend.Register;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.modularization_defend.Blocks.EntityBlock.ElectricityRepeaterEntityBlock;
import org.lingZero.modularization_defend.ModularizationDefend;


public class ModBlocks {
    // 创建延迟注册表
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(ModularizationDefend.MODID);

    public static final DeferredBlock<ElectricityRepeaterEntityBlock> ELECTRICITY_REPEATER_BLOCK =
            BLOCKS.register("electricity_repeater_block", () -> new ElectricityRepeaterEntityBlock (BlockBehaviour.Properties.of()
                    .strength(3.0f, 6.0f)      // 硬度等级与抗爆等级
                    .sound(SoundType.STONE)                                // 挖掘时的声音类型
                    .requiresCorrectToolForDrops()                        // 需要正确的工具（镐）破坏并掉落物品

            ));
    
    /**
     * 方块标签，用于数据生成器
     */
    public static final TagKey<net.minecraft.world.level.block.Block> MINEABLE_WITH_PICKAXE =
            BlockTags.create(ResourceLocation.fromNamespaceAndPath(ModularizationDefend.MODID, "mineable/pickaxe"));
}
