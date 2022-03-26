package com.gnarfodolfus.pixel.input;

import java.nio.ByteBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.*;

public class GamePad implements Updatable, Disposable {

    private final int jid;
    private final String name;
    private final GLFWGamepadState state;

    private boolean connected;
    
    private int buttons;
    private int lastButtons;

    GamePad(int joystickId) {
        this.jid = joystickId;
        this.state = GLFWGamepadState.create();
        this.name = GLFW.glfwGetGamepadName(jid);
        this.connected = GLFW.glfwGetGamepadState(jid, state);
    }

    @Override
    public void dispose() {
        state.free();
    }

    @Override
    public void update(DeltaTime dt) {
        connected = GLFW.glfwGetGamepadState(jid, state);
        lastButtons = buttons;
        if (connected) {
            buttons = toBitset(state.buttons());
        } else {
            buttons = 0;
        }
    }

    public int getId() {
        return jid;
    }
    
    public String getName() {
        return name;
    }

    public boolean isConnected() {
        return connected;
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
     * Get the axe value of the given axe.
     *
     * @param axeId Axe ID according to GLFW, e.g.
     * {@link GLFW#GLFW_GAMEPAD_AXIS_LEFT_X}
     * @return The axe value.
     */
    public float getAxeValue(int axeId) {
        // TODO: Do we need the frame delta here as well? Maybe implement it.
        return state.axes(axeId);
    }

    private static int toBitset(ByteBuffer buf) {
        int value = 0;
        int len = buf.remaining();
        for (int i = 0; i < len; i++) {
            if (buf.get(i) > 0)
                value |= 1 << i;
        }
        return value;
    }
}
