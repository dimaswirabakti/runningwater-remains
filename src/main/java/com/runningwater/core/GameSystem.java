package com.runningwater.core;

public abstract class GameSystem {
    protected boolean isActive;
    protected String systemName;

    protected GameSystem(String systemName) {
        this.systemName = systemName;
        this.isActive = false;
    }

    public abstract void Initialize();

    public abstract void UpdateSystem();

    public boolean isActive() { return isActive; }
    public String getSystemName() { return systemName; }
}
