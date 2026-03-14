package org.lingZero.modularization_defend.Items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.sighs.apricityui.instance.ApricityContainerScreen;
import net.minecraft.client.Minecraft;

/**
 * 终端 - 右键时打开 ApricityUI Screen 界面
 */
public class TerminalItem extends Item {

    public TerminalItem(Properties properties) {
        super(properties);
    }

    /**
     * 当玩家右键使用此物品时调用
     * @param level 世界等级
     * @param player 玩家实体
     * @param usedHand 使用的手（主手或副手）
     * @return 交互结果
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        
        // 仅在客户端执行打开界面操作
        if (level.isClientSide()) {
            openTerminalScreen();
        }
        
        return InteractionResultHolder.success(itemStack);
    }

    /**
     * 打开终端 Screen 界面
     * 使用 ApricityContainerScreen 创建Screen
     */
    @OnlyIn(Dist.CLIENT)
    private void openTerminalScreen() {
        // 创建并显示 Screen
        // 注意：路径需要包含 .html 扩展名
        // 对应文件位置：src/main/resources/assets/modularization_defend/apricity/terminal.html
        ApricityContainerScreen screen = new ApricityContainerScreen("terminal.html");
        Minecraft.getInstance().setScreen(screen);
    }
}
