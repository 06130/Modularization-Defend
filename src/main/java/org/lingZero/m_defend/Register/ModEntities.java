package org.lingZero.m_defend.Register;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.m_defend.ModularizationDefend;
import org.lingZero.m_defend.entity.projectile.impl.SimpleLaserProjectile;

import java.util.function.Supplier;

public class ModEntities {
    
    public static final DeferredRegister<EntityType<?>> ENTITIES = 
            DeferredRegister.create(Registries.ENTITY_TYPE, ModularizationDefend.MODID);
    
    // 激光子弹实体 - 简化版本
    public static final Supplier<EntityType<SimpleLaserProjectile>> SIMPLE_LASER_PROJECTILE = 
            ENTITIES.register("simple_laser_projectile", () -> EntityType.Builder.<SimpleLaserProjectile>of(
                    SimpleLaserProjectile::new,
                    MobCategory.MISC
            )
            .sized(0.25F, 0.25F)
            .clientTrackingRange(4)
            .updateInterval(10)
            .build("simple_laser_projectile"));
}
