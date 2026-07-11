package org.lingZero.modularization_defend.Block.render.bluedoor;

import net.minecraft.resources.ResourceLocation;
import org.lingZero.modularization_defend.Block.example.BlueDoorBlockEntity;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;

/**
 * 蓝门的GeckoLib模型类。
 * 自动加载以下资源路径：
 * <ul>
 *   <li>模型：assets/modularization_defend/geo/block/bluedoor.geo.json</li>
 *   <li>纹理：assets/modularization_defend/textures/block/bluedoor.png</li>
 *   <li>动画：assets/modularization_defend/animations/bluedoor.animation.json</li>
 * </ul>
 */
public class BlueDoorModel extends DefaultedBlockGeoModel<BlueDoorBlockEntity> {

    public BlueDoorModel() {
        super(ResourceLocation.fromNamespaceAndPath("modularization_defend", "bluedoor"));
    }
}
