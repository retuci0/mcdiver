package me.retucio.diver.util;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.NoSuchElementException;


public class PathUtil {

    public static String evaluate(String path) {
        int assignOp = ParseUtil.findAssignmentOp(path);
        if (assignOp > 0)
            return evaluateAssignment(path.substring(0, assignOp).trim(),
                    path.substring(assignOp + 1).trim());

        if (!path.startsWith("mc.")) {
            return ChatFormatting.RED + "path must start with mc.";
        }

        List<String> segments = ParseUtil.splitPath(path.substring(3));
        Object current = Minecraft.getInstance();

        try {
            for (int i = 0; i < segments.size() - 1; i++) {
                current = resolveSegment(current, segments.get(i));
                if (current == null) {
                    return ChatFormatting.RED + "null at: " + ChatFormatting.WHITE + segments.get(i);
                }
            }

            String last = segments.getLast();
            if (ParseUtil.isMethodCall(last)) {
                String name = last.substring(0, last.indexOf('('));
                Object result = invokeSegment(current, last);
                return ChatFormatting.GOLD + name + "()"
                        + ChatFormatting.GRAY + " = "
                        + ChatFormatting.GREEN + result;
            } else {
                Field field = ReflectionUtil.findField(current.getClass(), last);
                field.setAccessible(true);
                return ChatFormatting.GOLD + field.toString()
                        + ChatFormatting.GRAY + " = "
                        + ChatFormatting.GREEN + field.get(current);
            }
        } catch (NoSuchFieldException e) {
            return ChatFormatting.RED + "no such field: " + ChatFormatting.WHITE + e.getMessage();
        } catch (NoSuchMethodException e) {
            return ChatFormatting.RED + "no such method: " + ChatFormatting.WHITE + e.getMessage();
        } catch (NoSuchElementException e) {
            return ChatFormatting.RED + "no such element: " + ChatFormatting.WHITE + e.getMessage();
        } catch (IllegalAccessException e) {
            return ChatFormatting.RED + "access denied: " + ChatFormatting.WHITE + e.getMessage();
        } catch (Exception e) {
            return ChatFormatting.RED + e.getClass().getName() + ": " + ChatFormatting.WHITE + e.getMessage();
        }
    }

    private static String evaluateAssignment(String lhs, String rhs) {
        if (!lhs.startsWith("mc."))
            return ChatFormatting.RED + "path must start with mc.";

        List<String> segments = ParseUtil.splitPath(lhs.substring(3));
        Object current = Minecraft.getInstance();

        try {
            for (int i = 0; i < segments.size() - 1; i++) {
                current = resolveSegment(current, segments.get(i));
                if (current == null)
                    return ChatFormatting.RED + "null at: " + ChatFormatting.WHITE + segments.get(i);
            }

            String fieldName = segments.getLast();
            Field field = ReflectionUtil.findField(current.getClass(), fieldName);
            field.setAccessible(true);
            Object value = ParseUtil.parseLiteral(rhs);
            value = ReflectionUtil.coerce(value, field.getType());
            field.set(current, value);
            return ChatFormatting.GOLD + fieldName
                    + ChatFormatting.GRAY + " = "
                    + ChatFormatting.GREEN + value;

        } catch (NoSuchFieldException e) {
            return ChatFormatting.RED + "no such field: " + ChatFormatting.WHITE + e.getMessage();
        } catch (IllegalAccessException e) {
            return ChatFormatting.RED + "access denied: " + ChatFormatting.WHITE + e.getMessage();
        } catch (Exception e) {
            return ChatFormatting.RED + e.getClass().getName() + ": " + ChatFormatting.WHITE + e.getMessage();
        }
    }

    public static Object resolveToObject(String path) throws Exception {
        if (!path.startsWith("mc")) throw new IllegalArgumentException();
        if (path.equals("mc")) return Minecraft.getInstance();

        List<String> segments = ParseUtil.splitPath(path.substring(3));
        Object current = Minecraft.getInstance();
        for (String seg : segments) {
            current = resolveSegment(current, seg);
            if (current == null) return null;
        }
        return current;
    }

    private static Object resolveSegment(Object obj, String segment) throws Exception {
        if (ParseUtil.isMethodCall(segment)) return invokeSegment(obj, segment);
        Field f = ReflectionUtil.findField(obj.getClass(), segment);
        f.setAccessible(true);
        return f.get(obj);
    }

    private static Object invokeSegment(Object obj, String segment) throws Exception {
        int parenOpen = segment.indexOf('(');
        String name = segment.substring(0, parenOpen);
        String argsPart = segment.substring(parenOpen + 1, segment.length() - 1).trim();
        Object[] args = argsPart.isEmpty() ? new Object[0] : ParseUtil.parseArgs(argsPart);
        Method m = ReflectionUtil.findMethod(obj.getClass(), name, args);
        m.setAccessible(true);
        return m.invoke(obj, args);
    }
}