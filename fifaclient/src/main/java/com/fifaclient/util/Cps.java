package com.fifaclient.util;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;

/** Clicks-per-second tracker. Polled once per rendered frame (not per tick) so fast clicking is counted. */
public final class Cps {
    private static final ArrayDeque<Long> LEFT = new ArrayDeque<>();
    private static final ArrayDeque<Long> RIGHT = new ArrayDeque<>();
    private static boolean lastLeft, lastRight;
    public static boolean leftDown, rightDown;

    private Cps() {
    }

    public static void poll(MinecraftClient mc) {
        long handle = mc.getWindow().getHandle();
        boolean active = mc.currentScreen == null;
        leftDown = active && GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        rightDown = active && GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

        long now = System.currentTimeMillis();
        if (leftDown && !lastLeft) LEFT.addLast(now);
        if (rightDown && !lastRight) RIGHT.addLast(now);
        lastLeft = leftDown;
        lastRight = rightDown;

        while (!LEFT.isEmpty() && now - LEFT.peekFirst() > 1000) LEFT.pollFirst();
        while (!RIGHT.isEmpty() && now - RIGHT.peekFirst() > 1000) RIGHT.pollFirst();
    }

    public static int left() {
        return LEFT.size();
    }

    public static int right() {
        return RIGHT.size();
    }
}
