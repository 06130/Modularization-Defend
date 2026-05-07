package org.lingZero.m_defend.Blocks.Multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.lingZero.m_defend.Blocks.MultiblockFrame.BaseTurretBlock;
import org.lingZero.m_defend.Blocks.MultiblockFrame.BaseTurretBlockEntity;
import org.lingZero.m_defend.Config;
import org.lingZero.m_defend.DataComponents.TargetFilterData;
import org.lingZero.m_defend.DataComponents.TurretCoreData;
import org.lingZero.m_defend.DataComponents.TurretType;
import org.lingZero.m_defend.Items.TurretCore.frame.TurretCoreItem;
import org.lingZero.m_defend.Register.ModBlockEntities;
import org.lingZero.m_defend.entity.EntityTrace.*;

public class BasicBulletTurretV1BlockEntity extends BaseTurretBlockEntity {

    public BasicBulletTurretV1BlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BASIC_BULLET_TURRET_V1_BLOCK_ENTITY.get(), pos, state);

        // 配置射击系统
        int baseInterval = (int) (20.0 / Config.TURRET.basicBulletV1.fireRate.get());
        fireSystem.setBaseFireInterval(Math.max(1, baseInterval));
        fireSystem.setSearchCooldown(40);   // 每 2 秒搜索一次
    }

    // ==================== 实现基类抽象方法 ====================

    @Nullable
    @Override
    protected EntityTracker acquireTarget() {
        EntityFilter filter = getTargetFilter();
        if (filter == null) {
            return null;
        }

        return IEntitySearch.createAndLockTracker(
                getLevel(),
                getBlockPos(),
                Config.TURRET.basicBulletV1.range.get(),
                Config.TURRET.basicBulletV1.searchHeight.get(),
                filter
        );
    }

    @Override
    protected void onFire(Entity target) {
        var coreStack = coreItem(null);
        if (coreStack.isEmpty() || !(coreStack.getItem() instanceof TurretCoreItem turretCoreItem)) {
            return;
        }

        TurretType turretType = getTurretTypeFromBlock();
        TurretCoreData modifiedData = TurretCoreItem.getData(coreStack).withTurretType(turretType);

        turretCoreItem.executeAttack(
                getLevel(),
                modifiedData,
                getBlockPos().getCenter(),
                target instanceof LivingEntity livingTarget ? livingTarget : null,
                target.position()
        );
    }

    // ==================== 辅助方法 ====================

    private EntityFilter getTargetFilter() {
        TargetFilterData filterData = getCachedFilterData();
        if (filterData == null) {
            return null;
        }

        return switch (filterData.filterType()) {
            case HOSTILE -> EntityFilters.hostileMobs();
            case NEUTRAL -> EntityFilters.neutralMobs();
            case FRIENDLY -> EntityFilters.friendlyMobs();
            case PLAYER -> EntityFilters.players();
            case ENTITY_ID -> filterData.entityId()
                    .map(EntityFilters::byEntityId)
                    .orElse(null);
        };
    }

    private TurretType getTurretTypeFromBlock() {
        if (getLevel() != null) {
            var blockState = getLevel().getBlockState(getBlockPos());
            if (blockState.getBlock() instanceof BaseTurretBlock turretBlock) {
                return turretBlock.getTurretType();
            }
        }
        return TurretType.LASER;
    }
}