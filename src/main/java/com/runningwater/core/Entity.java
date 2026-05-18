package com.runningwater.core;

public abstract class Entity {
    protected String name;
    protected int health;
    protected int maxHealth;
    protected int level;
    protected float speed;
    protected Vector2 position;

    protected Entity(String name, int maxHealth, int level, float speed) {
        this.name = name;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.level = level;
        this.speed = speed;
        this.position = new Vector2();
    }

    public void TakeDamage(int dmg) {
        this.health = Math.max(0, this.health - dmg);
    }

    public void heal(int amount) {
        this.health = Math.min(maxHealth, this.health + amount);
    }

    public abstract void Move(Vector2 dir);

    public boolean isAlive() { return health > 0; }

    public String getName() { return name; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getLevel() { return level; }
    public float getSpeed() { return speed; }
    public Vector2 getPosition() { return position; }
}
