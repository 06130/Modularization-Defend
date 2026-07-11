package org.lingZero.modularization_defend.Block.render.bluedoor;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.lingZero.modularization_defend.Block.example.BlueDoorBlockEntity;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 蓝门的GeckoLib渲染器。
 * 使用自定义RenderType：半透明发光 + 写入深度缓冲，
 * 确保水、藤蔓等半透明方块不会错误地渲染在蓝门上层。
 */
public class BlueDoorRenderer extends GeoBlockRenderer<BlueDoorBlockEntity> {

    public BlueDoorRenderer() {
        super(new BlueDoorModel());
    }

    @Override
    public RenderType getRenderType(BlueDoorBlockEntity animatable,
                                     ResourceLocation texture,
                                     MultiBufferSource bufferSource,
                                     float partialTick) {
        return bluedoorRenderType(texture);
    }

    /**
     * 创建自定义RenderType：
     * - 半透明混合（支持纹理Alpha通道）
     * - 自发光着色器（无视环境光照，始终保持全亮度）
     * - 写入深度缓冲（防止其他半透明方块覆盖显示）
     */
    private static RenderType bluedoorRenderType(ResourceLocation texture) {
        return RenderType.create(
                "bluedoor",
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
