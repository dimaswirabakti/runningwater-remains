package com.runningwater.screen;

import com.runningwater.system.InputHandler;
import javafx.scene.canvas.GraphicsContext;

/**
 * Kontrak untuk semua layar game.
 *
 * Setiap Screen harus:
 *  1. Membaca input
 *  2. Memperbarui logika
 *  3. Menggambar ke kanvas
 */
public interface Screen {
    void handleInput(InputHandler input);
    void update(double deltaTime);
    void render(GraphicsContext gc);
}
