package me.retucio.diver.screen.widgets.impl;

import me.retucio.diver.screen.DiverScreen;
import me.retucio.diver.screen.widgets.AutocompleteWidget;
import me.retucio.diver.screen.widgets.TextFieldWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.List;


public class DiverFieldWidget extends TextFieldWidget {

    private final AutocompleteWidget autocomplete;

    public DiverFieldWidget(int x, int y, int w, int maxChars) {
        super(x, y, w, "take a dive into the Minecraft instance...", maxChars);
        autocomplete = new AutocompleteWidget(x, y + h + PADDING, w, (DiverScreen.HEIGHT - 32) / AutocompleteWidget.ITEM_H);
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mx, int my) {
        super.render(gui, mx, my);
        autocomplete.setX(x);
        autocomplete.setY(y + h + 2 * PADDING);
        autocomplete.setW(w);
        autocomplete.render(gui, mx, my);
    }

    @Override
    public void onKey(int key, int action) {
        if (action == GLFW.GLFW_RELEASE) return;

        if (key == GLFW.GLFW_KEY_TAB && autocomplete.isVisible()) {
            applyCompletion(autocomplete.getSelected());
            return;
        }

        if (key == GLFW.GLFW_KEY_ESCAPE) {
            if (autocomplete.isVisible()) {
                autocomplete.hide();
                return;
            }
        }

        if (autocomplete.keyPressed(key, action)) return;

        if (key == GLFW.GLFW_KEY_ENTER) {
            autocomplete.hide();
            DiverScreen.getInstance().text.setText(resolvePath(text.trim()));
            return;
        }

        super.onKey(key, action);
        refreshSuggestions();
    }

    private void refreshSuggestions() {
        if (!text.contains(".")) {
            autocomplete.hide();
            return;
        }

        int lastDot = lastDotOutsideParens(text);
        if (lastDot < 0) { autocomplete.hide(); return; }

        String basePath = text.substring(0, lastDot);
        String prefix = text.substring(lastDot + 1);

        Object base;
        try {
            base = resolveToObject(basePath);
        } catch (Exception e) {
            autocomplete.hide();
            return;
        }
        if (base == null) { autocomplete.hide(); return; }

        List<String> matches = new ArrayList<>();
        collectMembers(base.getClass(), prefix, matches);

        autocomplete.setSuggestions(matches);
    }

    private void applyCompletion(String suggestion) {
        if (suggestion == null) return;
        int lastDot = lastDotOutsideParens(text);
        if (lastDot < 0) return;

        String insert = suggestion.contains("(")
                ? suggestion.substring(0, suggestion.indexOf('(') + 1)
                : suggestion;

        text = text.substring(0, lastDot + 1) + insert;
        autocomplete.hide();
    }

