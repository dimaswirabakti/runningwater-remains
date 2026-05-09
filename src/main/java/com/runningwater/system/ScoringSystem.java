package com.runningwater.system;

import com.runningwater.core.GameSystem;

/**
 * Mengelola skor pemain selama game berjalan.
 */
public class ScoringSystem extends GameSystem {
    private int currentScore;
    private int highScore;
    private float multiplier;

    public ScoringSystem() {
        super("ScoringSystem");
        this.multiplier = 1.0f;
    }

    @Override
    public void Initialize() {
        this.isActive = true;
        this.currentScore = 0;
    }

    @Override
    public void UpdateSystem() {
        // Score update event-driven (lewat AddScore), tidak perlu per-tick.
    }

    /** OVERLOADING — versi 1: pakai multiplier internal. */
    public void AddScore(int pts) {
        AddScore(pts, this.multiplier);
    }

    /** OVERLOADING — versi 2: terima multiplier kustom (untuk combo bonus, boss kill, dst). */
    public void AddScore(int pts, float multiplier) {
        int gained = (int) (pts * multiplier);
        this.currentScore += gained;
        if (this.currentScore > this.highScore) {
            this.highScore = this.currentScore;
        }
    }

    public int GetScore() { return currentScore; }
    public int getHighScore() { return highScore; }

    public float getMultiplier() { return multiplier; }
    public void setMultiplier(float multiplier) { this.multiplier = multiplier; }

    public void resetScore() { this.currentScore = 0; }
}
