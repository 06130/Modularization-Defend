package org.lingZero.modularization_defend.GeoModel.Renderer;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.lingZero.modularization_defend.Blocks.ElectricityRepeater.ElectricityRepeaterBlockEntity;
import org.lingZero.modularization_defend.GeoModel.ElectricityRepeaterModel;

/**
 * 电力中继器方块渲染器
 * 使用 GeckoLib 渲染 3D 模型
 * 只有主方块（控制器）会渲染模型，其他结构方块不渲染
 */
public class ElectricityRepeaterRenderer extends AbstractMultiblockRenderer<ElectricityRepeaterBlockEntity> {
    public ElectricityRepeaterRenderer(BlockEntityRendererProvider.Context context) {
        super(new ElectricityRepeaterModel());
    }
    
    /**
     * 创建电力中继器的渲染边界框，覆盖整个 2x2x10 结构
     */
    @Override
    protected net.minecraft.world.phys.AABB createMultiblockRenderBoundingBox(ElectricityRepeaterBlockEntity blockEntity) {
        // 创建一个覆盖整个 2x2x10 结构的边界框
        return new net.minecraft.world.phys.AABB(
            blockEntity.getBlockPos().getX() - 64,
            blockEntity.getBlockPos().getY() - 64,
            blockEntity.getBlockPos().getZ() - 64,
            blockEntity.getBlockPos().getX() + 66,  // 2 + 64
            blockEntity.getBlockPos().getY() + 74,  // 10 + 64
            blockEntity.getBlockPos().getZ() + 66   // 2 + 64
        );
    }
}
