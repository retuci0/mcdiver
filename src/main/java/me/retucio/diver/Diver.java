package me.retucio.diver;

import me.retucio.diver.screen.DiverScreen;
import net.fabricmc.api.ModInitializer;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Diver implements ModInitializer {

	private static Diver INSTANCE;

	public static final String MOD_ID = "mcdiver";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private final static Minecraft mc = Minecraft.getInstance();

	private final int SCREEN_KEY = GLFW.GLFW_KEY_RIGHT_SHIFT;
	private final int DOUBLE_TAP_THRESHOLD = 200;

	private int lastKey = -1;
	private long lastKeyTime = -1L;


	@Override
	public void onInitialize() {
		LOGGER.info("sup world");
	}

	public void onTick() {
		if (mc.isPaused()) return;

		if (System.currentTimeMillis() - lastKeyTime >= DOUBLE_TAP_THRESHOLD) {
			lastKey = -1;
		}
	}

	public void onKey(int key, int action) {
		if (action != GLFW.GLFW_PRESS) return;

		if (key == SCREEN_KEY && lastKey == SCREEN_KEY && mc.screen == null) {
			mc.setScreen(DiverScreen.getInstance());
			lastKey = -1;
			return;
		}

		lastKey = key;
		lastKeyTime = System.currentTimeMillis();
	}

	public static Diver getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new Diver();
		}
		return INSTANCE;
	}
}