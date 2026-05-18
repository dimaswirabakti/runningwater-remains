package com.runningwater.system;

import com.runningwater.core.GameSystem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.HashSet;
import java.util.Set;

public class InputHandler extends GameSystem {

    private final Set<KeyCode> keysHeld        = new HashSet<>();
    private final Set<KeyCode> keysJustPressed  = new HashSet<>();

    public InputHandler() {
        super("InputHandler");
    }

    @Override
    public void Initialize() {
        this.isActive = true;
        keysHeld.clear();
        keysJustPressed.clear();
    }

    @Override
    public void UpdateSystem() {}

    public void handleKeyPressed(KeyEvent e) {
        KeyCode key = e.getCode();
        if (!keysHeld.contains(key)) {
            keysJustPressed.add(key);
        }
        keysHeld.add(key);
    }

    public void handleKeyReleased(KeyEvent e) {
        keysHeld.remove(e.getCode());
    }

    public boolean isHeld(KeyCode key) {
        return keysHeld.contains(key);
    }

    public boolean isJustPressed(KeyCode key) {
        return keysJustPressed.contains(key);
    }

    public void clearJustPressed() {
        keysJustPressed.clear();
    }
}
