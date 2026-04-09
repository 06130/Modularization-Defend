package org.lingZero.m_defend.Items.TargetFilter;

import org.lingZero.m_defend.DataComponents.TargetFilterData;

/**
 * 敌对生物过滤器物品
 * 用于筛选敌对生物作为炮塔目标
 */
public class HostileFilter extends BaseTargetFilter {

    public HostileFilter(Properties properties) {
        super(properties);
    }

    @Override
    public TargetFilterData.FilterType getFilterType() {
        return TargetFilterData.FilterType.HOSTILE;
    }
}
