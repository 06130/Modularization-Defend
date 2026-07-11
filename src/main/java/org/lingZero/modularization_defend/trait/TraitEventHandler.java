package org.lingZero.modularization_defend.trait;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.lingZero.modularization_defend.modularization_defend;

/**
 * 词条生命周期事件分发器。
 * 监听 NeoForge 游戏事件，将事件分发给目标实体身上所有词条的对应钩子方法。
 * <p>
 * 所有事件处理仅在服务端执行。
 */
@EventBusSubscriber(modid = modularization_defend.MODID, bus = EventBusSubscriber.Bus.GAME)
public class TraitEventHandler {

    /** 每 tick 触发所有词条的 onTick（仅 LivingEntity） */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.level().isClientSide()) return;

        TraitHolder holder = entity.getData(ModTraits.TRAIT_HOLDER.get());
        if (holder == null || holder.traitCount() == 0) return;

        Registry<Trait> registry = entity.level().registryAccess()
                .registryOrThrow(ModTraits.TRAIT_REGISTRY_KEY);

        for (var entry : holder.getAllTraits()) {
            Trait trait = registry.get(entry.getKey());
            if (trait != null) {
                trait.onTick(entity, entry.getValue());
            }
        }
    }

    /** 实体死亡时触发所有词条的 onDeath */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        TraitHolder holder = entity.getData(ModTraits.TRAIT_HOLDER.get());
        if (holder == null || holder.traitCount() == 0) return;

        Registry<Trait> registry = entity.level().registryAccess()
                .registryOrThrow(ModTraits.TRAIT_REGISTRY_KEY);

        for (var entry : holder.getAllTraits()) {
            Trait trait = registry.get(entry.getKey());
            if (trait != null) {
                trait.onDeath(entity, event.getSource(), entry.getValue());
            }
        }
    }

    /** 被攻击时触发受害者词条的 onAttacked（减伤前） */
    @SubscribeEvent
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;

        TraitHolder holder = victim.getData(ModTraits.TRAIT_HOLDER.get());
        if (holder == null || holder.traitCount() == 0) return;

        Registry<Trait> registry = victim.level().registryAccess()
                .registryOrThrow(ModTraits.TRAIT_REGISTRY_KEY);

        for (var entry : holder.getAllTraits()) {
            Trait trait = registry.get(entry.getKey());
            if (trait != null) {
                trait.onAttacked(victim, event.getSource(), event.getOriginalDamage(), entry.getValue());
            }
        }
    }

    /** 攻击造成伤害后触发攻击者词条的 onHurtTarget */
    @SubscribeEvent
    public static void onLivingDamagePost(LivingDamageEvent.Post event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;

        // 获取攻击者
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;

        TraitHolder holder = attacker.getData(ModTraits.TRAIT_HOLDER.get());
        if (holder == null || holder.traitCount() == 0) return;

        Registry<Trait> registry = attacker.level().registryAccess()
                .registryOrThrow(ModTraits.TRAIT_REGISTRY_KEY);

        for (var entry : holder.getAllTraits()) {
            Trait trait = registry.get(entry.getKey());
            if (trait != null) {
                trait.onHurtTarget(attacker, victim, entry.getValue());
            }
        }
    }

    /** 实体离开世界时触发所有词条的 onRemove 进行清理 */
    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.level().isClientSide()) return;

        TraitHolder holder = entity.getData(ModTraits.TRAIT_HOLDER.get());
        if (holder == null || holder.traitCount() == 0) return;

        Registry<Trait> registry = entity.level().registryAccess()
                .registryOrThrow(ModTraits.TRAIT_REGISTRY_KEY);

        // 先收集再清理，避免并发修改
        var traits = holder.getAllTraits();
        for (var entry : traits) {
            Trait trait = registry.get(entry.getKey());
            if (trait != null) {
                trait.onRemove(entity);
            }
        }
    }
}
