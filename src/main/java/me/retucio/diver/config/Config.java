package me.retucio.diver.config;

import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class Config {

    public Map<String, Macro> macros = new HashMap<>();
    public int keybind = GLFW.GLFW_KEY_RIGHT_SHIFT;

    public Config() {}
}
