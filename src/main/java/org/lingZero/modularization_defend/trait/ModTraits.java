package org.lingZero.modularization_defend.trait;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.lingZero.modularization_defend.modularization_defend;
import org.lingZero.modularization_defend.trait.impl.AttackDamageTrait;

/**
 * 词条类型注册中心。
 * 使用 NeoForge 自定义 Registry + DeferredRegister 模式注册所有词条类型和 Attachment。
 */
public class ModTraits {

    // ==================== 自定义注册表定义 ====================

    /** 词条类型注册表键 */
    public static final ResourceKey<Registry<Trait>> TRAIT_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(
                    modularization_defend.MODID, "trait"));

    /** 词条延迟注册表 */
    public static final DeferredRegister<Trait> TRAITS =
            DeferredRegister.create(TRAIT_REGISTRY_KEY, modularization_defend.MODID);

    // ==================== Attachment 注册表 ====================

    /** Attachment 类型延迟注册表（null 默认值必须注册后才能使用） */
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, modularization_defend.MODID);

    /** 附着在实体上的词条数据——无词条实体的 getData() 返回 null */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<TraitHolder>> TRAIT_HOLDER =
            ATTACHMENT_TYPES.register("trait_holder",
                    () -> AttachmentType.<TraitHolder>builder(() -> null).build());

    // ==================== 注册的词条实例 ====================

    /** 攻击伤害词条：每级 +2 攻击伤害，命中时产生暴击粒子 */
    public static final DeferredHolder<Trait, AttackDamageTrait> ATTACK_DAMAGE =
            TRAITS.register("attack_damage", AttackDamageTrait::new);

    /** 移动速度词条：每级 +10% 基础移速 */
    public static final DeferredHolder<Trait, Trait> MOVEMENT_SPEED =
            TRAITS.register("movement_speed", () -> TraitBuilder.create()
                    .attribute(Attributes.MOVEMENT_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, 0.1)
                    .maxLevel(5)
                    .color(0x55FF55)
                    .descriptionKey("trait.modularization_defend.movement_speed")
                    .build());

    // ==================== 事件回调 ====================

    /** 创建自定义 Trait 注册表（在 mod 构造器中通过 modEventBus 注册） */
    public static void onNewRegistry(NewRegistryEvent event) {
        event.create(new RegistryBuilder<>(TRAIT_REGISTRY_KEY));
    }
}
