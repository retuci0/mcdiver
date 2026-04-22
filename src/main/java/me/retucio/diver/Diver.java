package me.retucio.diver;

import me.retucio.diver.config.ConfigManager;
import me.retucio.diver.config.Macro;
import me.retucio.diver.screen.DiverScreen;
import me.retucio.diver.util.ChatUtil;
import me.retucio.diver.util.CommandUtil;
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

	private int lastKey = -1;
	private long lastKeyTime = -1L;
	private final int DOUBLE_TAP_THRESHOLD = 200;

	@Override
	public void onInitialize() {
		LOGGER.info("sup world");
		ConfigManager.getInstance().load();
	}

	public void onTick() {
		if (mc.isPaused()) return;

		if (System.currentTimeMillis() - lastKeyTime >= DOUBLE_TAP_THRESHOLD) {
			lastKey = -1;
		}
	}

	public void onKey(int key, int action) {
		if (action != GLFW.GLFW_PRESS) return;

		if (mc.screen == null
				&& key == ConfigManager.getInstance().getConfig().keybind
				&& lastKey == ConfigManager.getInstance().getConfig().keybind
		) {
			mc.setScreen(DiverScreen.getInstance());
			lastKey = -1;
			return;
		}

		Macro boundMacro = ConfigManager.getInstance().getMacroByKey(key);
		if (boundMacro != null) {
			String command = boundMacro.getCommand();
			if (command != null) {
				String result = CommandUtil.evaluate(command);
				if (mc.player != null) {
					ChatUtil.info(result);
				}
			}
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