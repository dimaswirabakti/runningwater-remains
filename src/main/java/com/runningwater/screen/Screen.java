package com.runningwater.screen;

import com.runningwater.system.InputHandler;
import javafx.scene.canvas.GraphicsContext;

public interface Screen {
    void handleInput(InputHandler input);
    void update(double deltaTime);
    void render(GraphicsContext gc);
}
