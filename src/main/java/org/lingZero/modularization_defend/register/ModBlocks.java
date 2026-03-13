package org.lingZero.modularization_defend.register;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.modularization_defend.blocks.BlockEntity.ElectricityRepeaterBlockEntity;
import org.lingZero.modularization_defend.blocks.EntityBlock.ElectricityRepeaterEntityBlock;
import org.lingZero.modularization_defend.modularization_defend;

import java.util.function.Supplier;

import static org.lingZero.modularization_defend.register.ModBlockEntities.BLOCK_ENTITIES;


public class ModBlocks {
    // 创建延迟注册表
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(modularization_defend.MODID);

    public static final DeferredBlock<ElectricityRepeaterEntityBlock> Electricity_Repeater =
            BLOCKS.register("ElectricityRepeater", () -> new ElectricityRepeaterEntityBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0f, 6.0f)      // 硬度等级与抗爆等级
                            .sound(SoundType.STONE)    // 挖掘时的声音类型
            ));
}
