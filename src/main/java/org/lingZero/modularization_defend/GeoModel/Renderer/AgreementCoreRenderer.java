package org.lingZero.modularization_defend.GeoModel.Renderer;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.lingZero.modularization_defend.Blocks.Multiblock.AgreementCore.AgreementCoreBlockEntity;
import org.lingZero.modularization_defend.GeoModel.AgreementCoreModel;

/**
 * 协议核心方块渲染器
 * 使用 GeckoLib 渲染 3D 模型
 * 只有主方块（控制器）会渲染模型，其他结构方块不渲染
 */
public class AgreementCoreRenderer extends AbstractMultiblockRenderer<AgreementCoreBlockEntity> {
    public AgreementCoreRenderer(BlockEntityRendererProvider.Context context) {
        super(new AgreementCoreModel());
    }
    
    /**
     * 创建协议核心的渲染边界框，覆盖整个 5x5x1 底座 + 3x3x12 柱体结构
     */
    @Override
    protected net.minecraft.world.phys.AABB createMultiblockRenderBoundingBox(AgreementCoreBlockEntity blockEntity) {
        // 创建一个覆盖整个 5x5x1 底座 + 3x3x12 柱体结构的边界框
        return new net.minecraft.world.phys.AABB(
            blockEntity.getBlockPos().getX() - 64,
            blockEntity.getBlockPos().getY() - 64,
            blockEntity.getBlockPos().getZ() - 64,
            blockEntity.getBlockPos().getX() + 66,  // 2 + 64
            blockEntity.getBlockPos().getY() + 77,  // 13 + 64
            blockEntity.getBlockPos().getZ() + 66   // 2 + 64
        );
    }
}
