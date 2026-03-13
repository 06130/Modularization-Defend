package org.lingZero.modularization_defend.GeoModel;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

public class ElectricityRepeaterModel extends GeoModel {
    /**
     * Returns the resource path for the {@link BakedGeoModel} (model json file) to render based on the provided animatable
     *
     * @param animatable
     */
    @Override
    public ResourceLocation getModelResource(GeoAnimatable animatable) {
        return null;
    }

    /**
     * Returns the resource path for the texture file to render based on the provided animatable
     *
     * @param animatable
     */
    @Override
    public ResourceLocation getTextureResource(GeoAnimatable animatable) {
        return null;
    }

    /**
     * Returns the resource path for the {@link BakedAnimations} (animation json file) to use for animations based on the provided animatable
     *
     * @param animatable
     */
    @Override
    public ResourceLocation getAnimationResource(GeoAnimatable animatable) {
        return null;
    }
}
