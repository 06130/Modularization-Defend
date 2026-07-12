package org.lingZero.modularization_defend.Block.render.reddoor;

import net.minecraft.resources.ResourceLocation;
import org.lingZero.modularization_defend.Block.example.RedDoorBlockEntity;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;

/**
 * 红门的GeckoLib模型类。
 * 自动加载以下资源路径：
 * <ul>
 *   <li>模型：assets/modularization_defend/geo/block/reddoor.geo.json</li>
 *   <li>纹理：assets/modularization_defend/textures/block/reddoor.png</li>
 * </ul>
 */
public class RedDoorModel extends DefaultedBlockGeoModel<RedDoorBlockEntity> {

    public RedDoorModel() {
        super(ResourceLocation.fromNamespaceAndPath("modularization_defend", "reddoor"));
    }
}
