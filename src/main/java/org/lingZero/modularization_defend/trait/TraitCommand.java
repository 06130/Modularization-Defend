package org.lingZero.modularization_defend.trait;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.Map;

/**
 * 词条调试命令。
 *
 * <pre>
 * /traits                  — 查看自己身上的词条
 * /traits list             — 列出所有注册的词条类型
 * /traits give ID 等级     — 给自己添加词条（权限2）
 * /traits give ID 等级 目标 — 给目标实体添加词条（权限2）
 * /traits remove ID        — 移除自身词条（权限2）
 * /traits remove ID 目标   — 移除目标实体词条（权限2）
 * </pre>
 */
public class TraitCommand {

    private TraitCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var cmd = Commands.literal("traits");

        // /traits — 查看自身词条
        cmd.executes(ctx -> showOwnTraits(ctx.getSource()));

        // /traits list — 列出所有词条类型
        cmd.then(Commands.literal("list")
                .executes(ctx -> listAllTraits(ctx.getSource())));

        // /traits give <trait> <level> [target]
        var give = Commands.literal("give")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("trait", ResourceLocationArgument.id())
                        .suggests((ctx, builder) -> {
                            var registry = ctx.getSource().getLevel().registryAccess()
                                    .registryOrThrow(ModTraits.TRAIT_REGISTRY_KEY);
                            return SharedSuggestionProvider.suggestResource(
                                    registry.keySet(), builder);
                        })
                        .then(Commands.argument("level", IntegerArgumentType.integer(1, 100))
                                .executes(ctx -> giveTrait(
                                        ctx.getSource(),
                                        ResourceLocationArgument.getId(ctx, "trait"),
                                        IntegerArgumentType.getInteger(ctx, "level"),
                                        null))
                                .then(Commands.argument("target", EntityArgument.entities())
                                        .executes(ctx -> giveTrait(
                                                ctx.getSource(),
                                                ResourceLocationArgument.getId(ctx, "trait"),
                                                IntegerArgumentType.getInteger(ctx, "level"),
                                                EntityArgument.getEntities(ctx, "target"))))));
        cmd.then(give);

        // /traits remove <trait> [target]
        var remove = Commands.literal("remove")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("trait", ResourceLocationArgument.id())
                        .suggests((ctx, builder) -> {
                            var registry = ctx.getSource().getLevel().registryAccess()
                                    .registryOrThrow(ModTraits.TRAIT_REGISTRY_KEY);
                            return SharedSuggestionProvider.suggestResource(
                                    registry.keySet(), builder);
                        })
                        .executes(ctx -> removeTrait(
                                ctx.getSource(),
                                ResourceLocationArgument.getId(ctx, "trait"),
                                null))
                        .then(Commands.argument("target", EntityArgument.entities())
                                .executes(ctx -> removeTrait(
                                        ctx.getSource(),
                                        ResourceLocationArgument.getId(ctx, "trait"),
                                        EntityArgument.getEntities(ctx, "target")))));
        cmd.then(remove);

        dispatcher.register(cmd);
    }

    // ==================== 命令实现 ====================

    private static int showOwnTraits(CommandSourceStack source) throws CommandSyntaxException {
        Player player = source.getPlayerOrException();
        return showTraits(source, player);
    }

    private static int showTraits(CommandSourceStack source, LivingEntity entity) {
        TraitHolder holder = entity.getData(ModTraits.TRAIT_HOLDER.get());
        if (holder == null || holder.traitCount() == 0) {
            source.sendSuccess(() -> Component.translatable(
                    "command.modularization_defend.traits.empty",
                    entity.getDisplayName()), false);
            return Command.SINGLE_SUCCESS;
        }

        source.sendSuccess(() -> Component.translatable(
                "command.modularization_defend.traits.header",
                entity.getDisplayName(), holder.traitCount()), false);

        Registry<Trait> registry = source.getLevel().registryAccess()
                .registryOrThrow(ModTraits.TRAIT_REGISTRY_KEY);

        for (Map.Entry<ResourceLocation, Integer> entry : holder.getAllTraits()) {
            Trait trait = registry.get(entry.getKey());
            String name = trait != null
                    ? Component.translatable("trait." + entry.getKey().getNamespace() + "." + entry.getKey().getPath()).getString()
                    : entry.getKey().toString();
            source.sendSuccess(() -> Component.literal(
                    "  §e" + name + " §7Lv." + entry.getValue()), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int listAllTraits(CommandSourceStack source) {
        Registry<Trait> registry = source.getLevel().registryAccess()
                .registryOrThrow(ModTraits.TRAIT_REGISTRY_KEY);

        var entries = registry.entrySet();
        source.sendSuccess(() -> Component.translatable(
                "command.modularization_defend.traits.list_header", entries.size()), false);

        for (var entry : entries) {
            ResourceLocation id = entry.getKey().location();
            Trait trait = entry.getValue();
            source.sendSuccess(() -> Component.literal(
                    "  §e" + id + " §7(maxLv=" + trait.getMaxLevel() + ")"), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int giveTrait(CommandSourceStack source, ResourceLocation traitId,
                                  int level, Collection<? extends Entity> targets)
            throws CommandSyntaxException {
        Registry<Trait> registry = source.getLevel().registryAccess()
                .registryOrThrow(ModTraits.TRAIT_REGISTRY_KEY);

        Trait trait = registry.get(traitId);
        if (trait == null) {
            source.sendFailure(Component.translatable(
                    "command.modularization_defend.traits.not_found", traitId.toString()));
            return 0;
        }

        Collection<? extends Entity> entities;
        if (targets != null) {
            entities = targets;
        } else {
            entities = java.util.Collections.singleton(source.getPlayerOrException());
        }

        int count = 0;
        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.level().isClientSide()) continue;

            TraitHolder holder = living.getData(ModTraits.TRAIT_HOLDER.get());
            if (holder == null) {
                holder = new TraitHolder();
                living.setData(ModTraits.TRAIT_HOLDER.get(), holder);
            }

            holder.addTrait(living, traitId, trait, level);
            count++;
        }

        int applied = count;
        int lvl = level;
        source.sendSuccess(() -> Component.translatable(
                "command.modularization_defend.traits.given",
                traitId.toString(), lvl, applied), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int removeTrait(CommandSourceStack source, ResourceLocation traitId,
                                    Collection<? extends Entity> targets)
            throws CommandSyntaxException {
        Registry<Trait> registry = source.getLevel().registryAccess()
                .registryOrThrow(ModTraits.TRAIT_REGISTRY_KEY);

        Trait trait = registry.get(traitId);
        if (trait == null) {
            source.sendFailure(Component.translatable(
                    "command.modularization_defend.traits.not_found", traitId.toString()));
            return 0;
        }

        Collection<? extends Entity> entities;
        if (targets != null) {
            entities = targets;
        } else {
            entities = java.util.Collections.singleton(source.getPlayerOrException());
        }

        int count = 0;
        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.level().isClientSide()) continue;

            TraitHolder holder = living.getData(ModTraits.TRAIT_HOLDER.get());
            if (holder == null || !holder.hasTrait(traitId)) continue;

            holder.removeTrait(living, traitId, trait);
            count++;
        }

        int removed = count;
        source.sendSuccess(() -> Component.translatable(
                "command.modularization_defend.traits.removed",
                traitId.toString(), removed), true);

        return Command.SINGLE_SUCCESS;
    }
}
