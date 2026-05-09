package com.runningwater.entity;

import com.runningwater.core.Entity;
import com.runningwater.core.Vector2;

// Lawan yang dikendalikan sistem (AI sederhana).

public class Enemies extends Entity {
    private float aggroRange;
    private int damage;
    private String enemyType;

    public Enemies(String name, int maxHealth, int level, int damage, String enemyType) {
        super(name, maxHealth, level, 3.0f);
        this.damage = damage;
        this.aggroRange = 5.0f;
        this.enemyType = enemyType;
    }

    /** Menyerang target — mengurangi health-nya sebesar damage Enemies ini. */
    public void Attack(Entity target) {
        target.TakeDamage(damage);
    }

    /** AI sederhana: bergerak ke kanan secara terus-menerus. */
    public void Patrol() {
        Move(new Vector2(1f, 0f));
    }

    @Override
    public void Move(Vector2 dir) {
        position.setX(position.getX() + dir.getX() * speed);
        position.setY(position.getY() + dir.getY() * speed);
    }

    @Override
    public void TakeDamage(int dmg) {
        // Override: Enemies punya pengurangan damage natural berbasis level
        int reduction = level / 2;
        int actual = Math.max(1, dmg - reduction);
        super.TakeDamage(actual);
    }

    public int getDamage() { return damage; }
    public float getAggroRange() { return aggroRange; }
    public String getEnemyType() { return enemyType; }
}
