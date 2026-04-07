package org.lingZero.modularization_defend.GeoModel;

import net.minecraft.resources.ResourceLocation;
import org.lingZero.modularization_defend.Blocks.Multiblock.AgreementCore.AgreementCoreBlockEntity;
import software.bernie.geckolib.model.GeoModel;

public class AgreementCoreModel extends GeoModel<AgreementCoreBlockEntity> {
    /**
     * Returns the resource path for the model json file to render based on the provided animatable
     */
    @Override
    public ResourceLocation getModelResource(AgreementCoreBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("modularization_defend", "geo/agreement_core.geo.json");
    }

    /**
     * Returns the resource path for the texture file to render based on the provided animatable
     */
    @Override
    public ResourceLocation getTextureResource(AgreementCoreBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("modularization_defend", "textures/block/agreement_core.png");
    }

    /**
     * Returns the resource path for the animation json file to use for animations based on the provided animatable
     */
    @Override
    public ResourceLocation getAnimationResource(AgreementCoreBlockEntity animatable) {
        return null; // 暂无动画
    }
}
