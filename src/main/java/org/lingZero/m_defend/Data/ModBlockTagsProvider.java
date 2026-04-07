package org.lingZero.m_defend.Data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.lingZero.m_defend.ModularizationDefend;
import org.lingZero.m_defend.Register.ModBlocks;

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
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.AFFILIATE_BLOCK.get());
    }

    @Override
    public String getName() {
        return "Modularization Defend Block Tags";
    }
}
