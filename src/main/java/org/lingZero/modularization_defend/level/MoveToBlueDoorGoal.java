package org.lingZero.modularization_defend.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * 前往蓝门 Goal——关卡怪物唯一的 AI：持续向目标蓝门中心寻路。
 * 漏怪判定（进入蓝门后移除实体）由 LevelControllerBlockEntity 负责。
 */
public class MoveToBlueDoorGoal extends Goal {

    private final Mob mob;
    private final BlockPos target;
    private final double speed;

    public MoveToBlueDoorGoal(Mob mob, BlockPos target, double speed) {
        this.mob = mob;
        this.target = target;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return true;
    }

    @Override
    public void start() {
        navigateToDoor();
    }

    @Override
    public void tick() {
        // 寻路被地形/碰撞打断后重新下发
        if (mob.getNavigation().isDone()) {
            navigateToDoor();
        }
    }

    private void navigateToDoor() {
        mob.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, speed);
    }
}
