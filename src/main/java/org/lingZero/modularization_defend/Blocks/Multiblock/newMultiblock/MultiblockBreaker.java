package org.lingZero.modularization_defend.Blocks.Multiblock.newMultiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lingZero.modularization_defend.util.DebugLogger;

import java.util.HashSet;
import java.util.Set;

/**
 * 多方块破坏类
 * 处理多方块结构的破坏逻辑
 */
public class MultiblockBreaker {
    
    /**
     * 标记是否正在破坏多方块结构（防止递归触发）
     */
    private static boolean isBreakingMultiblock = false;
    
    /**
     * 检查是否正在破坏多方块结构
     * @return true 如果正在破坏多方块
     */
    public static boolean isBreakingMultiblock() {
        return isBreakingMultiblock;
    }
    
    /**
     * 破坏多方块结构
     * 
     * @param level 世界
     * @param pos 被破坏的方块位置
     * @param player 破坏者（可能为 null）
     * @return 是否成功破坏整个结构
     */
    public static boolean breakMultiblock(@NotNull Level level, @NotNull BlockPos pos, @Nullable Player player) {
        DebugLogger.debug("===== MultiblockBreaker.breakMultiblock 被调用 =====");
        DebugLogger.debug("破坏位置：" + pos);
        
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) {
            DebugLogger.debug("BlockEntity 不存在，返回 false");
            return false;
        }
        
        // 强制转换为 AbstractMultiblockBlockEntity，直接使用 isController 字段
        if (!(be instanceof AbstractMultiblockBlockEntity multiblockBE)) {
            DebugLogger.error("BlockEntity 不是 AbstractMultiblockBlockEntity 类型：" + be.getClass().getName());
            return false;
        }
        
        boolean isMaster = multiblockBE.isController();
        BlockPos masterPosFromBe = multiblockBE.getControllerPos();
        
        DebugLogger.debug("isController: " + isMaster);
        DebugLogger.debug("storedControllerPos: " + masterPosFromBe);
        
        if (!isMaster) {
            // 如果不是主方块，从重定向到主方块进行破坏
            if (masterPosFromBe != null && !masterPosFromBe.equals(pos)) {
                DebugLogger.debug("重定向到主方块进行破坏：" + masterPosFromBe);
                return redirectAndBreak(level, masterPosFromBe, player);
            }
            DebugLogger.debug("无法获取有效的主方块坐标，返回 false");
            return false;
        }
        
        // 是主方块，直接破坏整个结构
        DebugLogger.info("是主方块（isController=true），开始破坏整个结构");
        DebugLogger.info("主方块位置：" + pos);
        
        // 获取 NBT 用于后续处理（如果需要）
        CompoundTag tag = be.saveWithoutMetadata(level.registryAccess());
        if (tag == null) {
            tag = new CompoundTag();
        }
        
