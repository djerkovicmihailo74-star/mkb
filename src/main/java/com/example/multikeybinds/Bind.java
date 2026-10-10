package com.example.multikeybinds;

import org.lwjgl.glfw.GLFW;

/** One extra key or mouse button. type is "key" or "mouse". */
public class Bind {
    public String type;
    public int code;

    public Bind() {}
    public Bind(String type, int code) { this.type = type; this.code = code; }

    public boolean isDown(long window) {
        if ("mouse".equals(type)) return GLFW.glfwGetMouseButton(window, code) == GLFW.GLFW_PRESS;
        return GLFW.glfwGetKey(window, code) == GLFW.GLFW_PRESS;
    }

    public String displayName() {
        if ("mouse".equals(type)) {
            return switch (code) {
                case 0 -> "Left Click";
                case 1 -> "Right Click";
                case 2 -> "Middle Click";
                default -> "Mouse " + (code + 1);
            };
        }
        if (code >= 290 && code <= 301) return "F" + (code - 289);
        if (code >= 320 && code <= 329) return "Num " + (code - 320);
        switch (code) {
            case 32: return "Space";
            case 256: return "Esc";
            case 257: return "Enter";
            case 258: return "Tab";
            case 259: return "Backspace";
            case 260: return "Insert";
            case 261: return "Delete";
            case 262: return "Right";
            case 263: return "Left";
            case 264: return "Down";
            case 265: return "Up";
            case 266: return "PgUp";
            case 267: return "PgDn";
            case 268: return "Home";
            case 269: return "End";
            case 280: return "Caps";
            case 340: return "L-Shift";
            case 341: return "L-Ctrl";
            case 342: return "L-Alt";
            case 344: return "R-Shift";
            case 345: return "R-Ctrl";
            case 346: return "R-Alt";
            default:
                String n = GLFW.glfwGetKeyName(code, 0);
                return n != null ? n.toUpperCase() : "Key " + code;
        }
    }

    @Override public boolean equals(Object o) {
        return o instanceof Bind b && b.code == code && java.util.Objects.equals(b.type, type);
    }
    @Override public int hashCode() { return java.util.Objects.hash(type, code); }
}
