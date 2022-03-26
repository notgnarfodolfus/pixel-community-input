package com.gnarfodolfus.pixel.input;

import java.util.Collection;
import org.pixel.commons.DeltaTime;

public final class Input {

    private static final Mouse mouse = new Mouse();
    private static final Keyboard keyboard = new Keyboard();
    private static final GamePads gamePads = new GamePads();

    public static void init(long windowHandle) {
        mouse.init(windowHandle);
        keyboard.init(windowHandle);
        gamePads.scan();
    }

    public static void update(DeltaTime dt) {
        mouse.update(dt);
        keyboard.update(dt);
        gamePads.update(dt);
    }
    
    public static void dispose() {
        gamePads.dispose();
    }
    
    public static Mouse getMouse() {
        return mouse;
    }

    public static Keyboard getKeyboard() {
        return keyboard;
    }
    
    public static GamePad getGamePad() {
        return gamePads.getDefault();
    }
    
    public static GamePad getGamePad(int index) {
        return gamePads.get(index);
    }
    
    public static Collection<GamePad> getGamePads() {
        return getGamePads(false);
    }
    
    public static Collection<GamePad> getGamePads(boolean rescan) {
        if (rescan)
            gamePads.scan();
        return gamePads.getAll();
    }
}
