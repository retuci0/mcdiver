package me.retucio.diver.util;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ChatUtil {

    private static final Minecraft mc = Minecraft.getInstance();

    public static void info(String message) {
        mc.gui.getChat().addClientSystemMessage(Component.literal(ChatFormatting.AQUA + "[retbrowser] " + ChatFormatting.RESET + message + ChatFormatting.RESET));
    }

    public static void warn(String message) {
        mc.gui.getChat().addClientSystemMessage(Component.literal(ChatFormatting.AQUA + "[retbrowser] " + ChatFormatting.YELLOW + message + ChatFormatting.RESET));
    }

    public static void error(String message) {
        mc.gui.getChat().addClientSystemMessage(Component.literal(ChatFormatting.AQUA + "[retbrowser] " + ChatFormatting.RED + message + ChatFormatting.RESET));
    }
}
