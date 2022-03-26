package com.gnarfodolfus.pixel.input;

import java.util.Arrays;
import org.lwjgl.glfw.*;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.Updatable;

public final class Keyboard implements Updatable {

    private static final int KEYBOARD_SIZE = GLFW.GLFW_KEY_LAST + 1;
    private static final int[] EMPTY = {};

    private final KeyboardInputHandler keyboardHandler = new KeyboardInputHandler();
    private final KeyboardTextHandler textHandler = new KeyboardTextHandler();

    private final FixedBitSet keys = new FixedBitSet(KEYBOARD_SIZE);
    private final FixedBitSet lastKeys = new FixedBitSet(KEYBOARD_SIZE);

    /**
     * Temporary reusable buffer to retrieve key IDs. Usually no more than 6
     * pressed keys are tracked anyways, so the size is sufficient.
     */
    private final int[] keyBuffer = new int[10];

    private long windowHandle;
    private boolean readText;

    Keyboard() {
    }

    void init(long windowHandle) {
        this.windowHandle = windowHandle;
        GLFW.glfwSetKeyCallback(windowHandle, keyboardHandler);
    }

    /**
     * Clear pressed/released button states and update mouse positions for the
     * current frame.
     *
     * @param dt delta time
     */
    @Override
    public void update(DeltaTime dt) {
        lastKeys.set(keys);
        keys.set(keyboardHandler.keys);
    }

    /**
     * Checks the current key state.
     *
     * @param button Key ID according to GLFW, e.g. {@link GLFW#GLFW_KEY_A}
     * @return True if the key is currently pressed, false otherwise.
     */
    public boolean getKey(int button) {
        return keys.get(button);
    }

    /**
     * Checks wether the key was pressed this frame.
     *
     * @param button Key ID according to GLFW, e.g. {@link GLFW#GLFW_KEY_A}
     * @return True if the key was pressed, false otherwise.
     */
    public boolean getKeyDown(int button) {
        return keys.get(button) && !lastKeys.get(button);
    }

    /**
     * Checks wether the key was released this frame.
     *
     * @param button Key ID according to GLFW, e.g. {@link GLFW#GLFW_KEY_A}
     * @return True if the key was released, false otherwise.
     */
    public boolean getKeyUp(int button) {
        return lastKeys.get(button) && !keys.get(button);
    }

    /**
     * Returns an array of all Key IDs that are currently pressed.
     *
     * @return Array of pressed keys
     */
    public int[] getKeys() {
        int num = keys.getSetBits(keyBuffer);
        return (num > 0) ? Arrays.copyOf(keyBuffer, num) : EMPTY;
    }

    /**
     * Returns an array of all Key IDs that were pressed this frame.
     *
     * @return Array of keys pressed this frame
     */
    public int[] getKeysDown() {
        if (keys.isEmpty())
            return EMPTY;
        int num = lastKeys.xorAnd(keys).getSetBits(keyBuffer);
        return (num > 0) ? Arrays.copyOf(keyBuffer, num) : EMPTY;
    }

    /**
     * Returns an array of all Key IDs that were released this frame.
     *
     * @return Array of keys released this frame
     */
    public int[] getKeysUp() {
        if (lastKeys.isEmpty())
            return EMPTY;
        int num = keys.xorAnd(lastKeys).getSetBits(keyBuffer);
        return (num > 0) ? Arrays.copyOf(keyBuffer, num) : EMPTY;
    }

    /**
     * Enable or disable keyboard layout dependent unicode text captures. When
     * enabled the text buffer can be consumed by calling {@link #readText()}.
     * The behaviour of the {@code getKey} methods is independent of this
     * setting.
     *
     * @param readTextInput Capture unicode text input
     */
    public void setReadText(boolean readTextInput) {
        if (windowHandle == 0)
            throw new IllegalStateException("Window not initialized");

        if (readTextInput) {
            GLFW.glfwSetCharCallback(windowHandle, textHandler);
        } else {
            textHandler.consume(); // clear
            GLFW.glfwSetCharCallback(windowHandle, null);
        }
        readText = readTextInput;
    }

    /**
     * Determines whether keyboard layout dependent unicode text captures are
     * enabled.
     *
     * @return True unicode text capture is enabled, false otherwise.
     */
    public boolean isReadText() {
        return readText;
    }

    /**
     * <b>Consumes</b> keyboard layout dependent unicode text input. By default
     * text input is disabled and this method always returns an empty string. To
     * enable text input call {@code setReadText(true)}. It is recommended to
     * disable text input when it is not used any more. Otherwise the buffer
     * will fill until {@code readText()} is called.
     *
     * @return unicode text input since the last {@code readText()} call or
     * empty string if there was no input
     */
    public String readText() {
        return textHandler.consume();
    }

    private static class KeyboardInputHandler extends GLFWKeyCallback {

        private final FixedBitSet keys = new FixedBitSet(KEYBOARD_SIZE);

        @Override
        public void invoke(long window, int key, int scanCode, int action, int mods) {
            if (key >= 0 && key < KEYBOARD_SIZE) {
                if (action == 0) { // key released, clear flag
                    keys.clear(key);
                } else { // key pressed, set flag
                    keys.set(key);
                }
            }
        }
    }

    private static class KeyboardTextHandler extends GLFWCharCallback {

        private boolean changed;
        private final StringBuilder text = new StringBuilder();

        public String consume() {
            if (changed) {
                String value = text.toString();
                text.setLength(0);
                changed = false;
                return value;
            } else {
                return "";
            }
        }

        @Override
        public void invoke(long window, int codepoint) {
            changed = true;
            text.appendCodePoint(codepoint);
        }
    }
}
