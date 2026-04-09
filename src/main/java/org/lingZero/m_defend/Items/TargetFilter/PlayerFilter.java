package org.lingZero.m_defend.Items.TargetFilter;

import org.lingZero.m_defend.DataComponents.TargetFilterData;

/**
 * 玩家过滤器物品
 * 用于筛选玩家作为炮塔目标
 */
public class PlayerFilter extends BaseTargetFilter {

    public PlayerFilter(Properties properties) {
        super(properties);
    }

    @Override
    public TargetFilterData.FilterType getFilterType() {
        return TargetFilterData.FilterType.PLAYER;
    }
}
