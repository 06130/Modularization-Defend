package org.lingZero.modularization_defend.ApricityUI;


import com.sighs.apricityui.init.Element;
import com.sighs.apricityui.instance.ApricityContainerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.lingZero.modularization_defend.util.DebugLogger;

public class DefendCoreCurioItemUI extends BaseApricityScreen{

    public DefendCoreCurioItemUI(String uiPath, ApricityContainerMenu menu, Inventory inventory, Component title) {
        super(uiPath, menu, inventory, title);
    }

    /**
     * 初始化 UI，子类重写此方法
     */
    @Override
    protected void initializeUI() {
        DebugLogger.info("Initializing DefendCore Curio Item UI");
        
        // 初始化标题栏
        Element titleLabel = findElementById("title");
        if (titleLabel != null) {
            titleLabel.innerText = this.title.getString();
        }
        
        // 初始化物品槽位
        setupInventorySlots();
        
        // 初始化容器槽位
        setupContainerSlots();
        
        DebugLogger.info("DefendCore Curio Item UI initialized successfully");
    }
    
    /**
     * 设置玩家物品栏槽位
     */
    private void setupInventorySlots() {
        // 绑定玩家物品栏槽位到 UI
        // ApricityUI 会自动处理与 Minecraft 容器的同步
        DebugLogger.debug("Setup inventory slots for player inventory");
    }
    
    /**
     * 设置容器物品槽位
     */
    private void setupContainerSlots() {
        // 绑定容器物品槽位到 UI
        // ApricityUI 会自动处理与 Minecraft 容器的同步
        DebugLogger.debug("Setup container slots");
    }
}
