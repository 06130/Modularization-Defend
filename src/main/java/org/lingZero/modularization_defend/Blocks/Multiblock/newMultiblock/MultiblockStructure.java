package org.lingZero.modularization_defend.Blocks.Multiblock.newMultiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.lingZero.modularization_defend.util.DebugLogger;

import java.util.*;

/**
 * 多方块结构实现类
 * 用于解析和存储多方块结构信息
 */
public class MultiblockStructure implements IMultiblockStructure {
    
    private final String name;
    private final int width;
    private final int height;
    private final int depth;
    private final BlockPos masterPosition;
    private final Set<BlockPos> partPositions;
    private final Map<Integer, List<String>> layers;
    private final Map<Character, BlockPredicate> predicates;
    
    /**
     * 方块谓词接口
     */
    public interface BlockPredicate {
        boolean test(Level level, BlockPos pos, BlockState state);
    }
    
    private MultiblockStructure(Builder builder) {
        this.name = builder.name;
        this.width = builder.width;
        this.height = builder.height;
        this.depth = builder.depth;
        this.masterPosition = builder.masterPosition;
        this.partPositions = builder.partPositions;
        this.layers = builder.layers;
        this.predicates = builder.predicates;
    }
    
    @Override
    @NotNull
    public String getName() {
        return name;
    }
    
    @Override
    public int getWidth() {
        return width;
    }
    
    @Override
    public int getHeight() {
        return height;
    }
    
    @Override
    public int getDepth() {
        return depth;
    }
    
    @Override
    public BlockPos getMasterPosition() {
        return masterPosition;
    }
    
    @Override
    public boolean isMaster(BlockPos pos) {
        return pos.equals(masterPosition);
    }
    
    @Override
    public boolean isPartBlock(BlockPos pos) {
        return partPositions.contains(pos);
    }
    
    @Override
    public Set<BlockPos> getPartPositions() {
        return Collections.unmodifiableSet(partPositions);
    }
    
    @Override
    public boolean canForm(Level level, BlockPos origin) {
        DebugLogger.info("开始验证多方块结构，原点：" + origin);
        DebugLogger.info("主方块相对坐标：" + masterPosition);
            
        for (int y = 0; y < height; y++) {
            if (y >= layers.size()) continue;
                
            List<String> layer = layers.get(y);
            if (layer == null) continue;
                
            DebugLogger.info("检查第 " + y + " 层，该层有 " + layer.size() + " 行");
                
            // 逐行解析
            for (int z = 0; z < layer.size(); z++) {
                String row = layer.get(z);
                DebugLogger.info("  处理第 " + z + " 行，长度：" + row.length());
                    
                // 逐方块解析
                for (int x = 0; x < row.length(); x++) {
                    char c = row.charAt(x);
                        
                    // 空格表示"任意方块"位置（通常是结构外围的空气或其他不重要的方块）
                    // 这些位置不进行验证，允许任何方块存在
                    if (c == ' ') {
                        DebugLogger.info("    位置 [" + x + "," + y + "," + z + "] 为空格，跳过验证");
                        continue;
                    }
                        
                    BlockPos checkPos = origin.offset(x, y, z);
                    BlockState state = level.getBlockState(checkPos);
                        
                    DebugLogger.info("    检查位置 [" + x + "," + y + "," + z + "] (世界坐标：" + checkPos + ")，期望字符：'" + c + "'");
                    DebugLogger.info("      实际方块：" + state.getBlock().getName().getString());
                        
                    // 对于主动放置，只检查是否为空气或可替换的方块
                    if (!state.canBeReplaced() && !state.isAir()) {
                        DebugLogger.warn("      位置 " + checkPos + " 被方块阻挡，无法放置");
                        return false;
                    }
                    DebugLogger.info("      空间为空，可以放置");
                }
            }
        }
        DebugLogger.info("结构验证通过！");
        return true;
    }
    
    @Override
    public Map<Integer, List<String>> getLayers() {
        return Collections.unmodifiableMap(layers);
    }
    
    /**
     * 构建器模式
     */
    public static class Builder {
        private final String name;
        private int width = 0;
        private int height = 0;
        private int depth = 0;
        private BlockPos masterPosition = BlockPos.ZERO;
        private final Set<BlockPos> partPositions = new HashSet<>();
        private final Map<Integer, List<String>> layers = new HashMap<>();
        private final Map<Character, BlockPredicate> predicates = new HashMap<>();
        
        public Builder(String name) {
            this.name = name;
        }
        
        /**
         * 添加一层结构
         * @param rows 该层的行数据，每个字符串代表一行
         */
        public Builder aisle(String... rows) {
            int y = height;
            List<String> layerRows = new ArrayList<>();
            
            // 逐行添加，保持原始顺序
            for (String row : rows) {
                layerRows.add(row);
                
                // 记录该行的所有方块位置
                for (int x = 0; x < row.length(); x++) {
                    char c = row.charAt(x);
                    if (c != ' ') {
                        // z 坐标为当前行在该层中的索引
                        partPositions.add(new BlockPos(x, y, layerRows.size() - 1));
                    }
                }
            }
            
            layers.put(y, layerRows);
            height++;
            return this;
        }
        
        /**
         * 定义字符对应的方块谓词
         * @param c 字符
         * @param block 期望的方块
         */
        public Builder where(char c, Block block) {
            predicates.put(c, (level, pos, state) -> state.is(block));
            return this;
        }
        
        /**
         * 定义字符对应的谓词（用于主方块等特殊检测）
         * @param c 字符
         * @param predicate 谓词函数
         */
        public Builder where(char c, BlockPredicate predicate) {
            predicates.put(c, predicate);
            return this;
        }
        
        /**
         * 设置主方块位置（通过字符标记）
         * @param c 主方块字符标记
         */
        public Builder setMaster(char c) {
            // 查找所有该字符的位置并设置第一个为主方块
            for (BlockPos pos : partPositions) {
                int y = pos.getY();
                int z = pos.getZ();
                if (y < layers.size() && z < layers.get(y).size()) {
                    String row = layers.get(y).get(z);
                    if (pos.getX() < row.length() && row.charAt(pos.getX()) == c) {
                        this.masterPosition = pos;
                        break;
                    }
                }
            }
            return this;
        }
        
        /**
         * 构建结构
         */
        public MultiblockStructure build() {
            return new MultiblockStructure(this);
        }
    }
    
    /**
     * 创建构建器
     */
    public static Builder start(String name) {
        return new Builder(name);
    }
}
