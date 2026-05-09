package org.lingZero.m_defend.Client.Render;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.lingZero.m_defend.entity.TriggerArrow;

public class TriggerArrowRenderer extends ArrowRenderer<TriggerArrow> {

    public TriggerArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(TriggerArrow entity) {
        return ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png");
    }
}