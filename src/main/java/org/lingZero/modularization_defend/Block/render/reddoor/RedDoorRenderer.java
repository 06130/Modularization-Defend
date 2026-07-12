package org.lingZero.modularization_defend.Block.render.reddoor;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.lingZero.modularization_defend.Block.example.RedDoorBlockEntity;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 红门的GeckoLib渲染器。
 * 使用自定义RenderType：半透明发光 + 写入深度缓冲。
 */
public class RedDoorRenderer extends GeoBlockRenderer<RedDoorBlockEntity> {

    public RedDoorRenderer() {
        super(new RedDoorModel());
    }

    @Override
    public RenderType getRenderType(RedDoorBlockEntity animatable,
                                     ResourceLocation texture,
                                     MultiBufferSource bufferSource,
                                     float partialTick) {
        return reddoorRenderType(texture);
    }

    private static RenderType reddoorRenderType(ResourceLocation texture) {
        return RenderType.create(
                "reddoor",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                true,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderType.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setOverlayState(RenderStateShard.OVERLAY)
                        .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                        .createCompositeState(true)
        );
    }
}
