package com.gnarfodolfus.pixel.input;

import java.util.*;
import org.lwjgl.glfw.GLFW;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Updatable;

class GamePads implements Updatable, Disposable {

    private GamePad defaultGamePad = null;
    private final NavigableMap<Integer, GamePad> gamePads = new TreeMap<>();

    @Override
    public void update(DeltaTime dt) {
        gamePads.forEach((id, pad) -> pad.update(dt));
    }

    @Override
    public void dispose() {
        gamePads.forEach((id, pad) -> pad.dispose());
        gamePads.clear();
    }
    
    public void scan() {
        for (int id = GLFW.GLFW_JOYSTICK_1; id <= GLFW.GLFW_JOYSTICK_LAST; id++) {
            GamePad pad = gamePads.get(id);
            if (pad != null) {
                if (!pad.isConnected()) {
                    pad.dispose();
                    gamePads.remove(id);
                }
            } else {
                if (GLFW.glfwJoystickIsGamepad(id)) {
                    pad = new GamePad(id);
                    if (pad.isConnected()) { // Should be true
                        gamePads.put(id, pad);
                    }
                }
            }
        }
        updateDefault();
    }
    
    private void updateDefault() {
        var entry = gamePads.firstEntry();
        if (entry != null) {
            defaultGamePad = entry.getValue();
        }
    }
    
    public GamePad getDefault() {
        return defaultGamePad;
    }
    
    public GamePad get(int jid) {
        return gamePads.get(jid);
    }
    
    public Collection<GamePad> getAll() {
        return gamePads.values();
    }
}
