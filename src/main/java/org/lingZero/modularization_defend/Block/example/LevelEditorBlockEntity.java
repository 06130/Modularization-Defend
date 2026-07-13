package org.lingZero.modularization_defend.Block.example;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.lingZero.modularization_defend.Block.ModBlockEntities;
import org.lingZero.modularization_defend.Block.bounding.IBoundingBlock;

/**
 * 关卡编辑器的BlockEntity。
 * 定义一个可建造区域（默认 15x15x15），供玩家在其中设计关卡。
 */
public class LevelEditorBlockEntity extends BlockEntity implements IBoundingBlock {

    private static final int DEFAULT_ZONE_SIZE = 15;
    private int zoneSize = DEFAULT_ZONE_SIZE;

    public LevelEditorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEVEL_EDITOR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LevelEditorBlockEntity be) {
        // 预留：未来可在此处理关卡运行逻辑
    }

    /** 获取建造区域在世界中的包围盒（主方块上方 1 格起，zoneSize 格高） */
    public AABB getZoneAABB() {
        BlockPos start = getBlockPos().offset(0, 1, 0);
        return new AABB(
                start.getX(), start.getY(), start.getZ(),
                start.getX() + zoneSize, start.getY() + zoneSize, start.getZ() + zoneSize
        );
    }

    public int getZoneSize() {
        return zoneSize;
    }

    // ==================== 关卡操作 ====================

    /** 保存建造区域内的所有方块到持久化存储 */
    public void saveZone(Player player) {
        if (level == null || level.isClientSide) return;
        // TODO: 序列化 zoneSize³ 范围内的方块到 NBT/文件
        player.sendSystemMessage(Component.translatable("block.modularization_defend.level_editor.save"));
    }

    /** 从持久化存储加载方块到建造区域 */
    public void loadZone(Player player) {
        if (level == null || level.isClientSide) return;
        // TODO: 从 NBT/文件反序列化方块并放置到区域中
        player.sendSystemMessage(Component.translatable("block.modularization_defend.level_editor.load"));
    }

    /** 清空建造区域内的所有方块 */
    public void clearZone(Player player) {
        if (level == null || level.isClientSide) return;
        // TODO: 遍历 zoneSize³ 区域，将所有非空气方块移除
        player.sendSystemMessage(Component.translatable("block.modularization_defend.level_editor.clear"));
    }

    /** 扩展渲染包围盒到整个建造区域，确保玩家在任意角度都能看到边界线框。
     *  NeoForge 通过 mixin 注入此方法，编译期不可见故不加 @Override。 */
    public AABB getRenderBoundingBox() {
        return getZoneAABB();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("ZoneSize", zoneSize);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        zoneSize = tag.getInt("ZoneSize");
        if (zoneSize <= 0) zoneSize = DEFAULT_ZONE_SIZE;
    }
}
