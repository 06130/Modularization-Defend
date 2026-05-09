package org.lingZero.m_defend.Event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;
import org.lingZero.m_defend.entity.TriggerArrow;

/**
 * 触发箭矢击中方块时发出的事件，通过 NeoForge 事件总线投递
 */
public class TriggerArrowHitBlockEvent extends Event {

    private final TriggerArrow arrow;
    private final BlockPos pos;

    public TriggerArrowHitBlockEvent(TriggerArrow arrow, BlockPos pos) {
        this.arrow = arrow;
        this.pos = pos;
    }

    public TriggerArrow getArrow() {
        return arrow;
    }

    public BlockPos getPos() {
        return pos;
    }
}