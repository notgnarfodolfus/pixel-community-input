package com.gnarfodolfus.pixel.input;

import org.lwjgl.glfw.*;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.Updatable;
import org.pixel.math.Vector2;

public final class Mouse implements Updatable {

    private final CursorPositionHandler positionHandler = new CursorPositionHandler();
    private final MouseButtonHandler buttonHandler = new MouseButtonHandler();
    private final MouseScrollHandler scrollHandler = new MouseScrollHandler();

    private final Vector2 position = new Vector2();
    private final Vector2 lastPosition = new Vector2();

    private final Vector2 scroll = new Vector2();

    private int buttons;
    private int lastButtons;

    Mouse() {
    }

    void init(long windowHandle) {
        GLFW.glfwSetCursorPosCallback(windowHandle, positionHandler);
        GLFW.glfwSetMouseButtonCallback(windowHandle, buttonHandler);
        GLFW.glfwSetScrollCallback(windowHandle, scrollHandler);
    }

    /**
     * Clear pressed/released button states and update mouse positions for the
     * current frame.
     *
     * @param dt delta time
     */
    @Override
    public void update(DeltaTime dt) {
        lastPosition.set(position);
        position.set(positionHandler.position);
        scroll.set(scrollHandler.scroll);
        scrollHandler.scroll.set(0f, 0f);
        lastButtons = buttons;
        buttons = buttonHandler.state;
    }

    /**
     * Checks the current button state.
     *
     * @param button Button ID according to GLFW, e.g.
     * {@link GLFW#GLFW_MOUSE_BUTTON_1}
     * @return True if the mouse button is currently pressed, false otherwise.
     */
    public boolean getButton(int button) {
        return (buttons & (1 << button)) != 0;
    }

    /**
     * Checks wether the button was pressed this frame.
     *
     * @param button Button ID according to GLFW, e.g.
     * {@link GLFW#GLFW_MOUSE_BUTTON_1}
     * @return True if the mouse button was pressed, false otherwise.
     */
    public boolean getButtonDown(int button) {
        return (buttons & (1 << button)) > (lastButtons & (1 << button));
    }

    /**
     * Checks wether the button was released this frame.
     *
     * @param button Button ID according to GLFW, e.g.
     * {@link GLFW#GLFW_MOUSE_BUTTON_1}
     * @return True if the mouse button was released, false otherwise.
     */
    public boolean getButtonUp(int button) {
        return (buttons & (1 << button)) < (lastButtons & (1 << button));
    }

    /**
     * Returns a bit set determining which buttons are currently pressed. One
     * bits indicate the button with that index number is pressed.
     *
     * @return Bit set of pressed buttons.
     */
    public int getButtons() {
        return buttons;
    }

    /**
     * Returns a bit set determining which buttons were pressed this frames. One
     * bits indicate the button with that index number was pressed.
     *
     * @return Bit set of pressed buttons in this frame.
     */
    public int getButtonsDown() {
        return buttons & (buttons ^ lastButtons);
    }

    /**
     * Returns a bit set determining which buttons were released this frames.
     * One bits indicate the button with that index number was released.
     *
     * @return Bit set of released buttons in this frame.
     */
    public int getButtonsUp() {
        return lastButtons & (buttons ^ lastButtons);
    }

    /**
     * Get the current mouse position. The position is updated once each frame.
     *
     * @return The current mouse position.
     */
    public Vector2 getPosition() {
        return position;
    }

    /**
     * Get the difference of the mouse position between the last frame and the
     * current frame.
     *
     * @return The mouse offset since the last frame.
     */
    public Vector2 getMovement() {
        return Vector2.subtract(position, lastPosition);
    }

    /**
     * Get the mouse scroll value since the last frame. The y-value usually is
     * more significant.
     *
     * @return The mouse scroll since the last frame.
     */
    public Vector2 getScroll() {
        return scroll;
    }

    private static class CursorPositionHandler extends GLFWCursorPosCallback {

        private final Vector2 position = new Vector2();

        @Override
        public void invoke(long window, double x, double y) {
            position.set((float) x, (float) y);
        }
    }

    private static class MouseScrollHandler extends GLFWScrollCallback {

        private final Vector2 scroll = new Vector2();

        @Override
        public void invoke(long window, double xoffset, double yoffset) {
            // Unusual, but there may be more than one event per frame, so we
            // add up all offsets and clear the delta value in the update loop
            scroll.add((float) xoffset, (float) yoffset);
        }
    }

    private static class MouseButtonHandler extends GLFWMouseButtonCallback {

        private int state = 0;

        @Override
        public void invoke(long window, int button, int action, int mods) {
            if (button >= 0 && button < 32) {
                if (action == 0) { // button released, clear flag
                    state &= ~(1 << button);
                } else { // button pressed, set flag
                    state |= 1 << button;
                }
            }
        }
    }
}