    private void collectMembers(Class<?> clazz, String prefix, List<String> out) {
        List<String> fields  = new ArrayList<>();
        List<String> methods = new ArrayList<>();

        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                String name = f.getName();
                if (name.startsWith(prefix) && !fields.contains(name))
                    fields.add(name);
            }
            for (Method m : c.getDeclaredMethods()) {
                String entry = formatMethod(m);
                if (m.getName().startsWith(prefix) && !methods.contains(entry))
                    methods.add(entry);
            }
        }

        // fields fist, then methods -- both sorted
        fields.sort(String::compareTo);
        methods.sort(String::compareTo);
        out.addAll(fields);
        out.addAll(methods);
    }

    private String formatMethod(Method m) {
        StringBuilder sb = new StringBuilder(m.getName()).append('(');

        Class<?>[] params = m.getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) sb.append(", ");
            String typeName = simpleTypeName(params[i]);
            sb.append(typeName).append(' ').append(syntheticParamName(params[i], i));
        }

        sb.append(") -> ").append(simpleTypeName(m.getReturnType()));
        return sb.toString();
    }

    private String syntheticParamName(Class<?> type, int index) {
        String base = simpleTypeName(type);
        String name = Character.toLowerCase(base.charAt(0)) + base.substring(1);
        return index == 0 ? name : name + index;
    }

    private String simpleTypeName(Class<?> type) {
        if (type == void.class) return "void";
        if (type.isPrimitive()) return type.getName();
        if (type.isArray()) return simpleTypeName(type.getComponentType()) + "[]";
        return type.getSimpleName();
    }


    private String resolvePath(String path) {
        if (!path.startsWith("mc."))
            return ChatFormatting.RED + "path must start with mc.";

        List<String> segments = splitPath(path.substring(3));
        Object current = Minecraft.getInstance();

        try {
            for (int i = 0; i < segments.size() - 1; i++) {
                current = resolveSegment(current, segments.get(i));
                if (current == null)
                    return ChatFormatting.RED + "null at: " + ChatFormatting.WHITE + segments.get(i);
            }

            String last = segments.getLast();

            if (isMethodCall(last)) {
                String name   = last.substring(0, last.indexOf('('));
                Object result = invokeSegment(current, last);
                return ChatFormatting.GOLD + name + "()"
                        + ChatFormatting.GRAY + " = "
                        + ChatFormatting.GREEN + result;
            } else {
                Field field = findField(current.getClass(), last);
                field.setAccessible(true);
                return ChatFormatting.GOLD + field.toString()
                        + ChatFormatting.GRAY + " = "
                        + ChatFormatting.GREEN + field.get(current);
            }
        } catch (NoSuchFieldException e) {
            return ChatFormatting.RED + "no such field: " + ChatFormatting.WHITE + e.getMessage();
        } catch (NoSuchMethodException e) {
            return ChatFormatting.RED + "no such method: " + ChatFormatting.WHITE + e.getMessage();
        } catch (IllegalAccessException e) {
            return ChatFormatting.RED + "access denied: " + ChatFormatting.WHITE + e.getMessage();
        } catch (Exception e) {
            return ChatFormatting.RED + "error: " + ChatFormatting.WHITE + e.getMessage();
        }
    }

    private Object resolveToObject(String path) throws Exception {
        if (!path.startsWith("mc")) throw new IllegalArgumentException();

        if (path.equals("mc")) return Minecraft.getInstance();

        List<String> segments = splitPath(path.substring(3));  // drop "mc."
        Object current = Minecraft.getInstance();

        for (String seg : segments) {
            current = resolveSegment(current, seg);
            if (current == null) return null;
        }
        return current;
    }

    private Object resolveSegment(Object obj, String segment) throws Exception {
        if (isMethodCall(segment)) return invokeSegment(obj, segment);
        Field f = findField(obj.getClass(), segment);
        f.setAccessible(true);
        return f.get(obj);
    }

    private Object invokeSegment(Object obj, String segment) throws Exception {
        int parenOpen  = segment.indexOf('(');
        String name    = segment.substring(0, parenOpen);
        String argsPart = segment.substring(parenOpen + 1, segment.length() - 1).trim();
        Object[] args  = argsPart.isEmpty() ? new Object[0] : parseArgs(argsPart);
        Method m = findMethod(obj.getClass(), name, args);
        m.setAccessible(true);
        return m.invoke(obj, args);
    }


    private Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            try { return c.getDeclaredField(name); } catch (NoSuchFieldException ignored) {}
        }
        throw new NoSuchFieldException(name);
    }

    private Method findMethod(Class<?> clazz, String name, Object[] args) throws NoSuchMethodException {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (!m.getName().equals(name) || m.getParameterCount() != args.length) continue;
                if (args.length == 0) return m;
                Class<?>[] params = m.getParameterTypes();
                boolean match = true;
                for (int i = 0; i < args.length; i++)
                    if (!isCompatible(params[i], args[i])) { match = false; break; }
                if (match) return m;
            }
        }
        throw new NoSuchMethodException(name);
    }

    private boolean isCompatible(Class<?> param, Object arg) {
        if (arg == null)  return !param.isPrimitive();
        if (param.isInstance(arg)) return true;
        if (param == int.class     && arg instanceof Integer) return true;
        if (param == long.class    && (arg instanceof Long   || arg instanceof Integer)) return true;
        if (param == float.class   && (arg instanceof Float  || arg instanceof Integer)) return true;
        if (param == double.class  && (arg instanceof Double || arg instanceof Float
                || arg instanceof Integer || arg instanceof Long)) return true;
        return param == boolean.class && arg instanceof Boolean;
    }

    private Object[] parseArgs(String raw) {
        List<String> tokens = splitArgs(raw);
        Object[] out = new Object[tokens.size()];
        for (int i = 0; i < tokens.size(); i++) out[i] = parseArg(tokens.get(i));
        return out;
    }

    private Object parseArg(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"")) return s.substring(1, s.length() - 1);
        if (s.equals("true"))  return true;
        if (s.equals("false")) return false;
        if (s.endsWith("L") || s.endsWith("l")) {
            try { return Long.parseLong(s.substring(0, s.length() - 1)); } catch (NumberFormatException ignored) {}
        }
        if (s.endsWith("f") || s.endsWith("F")) {
            try { return Float.parseFloat(s.substring(0, s.length() - 1)); } catch (NumberFormatException ignored) {}
        }
        if (s.contains(".")) {
            try { return Double.parseDouble(s); } catch (NumberFormatException ignored) {}
        }
        try { return Integer.parseInt(s); } catch (NumberFormatException ignored) {}
        return s;
    }

    private List<String> splitPath(String path) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur  = new StringBuilder();
        int depth   = 0;
        boolean inStr = false;
        for (char c : path.toCharArray()) {
            if (c == '"') inStr = !inStr;
            if (!inStr) {
                if      (c == '(') depth++;
                else if (c == ')') depth--;
                else if (c == '.' && depth == 0) { parts.add(cur.toString()); cur.setLength(0); continue; }
            }
            cur.append(c);
        }
        if (!cur.isEmpty()) parts.add(cur.toString());
        return parts;
    }

    private List<String> splitArgs(String raw) {
        List<String> args = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inStr = false;
        for (char c : raw.toCharArray()) {
            if (c == '"') inStr = !inStr;
            if (c == ',' && !inStr) { args.add(cur.toString().trim()); cur.setLength(0); }
            else cur.append(c);
        }
        if (!cur.isEmpty()) args.add(cur.toString().trim());
        return args;
    }

    private int lastDotOutsideParens(String s) {
        int depth = 0; boolean inStr = false; int last = -1;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"')                  inStr = !inStr;
            if (!inStr && c == '(')        depth++;
            if (!inStr && c == ')')        depth--;
            if (!inStr && c == '.' && depth == 0) last = i;
        }
        return last;
    }

    private boolean isMethodCall(String segment) {
        return segment.endsWith(")") && segment.contains("(");
    }
}