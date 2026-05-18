package com.runningwater.system;

import com.runningwater.core.GameSystem;
import com.runningwater.screen.Screen;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

//  Mengelola Screen yang sedang aktif dan efek fade antar-screen.
public class UIManager extends GameSystem {

    private Screen currentScreen;
    private Screen nextScreen;

    // Fade transition
    private double fadeAlpha      = 0.0;   // 0 = transparan, 1 = hitam penuh
    private boolean fadingOut     = false;
    private boolean fadingIn      = false;
    private static final double FADE_SPEED = 2.5; // unit per detik

    public UIManager() {
        super("UIManager");
    }

    @Override
    public void Initialize() {
        this.isActive = true;
    }

    @Override
    public void UpdateSystem() {}

    // SCREEN MANAGEMENT

    // OVERLOADING 1: ganti screen tanpa animasi.
    public void ShowMenu(Screen screen) {
        ShowMenu(screen, false);
    }

    // OVERLOADING 2: ganti screen dengan animasi fade jika animated=true.
    public void ShowMenu(Screen screen, boolean animated) {
        if (animated) {
            nextScreen = screen;
            fadeAlpha  = 0.0;
            fadingOut  = true;
            fadingIn   = false;
        } else {
            currentScreen = screen;
        }
    }

    public Screen getCurrentScreen() { return currentScreen; }

    // UPDATE DIPANGGIL SETIAP FRAME OLEH GAME LOOP

    public void updateFade(double deltaTime) {
        if (fadingOut) {
            fadeAlpha = Math.min(1.0, fadeAlpha + FADE_SPEED * deltaTime);
            if (fadeAlpha >= 1.0) {
                currentScreen = nextScreen;
                nextScreen    = null;
                fadingOut     = false;
                fadingIn      = true;
            }
        } else if (fadingIn) {
            fadeAlpha = Math.max(0.0, fadeAlpha - FADE_SPEED * deltaTime);
            if (fadeAlpha <= 0.0) {
                fadingIn = false;
            }
        }
    }

    // Gambar overlay hitam di atas seluruh kanvas untuk efek fade.
    public void renderFade(GraphicsContext gc, double w, double h) {
        if (fadeAlpha > 0.0) {
            gc.save();
            gc.setGlobalAlpha(fadeAlpha);
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, w, h);
            gc.restore();
        }
    }

    public boolean isTransitioning() { return fadingOut || fadingIn; }
}
