package org.lingZero.m_defend.Register;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.m_defend.ModularizationDefend;
import org.lingZero.m_defend.entity.LaserProjectile;
import org.lingZero.m_defend.entity.TriggerArrow;

import java.util.function.Supplier;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ModularizationDefend.MODID);

    // 激光子弹实体 - 简化版本
    public static final Supplier<EntityType<LaserProjectile>> SIMPLE_LASER_PROJECTILE =
            ENTITIES.register("simple_laser_projectile", () -> EntityType.Builder.<LaserProjectile>of(
                    LaserProjectile::new,
                    MobCategory.MISC
            )
            .sized(0.25F, 0.25F)
            .clientTrackingRange(4)
            .updateInterval(10)
            .build("simple_laser_projectile"));

    // 触发箭矢实体
    public static final Supplier<EntityType<TriggerArrow>> TRIGGER_ARROW =
            ENTITIES.register("trigger_arrow", () -> EntityType.Builder.<TriggerArrow>of(
                    TriggerArrow::new,
                    MobCategory.MISC
            )
            .sized(0.5F, 0.5F)
            .clientTrackingRange(4)
            .updateInterval(20)
            .build("trigger_arrow"));
}
