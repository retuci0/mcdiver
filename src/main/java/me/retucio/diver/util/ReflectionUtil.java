package me.retucio.diver.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public final class ReflectionUtil {

    private ReflectionUtil() {}


    /* lookup */

    public static Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            try { return c.getDeclaredField(name); } catch (NoSuchFieldException ignored) {}
        }
        throw new NoSuchFieldException(name);
    }

    public static Method findMethod(Class<?> clazz, String name, Object[] args) throws NoSuchMethodException {
        List<Method> byName = new ArrayList<>();
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(name)) byName.add(m);
            }
        }

        if (byName.isEmpty())
            throw new NoSuchMethodException(name);

        outer:  // first time using this lmfao
        for (Method m : byName) {
            if (m.getParameterCount() != args.length) continue;
            if (args.length == 0) return m;
            Class<?>[] params = m.getParameterTypes();
            for (int i = 0; i < args.length; i++) {
                if (!isCompatible(params[i], args[i])) continue outer;
            }
            return m;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(name).append('(');
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(args[i] == null ? "null" : args[i].getClass().getSimpleName());
        }
        sb.append(") - available overloads: ");
        for (int j = 0; j < byName.size(); j++) {
            if (j > 0) sb.append(" | ");
            sb.append(formatMethod(byName.get(j)));
        }
        throw new NoSuchMethodException(sb.toString());
    }


    /* coercion */

    public static boolean isCompatible(Class<?> param, Object arg) {
        if (arg == null) return !param.isPrimitive();
        if (param.isInstance(arg)) return true;
        if (param == int.class && arg instanceof Integer) return true;
        if (param == long.class && (arg instanceof Long || arg instanceof Integer)) return true;
        if (param == float.class && (arg instanceof Float || arg instanceof Integer)) return true;
        if (param == double.class && (arg instanceof Double || arg instanceof Float
                || arg instanceof Integer || arg instanceof Long)) return true;
        return param == boolean.class && arg instanceof Boolean;
    }

    public static Object coerce(Object value, Class<?> target) {
        if (value == null || target.isInstance(value)) return value;
        if (value instanceof Number n) {
            if (target == int.class || target == Integer.class) return n.intValue();
            if (target == long.class || target == Long.class) return n.longValue();
            if (target == float.class || target == Float.class) return n.floatValue();
            if (target == double.class || target == Double.class) return n.doubleValue();
            if (target == short.class || target == Short.class) return n.shortValue();
            if (target == byte.class || target == Byte.class) return n.byteValue();
        }
        if ((target == boolean.class || target == Boolean.class) && value instanceof Boolean b) return b;
        if ((target == String.class) && value instanceof String str) return str;
        return value;
    }


    /* formatting */

    public static String formatMethod(Method m) {
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

    public static String simpleTypeName(Class<?> type) {
        if (type == void.class) return "void";
        if (type.isPrimitive()) return type.getName();
        if (type.isArray()) return simpleTypeName(type.getComponentType()) + "[]";
        return type.getSimpleName();
    }

    private static String syntheticParamName(Class<?> type, int index) {
        String base = simpleTypeName(type);
        String name = Character.toLowerCase(base.charAt(0)) + base.substring(1);
        return index == 0 ? name : name + index;
    }


    /* member collection */

    public static void collectMembers(Class<?> clazz, String prefix, List<String> out) {
        List<String> fields = new ArrayList<>();
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

        fields.sort(String::compareTo);
        methods.sort(String::compareTo);
        out.addAll(fields);
        out.addAll(methods);
    }
}