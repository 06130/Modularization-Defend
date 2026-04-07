package org.lingZero.modularization_defend.GeoModel;

import net.minecraft.resources.ResourceLocation;
import org.lingZero.modularization_defend.Blocks.Multiblock.ElectricityRepeater.ElectricityRepeaterBlockEntity;
import software.bernie.geckolib.model.GeoModel;

public class ElectricityRepeaterModel extends GeoModel<ElectricityRepeaterBlockEntity> {
    /**
     * Returns the resource path for the model json file to render based on the provided animatable
     */
    @Override
    public ResourceLocation getModelResource(ElectricityRepeaterBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("modularization_defend", "geo/electricity_repeater.geo.json");
    }

    /**
     * Returns the resource path for the texture file to render based on the provided animatable
     */
    @Override
    public ResourceLocation getTextureResource(ElectricityRepeaterBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("modularization_defend", "textures/block/electricity_repeater.png");
    }

    /**
     * Returns the resource path for the animation json file to use for animations based on the provided animatable
     */
    @Override
    public ResourceLocation getAnimationResource(ElectricityRepeaterBlockEntity animatable) {
        return null; // 暂无动画
    }
}
