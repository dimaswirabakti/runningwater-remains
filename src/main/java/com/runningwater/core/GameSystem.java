package com.runningwater.core;

/**
 * Abstract class untuk seluruh manager backend.
 * Subclass: CombatSystem, ScoringSystem, RewardSystem, SaveSystem, UIManager.
 */
public abstract class GameSystem {
    protected boolean isActive;
    protected String systemName;

    protected GameSystem(String systemName) {
        this.systemName = systemName;
        this.isActive = false;
    }

    /** Method abstract — tiap subclass wajib mendefinisikan prosedur startup-nya. */
    public abstract void Initialize();

    /** Method abstract — dipanggil tiap tick game loop. */
    public abstract void UpdateSystem();

    public boolean isActive() { return isActive; }
    public String getSystemName() { return systemName; }
}
