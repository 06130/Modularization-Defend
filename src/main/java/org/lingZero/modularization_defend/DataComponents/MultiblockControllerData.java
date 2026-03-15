package org.lingZero.modularization_defend.DataComponents;

/**
 * 多方块控制器标记数据组件（未使用）
 * 暂时保留但不会在渲染中使用
 */
public class MultiblockControllerData {
    private final boolean isController;
    
    public MultiblockControllerData(boolean isController) {
        this.isController = isController;
    }
    
    public boolean isController() {
        return isController;
    }
}
