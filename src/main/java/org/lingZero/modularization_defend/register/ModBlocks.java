package org.lingZero.modularization_defend.register;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.modularization_defend.blocks.EntityBlock.ElectricityRepeaterEntityBlock;
import org.lingZero.modularization_defend.modularization_defend;


public class ModBlocks {
    // 创建延迟注册表
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(modularization_defend.MODID);

    public static final DeferredBlock<ElectricityRepeaterEntityBlock> ELECTRICITY_REPEATER_BLOCK =
            BLOCKS.register("my_block_1", () -> new ElectricityRepeaterEntityBlock (BlockBehaviour.Properties.of()
                    .strength(3.0f, 6.0f)      // 硬度等级与抗爆等级
                    .sound(SoundType.STONE)                                // 挖掘时的声音类型

            ));
}
