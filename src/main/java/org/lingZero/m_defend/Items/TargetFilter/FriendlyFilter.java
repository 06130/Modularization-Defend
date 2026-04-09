package org.lingZero.m_defend.Items.TargetFilter;

import org.lingZero.m_defend.DataComponents.TargetFilterData;

/**
 * 友好生物过滤器物品
 * 用于筛选友好生物作为炮塔目标
 */
public class FriendlyFilter extends BaseTargetFilter {

    public FriendlyFilter(Properties properties) {
        super(properties);
    }

    @Override
    public TargetFilterData.FilterType getFilterType() {
        return TargetFilterData.FilterType.FRIENDLY;
    }
}
