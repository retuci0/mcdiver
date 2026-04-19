package me.retucio.diver.mixin;

import me.retucio.diver.util.DrawUtil;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void onRender2D(GuiGraphicsExtractor gui, DeltaTracker deltaTracker, CallbackInfo ci) {
        DrawUtil.setGuiGraphics(gui);
    }
}
