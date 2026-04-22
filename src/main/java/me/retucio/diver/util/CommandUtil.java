package me.retucio.diver.util;

import me.retucio.diver.config.ConfigManager;
import me.retucio.diver.config.Macro;
import net.minecraft.ChatFormatting;

import java.util.Map;


public class CommandUtil {

    public static String evaluate(String input) {
        if (input.startsWith("$") && !(input.startsWith("$macro") || input.startsWith("$bind"))) {
            String macroName = input.substring(1);
            String command = ConfigManager.getInstance().getCommand(macroName);
            if (command != null) return evaluate(command);
            else return ChatFormatting.RED + "unknown macro: " + macroName;
        }

        if (input.startsWith("$macro ")) {
            return handleMacroCommand(input);
        } else if (input.startsWith("$bind")) {
            String k = input.split(" ", 3)[1];
            ConfigManager.getInstance().getConfig().keybind = KeyUtil.getKeyCode(k);
            return "bound diver screen to " + ChatFormatting.GREEN + k;
        }

        return PathUtil.evaluate(input);
    }

    private static String handleMacroCommand(String input) {
        String[] parts = input.split(" ", 5);
        if (parts.length < 2) return ChatFormatting.RED + "usage: $macro <list|set|del|run|bind|unbind> ...";

        String sub = parts[1];
        ConfigManager cm = ConfigManager.getInstance();

        switch (sub) {
            case "list":
                Map<String, Macro> macros = cm.getAllMacros();
                if (macros.isEmpty()) return ChatFormatting.YELLOW + "no macros defined.";
                StringBuilder sb = new StringBuilder(ChatFormatting.GOLD + "Macros:\n");
                for (Macro m : macros.values()) {
                    sb.append(ChatFormatting.GREEN).append(m.getName());
                    if (m.getKeybind() != -1) {
                        String keyName = KeyUtil.getKeyName(m.getKeybind());
                        sb.append(ChatFormatting.GRAY).append(" (bound to ").append(keyName).append(")");
                    }
                    sb.append(ChatFormatting.GRAY).append(": ")
                            .append(ChatFormatting.WHITE).append(m.getCommand()).append("\n");
                }
                return sb.toString();

            case "set":
                if (parts.length < 4) return ChatFormatting.RED + "usage: $macro set <name> <command>";
                cm.setMacro(parts[2], parts[3]);
                return ChatFormatting.GREEN + "macro \"" + parts[2] + "\" saved.";

            case "del":
                if (parts.length < 3) return ChatFormatting.RED + "usage: $macro del <name>";
                cm.removeMacro(parts[2]);
                return ChatFormatting.GREEN + "macro \"" + parts[2] + "\" deleted.";

            case "run":
                if (parts.length < 3) return ChatFormatting.RED + "usage: $macro run <name>";
                String cmd = cm.getCommand(parts[2]);
                if (cmd == null) return ChatFormatting.RED + "macro not found: " + parts[2];
                return evaluate(cmd);

            case "bind":
                if (parts.length < 4) return ChatFormatting.RED + "usage: $macro bind <name> <key>";
                String bindName = parts[2];
                String keyName = parts[3].toUpperCase();
                int keyCode = KeyUtil.getKeyCode(keyName);
                if (keyCode == -1) return ChatFormatting.RED + "invalid key: " + keyName;
                if (cm.getMacro(bindName) == null)
                    return ChatFormatting.RED + "macro not found: " + bindName;
                cm.bindKey(bindName, keyCode);
                return ChatFormatting.GREEN + "bound \"" + bindName + "\" to " + keyName;

            case "unbind":
                if (parts.length < 3) return ChatFormatting.RED + "usage: $macro unbind <name>";
                cm.unbindKey(parts[2]);
                return ChatFormatting.GREEN + "unbound \"" + parts[2] + "\"";

            default:
                return ChatFormatting.RED + "unknown subcommand: " + sub;
        }
    }
}