        return breakEntireStructure(level, pos, tag, player);
    }
    
    /**
     * 重定向到主方块并破坏整个结构
     * @param level 世界
     * @param masterPos 主方块坐标
     * @param player 玩家
     * @return 是否成功破坏
     */
    private static boolean redirectAndBreak(@NotNull Level level, @NotNull BlockPos masterPos, @Nullable Player player) {
        BlockEntity masterBE = level.getBlockEntity(masterPos);
        if (masterBE == null) {
            return false;
        }
        
        CompoundTag masterTag = masterBE.saveWithoutMetadata(level.registryAccess());
        return breakEntireStructure(level, masterPos, masterTag, player);
    }
    
    /**
     * 破坏整个多方块结构
     * @param level 世界
     * @param masterPos 主方块坐标
     * @param tag 主方块 NBT
     * @param player 玩家
     * @return 是否成功破坏
     */
    private static boolean breakEntireStructure(@NotNull Level level, @NotNull BlockPos masterPos, 
                                               @NotNull CompoundTag tag, @Nullable Player player) {
        // 不再依赖结构名称，直接根据主方块坐标连锁破坏所有相关方块
        DebugLogger.debug("NBT 标签内容：" + tag);
        
        // 设置标志位，防止递归触发
        isBreakingMultiblock = true;
        
        try {
            DebugLogger.info("========== 开始破坏多方块结构 ==========");
            DebugLogger.info("主方块位置：" + masterPos);
            
            // 获取所有结构方块位置并破坏（不依赖结构名称）
            Set<BlockPos> partsToBreak = getStructureParts(level, masterPos, null);
            DebugLogger.info("需要破坏的方块数量：" + partsToBreak.size());
            DebugLogger.info("需要破坏的方块位置：" + partsToBreak);
            
            int brokenCount = 0;
            for (BlockPos partPos : partsToBreak) {
                DebugLogger.debug("破坏方块：" + partPos);
                breakBlockPart(level, partPos, player);
                brokenCount++;
            }
            
            DebugLogger.info("多方块结构破坏完成，共破坏 " + brokenCount + " 个方块");
            DebugLogger.info("==========================================");
            return true;
        } finally {
            // 无论成功还是失败，都要重置标志位
            isBreakingMultiblock = false;
        }
    }
    
    /**
     * 获取结构的所有组成部分
     * @param level 世界
     * @param masterPos 主方块坐标
     * @param structureName 结构名称（可选，为 null 时只匹配主方块坐标）
     * @return 需要破坏的方块位置集合
     */
    @NotNull
    private static Set<BlockPos> getStructureParts(@NotNull Level level, @NotNull BlockPos masterPos, 
                                                  @Nullable String structureName) {
        Set<BlockPos> parts = new HashSet<>();
        parts.add(masterPos);
        
        DebugLogger.debug("开始搜索结构方块...");
        DebugLogger.debug("主方块：" + masterPos + ", 结构名：" + (structureName != null ? structureName : "忽略"));
        
        // 搜索周围可能的结构方块
        // 这里假设结构不会太大，搜索一个合理的范围
        int searchRadius = 15;  // 增加到 15 以覆盖更大的结构
        
        int checkedCount = 0;
        int foundCount = 0;
        
        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dy = -searchRadius; dy <= searchRadius; dy++) {
                for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                    BlockPos checkPos = masterPos.offset(dx, dy, dz);
                    
                    if (checkPos.equals(masterPos)) {
                        continue;
                    }
                    
                    checkedCount++;
                    BlockEntity be = level.getBlockEntity(checkPos);
                    if (be != null) {
                        CompoundTag partTag = be.saveWithoutMetadata(level.registryAccess());
                        if (partTag != null) {
                            BlockPos storedMasterPos = IMultiblockBlockEntity.readMasterPos(partTag);
                            
                            // 只检查主方块坐标是否匹配，不再检查结构名称
                            if (masterPos.equals(storedMasterPos)) {
                                DebugLogger.debug("✓ 找到结构方块：" + checkPos + " [NBT: MasterPos=" + storedMasterPos + "]");
                                parts.add(checkPos);
                                foundCount++;
                            } else {
                                DebugLogger.trace("✗ 不匹配：" + checkPos + " [NBT: MasterPos=" + storedMasterPos + "]");
                            }
                        }
                    }
                }
            }
        }
        
        DebugLogger.info("搜索完成：检查了 " + checkedCount + " 个位置，找到 " + foundCount + " 个结构方块");
        DebugLogger.debug("共找到 " + parts.size() + " 个结构方块（包括主方块）");
        return parts;
    }
    
    /**
     * 破坏单个结构方块
     * @param level 世界
     * @param pos 方块位置
     * @param player 玩家
     */
    private static void breakBlockPart(@NotNull Level level, @NotNull BlockPos pos, @Nullable Player player) {
        BlockState state = level.getBlockState(pos);
        
        // 清除 NBT 数据
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null) {
            be.clearRemoved();
        }
        
        // 破坏方块（不触发掉落物，避免再次触发破坏事件）
        // 使用 setBlock 来直接移除方块，而不是 destroyBlock
        // 标志位 UPDATE_ALL | UPDATE_SUPPRESS_DROPS 表示更新客户端并抑制掉落物
        level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 
                      net.minecraft.world.level.block.Block.UPDATE_ALL | net.minecraft.world.level.block.Block.UPDATE_SUPPRESS_DROPS);
        
        DebugLogger.debug("已破坏结构方块：" + pos);
    }

}
