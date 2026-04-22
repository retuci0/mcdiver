package me.retucio.diver.util;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;

public class KeyUtil {

    private static final Minecraft mc = Minecraft.getInstance();

    public static @Nullable Character keyToChar(int key) {
        boolean shift = isShiftDown();

        if (key >= GLFW.GLFW_KEY_A && key <= GLFW.GLFW_KEY_Z) {
            boolean upper = hasCapsLock() ^ shift;
            return upper ? (char) key : (char) (key + 32);
        }

        if (key >= GLFW.GLFW_KEY_0 && key <= GLFW.GLFW_KEY_9) {
            if (!shift) return (char) key;
            return switch (key) {
                case GLFW.GLFW_KEY_0 -> ')';
                case GLFW.GLFW_KEY_1 -> '!';
                case GLFW.GLFW_KEY_2 -> '@';
                case GLFW.GLFW_KEY_3 -> '#';
                case GLFW.GLFW_KEY_4 -> '$';
                case GLFW.GLFW_KEY_5 -> '%';
                case GLFW.GLFW_KEY_6 -> '^';
                case GLFW.GLFW_KEY_7 -> '&';
                case GLFW.GLFW_KEY_8 -> '*';
                case GLFW.GLFW_KEY_9 -> '(';
                default -> null;
            };
        }

        return switch (key) {
            case GLFW.GLFW_KEY_SPACE          -> ' ';
            case GLFW.GLFW_KEY_APOSTROPHE     -> shift ? '"'  : '\'';
            case GLFW.GLFW_KEY_COMMA          -> shift ? '<'  : ',';
            case GLFW.GLFW_KEY_MINUS          -> shift ? '_'  : '-';
            case GLFW.GLFW_KEY_PERIOD         -> shift ? '>'  : '.';
            case GLFW.GLFW_KEY_SLASH          -> shift ? '?'  : '/';
            case GLFW.GLFW_KEY_SEMICOLON      -> shift ? ':'  : ';';
            case GLFW.GLFW_KEY_EQUAL          -> shift ? '+'  : '=';
            case GLFW.GLFW_KEY_LEFT_BRACKET   -> shift ? '{'  : '[';
            case GLFW.GLFW_KEY_BACKSLASH      -> shift ? '|'  : '\\';
            case GLFW.GLFW_KEY_RIGHT_BRACKET  -> shift ? '}'  : ']';
            case GLFW.GLFW_KEY_GRAVE_ACCENT   -> shift ? '~'  : '`';
            default -> null;
        };
    }


    public static int getKeyCode(String keyName) {
        try {
            return (int) GLFW.class.getField("GLFW_KEY_" + keyName).get(null);
        } catch (Exception e) {
            return -1;
        }
    }

    public static String getKeyName(int keyCode) {
        for (Field f : GLFW.class.getDeclaredFields()) {
            if (f.getName().startsWith("GLFW_KEY_")) {
                try {
                    if (f.getInt(null) == keyCode)
                        return f.getName().substring("GLFW_KEY_".length());
                } catch (IllegalAccessException _) {}
            }
        }
        return "UNKNOWN";
    }

    public static boolean isKeyDown(int key) {
        return GLFW.glfwGetKey(mc.getWindow().handle(), key) != GLFW.GLFW_RELEASE;
    }

    public static boolean isCtrlDown() {
        return isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)
            || isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    public static boolean isShiftDown() {
        return isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)
            || isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    public static boolean hasCapsLock() {
        return (GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_CAPS_LOCK) & GLFW.GLFW_MOD_CAPS_LOCK) != 0;
    }
}
