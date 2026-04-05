package org.lingZero.modularization_defend.ldlibUI;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import static org.lingZero.modularization_defend.ldlibUI.DefendCoreGUI.createModularUI;

public class NewUIScreen extends Screen {

    private final ModularUI modularUI;

    public NewUIScreen(ModularUI modularUI) {
        super(Component.empty());
        this.modularUI = modularUI;
    }
    @Override
    protected void init() {
        super.init();
        var modularUI = createModularUI(getMinecraft().player);
        modularUI.setScreenAndInit(this);
        this.addRenderableWidget(modularUI.getWidget());
    }
    @Override
    public void removed() {
        super.removed();
    }

}
