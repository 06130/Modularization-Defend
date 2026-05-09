package org.lingZero.m_defend.Event;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;
import org.lingZero.m_defend.entity.TriggerArrow;

/**
 * 触发箭矢击中实体时发出的事件，通过 NeoForge 事件总线投递
 */
public class TriggerArrowHitEntityEvent extends Event {

    private final TriggerArrow arrow;
    private final Entity target;

    public TriggerArrowHitEntityEvent(TriggerArrow arrow, Entity target) {
        this.arrow = arrow;
        this.target = target;
    }

    public TriggerArrow getArrow() {
        return arrow;
    }

    public Entity getTarget() {
        return target;
    }
}