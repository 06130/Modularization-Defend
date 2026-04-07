package org.lingZero.m_defend.Register;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.m_defend.Blocks.Multiblock.AffiliateBlock;
import org.lingZero.m_defend.Blocks.Multiblock.Turret1Block;
import org.lingZero.m_defend.Blocks.UpgradeCraftingTableBlock;
import org.lingZero.m_defend.ModularizationDefend;


public class ModBlocks {
    // 创建延迟注册表
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(ModularizationDefend.MODID);

    public static final DeferredBlock<UpgradeCraftingTableBlock> UPGRADE_CRAFTING_TABLE =
            BLOCKS.register("upgrade_crafting_table", () -> new UpgradeCraftingTableBlock(BlockBehaviour.Properties.of()
                    .strength(3.0f, 6.0f)      // 硬度等级与抗爆等级
                    .sound(SoundType.WOOD)                             // 挖掘时的声音类型（木质）
                    .requiresCorrectToolForDrops()                    // 需要正确的工具（斧）破坏并掉落物品
            ));
    
    public static final DeferredBlock<AffiliateBlock> AFFILIATE_BLOCK =
            BLOCKS.register("affiliate_block", () -> new AffiliateBlock(BlockBehaviour.Properties.of()
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
            ));
    
    public static final DeferredBlock<Turret1Block> TURRET1_BLOCK =
            BLOCKS.register("turret1_block", () -> new Turret1Block(BlockBehaviour.Properties.of()
                    .strength(5.0f, 10.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
            ));

    /**
     * 方块标签，用于数据生成器
     */
    public static final TagKey<net.minecraft.world.level.block.Block> MINEABLE_WITH_PICKAXE =
            BlockTags.create(ResourceLocation.fromNamespaceAndPath(ModularizationDefend.MODID, "mineable/pickaxe"));
}
