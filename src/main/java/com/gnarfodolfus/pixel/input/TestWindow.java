package com.gnarfodolfus.pixel.input;

import java.util.*;
import org.lwjgl.glfw.GLFW;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.*;
import org.pixel.content.ContentManager;
import org.pixel.content.Font;
import org.pixel.core.*;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.BlendMode;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Vector2;

public class TestWindow extends PixelWindow {

    private final Labels labels = new Labels();
    private Camera2D camera;

    public TestWindow(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        camera = new Camera2D(this);
        camera.setOrigin(0f, 0f);
        addWindowEventListener(new WindowEventListener() {
            @Override
            public void windowSizeChanged(int newWidth, int newHeight) {
                camera.setSize(newWidth, newHeight);
            }

            @Override
            public void windowModeChanged(WindowMode windowMode) {
            }
        });
        labels.load();
        super.load();
    }

    @Override
    public void centerWindow() {
        super.centerWindow();
        Input.init(getWindowHandle());
    }

    @Override
    public void dispose() {
        super.dispose();
        labels.dispose();
        Input.dispose();
    }

    @Override
    public void update(DeltaTime delta) {
        Input.update(delta);
        var mouse = Input.getMouse();
        float scroll = mouse.getScroll().getY();
        if (mouse.getButtonDown(GLFW.GLFW_MOUSE_BUTTON_1))
            labels.add("Left Click", mouse.getPosition(), 1f);
        if (mouse.getButtonDown(GLFW.GLFW_MOUSE_BUTTON_2))
            labels.add("Right Click", mouse.getPosition(), 1f);
        if (mouse.getButtonDown(GLFW.GLFW_MOUSE_BUTTON_3))
            labels.add("Middle Click", mouse.getPosition(), 1f);
        if (scroll > 0)
            labels.add("Scroll Up", mouse.getPosition(), 1f);
        if (scroll < 0)
            labels.add("Scroll Down", mouse.getPosition(), 1f);

        var keyboard = Input.getKeyboard();
        for (int key : keyboard.getKeysDown()) {
            String text;
            if ((key >= 'A' && key <= 'Z') || (key >= '0' && key <= '9'))
                text = Character.toString(key);
            else
                text = GLFW.glfwGetKeyName(key, 0);
            if (text == null)
                text = "Key " + key;
            labels.add(text, mouse.getPosition(), 1f);
        }
        labels.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
        labels.draw(camera, delta);
    }

    public static void main(String[] args) {
        WindowSettings settings = new WindowSettings(800, 600);
        settings.setWindowTitle("Input Test");
        settings.setWindowResizable(true);
        settings.setVsync(true);
        settings.setMultisampling(2);
        settings.setWindowMode(WindowMode.WINDOWED);

        PixelWindow gameWindow = new TestWindow(settings);
        gameWindow.start();
    }

    private static class Label implements Updatable {

        private final String text;
        private final Font font;
        private final Vector2 location;
        private final float maxAge;

        private float age;

        public Label(String text, Font font, Vector2 location, float duration) {
            this.text = text;
            this.font = font;
            this.location = Vector2.subtract(location, new Vector2(font.computeTextWidth(text) / 2f, font.getComputedFontSize() / 2f));
            this.maxAge = duration;
        }

        @Override
        public void update(DeltaTime dt) {
            age += dt.getElapsed();
        }

        public boolean isDead() {
            return age >= maxAge;
        }

        public void draw(SpriteBatch batch) {
            float m = Math.min(age / maxAge, 1f);
            Color color = new Color(0f, 0f, 0f, 1f - m);
            batch.drawText(font, text, Vector2.add(location, new Vector2(0f, -50f * age)), color, 30);
        }
    }

    private static class Labels implements Loadable, Updatable, Disposable {

        private final List<Label> labels = new ArrayList<>();

        private ContentManager content;
        private Font font;
        private SpriteBatch batch;

        public void add(String text, Vector2 location, float duration) {
            labels.add(new Label(text, font, location, duration));
        }

        @Override
        public void load() {
            content = new ContentManager();
            font = content.load("font/gidole-regular.ttf", Font.class);
            font.setFontSize(32);
            batch = new SpriteBatch();
        }

        @Override
        public void update(DeltaTime dt) {
            labels.removeIf(l -> {
                l.update(dt);
                return l.isDead();
            });
        }

        public void draw(Camera2D cam, DeltaTime delta) {
            batch.begin(cam.getViewMatrix(), BlendMode.NORMAL_BLEND);
            labels.forEach(l -> l.draw(batch));
            batch.end();
        }

        @Override
        public void dispose() {
            labels.clear();
            content.dispose();
            batch.dispose();
        }
    }
}
