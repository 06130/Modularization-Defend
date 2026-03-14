package org.lingZero.modularization_defend.Data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.lingZero.modularization_defend.ModularizationDefend;
import org.lingZero.modularization_defend.Register.ModBlocks;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * 方块标签数据生成器
 */
public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, ModularizationDefend.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // 设置 electricity_repeater_block 可以被镐破坏（挖掘等级 1）
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.ELECTRICITY_REPEATER_BLOCK.get());
        
        // 注意：NeoForge 会自动识别 requiresCorrectToolForDrops() 并关联到对应的工具标签
        // 不需要手动添加到 needs_stone_tool 等标签
    }

    @Override
    public String getName() {
        return "Modularization Defend Block Tags";
    }
}
