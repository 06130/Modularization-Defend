package org.lingZero.m_defend.Items.TargetFilter;

import org.lingZero.m_defend.DataComponents.TargetFilterData;

/**
 * 中立生物过滤器物品
 * 用于筛选中立生物作为炮塔目标
 */
public class NeutralFilter extends BaseTargetFilter {

    public NeutralFilter(Properties properties) {
        super(properties);
    }

    @Override
    public TargetFilterData.FilterType getFilterType() {
        return TargetFilterData.FilterType.NEUTRAL;
    }
}
