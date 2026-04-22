package me.retucio.diver.util;

import java.util.ArrayList;
import java.util.List;

public final class ParseUtil {

    private ParseUtil() {}


    /* args parsing */

    public static Object[] parseArgs(String raw) {
        List<String> tokens = splitArgs(raw);
        Object[] out = new Object[tokens.size()];
        for (int i = 0; i < tokens.size(); i++) {
            out[i] = parseLiteral(tokens.get(i));
        }
        return out;
    }

    public static Object parseLiteral(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\""))
            return s.substring(1, s.length() - 1);
        if (s.equals("true")) return true;
        if (s.equals("false")) return false;
        if (s.endsWith("L") || s.endsWith("l")) {
            try { return Long.parseLong(s.substring(0, s.length() - 1)); } catch (NumberFormatException _) {}
        }
        if (s.endsWith("f") || s.endsWith("F")) {
            try { return Float.parseFloat(s.substring(0, s.length() - 1)); } catch (NumberFormatException _) {}
        }
        if (s.contains(".")) {
            try { return Double.parseDouble(s); } catch (NumberFormatException _) {}
        }
        try { return Integer.parseInt(s); } catch (NumberFormatException _) {}

        // if nothing matches, treat as string
        return s;
    }

    public static List<String> splitArgs(String raw) {
        List<String> args = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        int parenDepth = 0;
        boolean inStr = false;

        for (char c : raw.toCharArray()) {
            if (c == '"') {
                inStr = !inStr;
                cur.append(c);
                continue;
            }
            if (!inStr) {
                if (c == '(') parenDepth++;
                else if (c == ')') parenDepth--;
                else if (c == ',' && parenDepth == 0) {
                    args.add(cur.toString().trim());
                    cur.setLength(0);
                    continue;
                }
            }
            cur.append(c);
        }

        if (!cur.isEmpty()) args.add(cur.toString().trim());
        return args;
    }

    /* path splitting */

    public static List<String> splitPath(String path) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        int depth = 0;
        boolean inStr = false;
        for (char c : path.toCharArray()) {
            if (c == '"') inStr = !inStr;
            if (!inStr) {
                if (c == '(') depth++;
                else if (c == ')') depth--;
                else if (c == '.' && depth == 0) {
                    parts.add(cur.toString());
                    cur.setLength(0);
                    continue;
                }
            }
            cur.append(c);
        }
        if (!cur.isEmpty()) parts.add(cur.toString());
        return parts;
    }

    public static int lastDotOutsideParens(String s) {
        int depth = 0;
        boolean inStr = false;
        int last = -1;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') inStr = !inStr;
            if (!inStr && c == '(') depth++;
            if (!inStr && c == ')') depth--;
            if (!inStr && c == '.' && depth == 0) last = i;
        }
        return last;
    }

    public static int findAssignmentOp(String s) {
        boolean inStr = false;
        int depth = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') inStr = !inStr;
            if (!inStr) {
                if (c == '(') depth++;
                else if (c == ')') depth--;
                else if (c == '=' && depth == 0) {
                    char prev = i > 0 ? s.charAt(i - 1) : 0;
                    char next = i < s.length() - 1 ? s.charAt(i + 1) : 0;
                    if (next != '=' && prev != '!' && prev != '<' && prev != '>' && prev != '=')
                        return i;
                }
            }
        }
        return -1;
    }

    public static boolean isMethodCall(String segment) {
        return segment.endsWith(")") && segment.contains("(");
    }
}