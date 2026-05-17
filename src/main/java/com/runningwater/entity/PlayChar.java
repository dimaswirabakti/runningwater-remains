package com.runningwater.entity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.runningwater.core.Entity;
import com.runningwater.core.ISavable;
import com.runningwater.core.Item;
import com.runningwater.core.Vector2;
import com.runningwater.item.Armour;
import com.runningwater.item.Weapon;

// Karakter yang dikendalikan player.
public class PlayChar extends Entity implements ISavable {
    private int experience;
    private int score;
    private List<Item> inventory;
    private Weapon equippedWeapon;
    private Armour equippedArmour;

    public PlayChar(String name) {
        super(name, 100, 1, 5.0f);
        this.experience = 0;
        this.score = 0;
        this.inventory = new ArrayList<>();
    }

    /** Tambah XP dan auto level up bila threshold terlampaui. */
    public void GainXP(int xp) {
        this.experience += xp;
        while (this.experience >= getXPThreshold()) {
            this.experience -= getXPThreshold();
            levelUp();
        }
    }

    private int getXPThreshold() {
        return level * 100;
    }

    private void levelUp() {
        this.level++;
        this.maxHealth += 20;
        this.health = this.maxHealth;
    }

    /** Menyerang target. Damage = base 10 + attackBonus weapon. */
    public void Attack(Entity target) {
        int dmg = 10 + (equippedWeapon != null ? equippedWeapon.getAttackBonus() : 0);
        target.TakeDamage(dmg);
    }

    /** Ability sederhana: pulihkan 15 HP. */
    public void UseAbility() {
        heal(15);
    }

    @Override
    public void Move(Vector2 dir) {
        position.setX(position.getX() + dir.getX() * speed);
        position.setY(position.getY() + dir.getY() * speed);
    }

    /** Override TakeDamage untuk menerapkan defense bonus dari armor. */
    @Override
    public void TakeDamage(int dmg) {
        int defense = equippedArmour != null ? equippedArmour.getDefenseBonus() : 0;
        int actual = Math.max(1, dmg - defense);
        super.TakeDamage(actual);
    }

    public void addItem(Item item) { inventory.add(item); }
    public void removeItem(Item item) { inventory.remove(item); }
    public List<Item> getInventory() { return inventory; }

    public void equipWeapon(Weapon w) {
        if (this.equippedWeapon != null) {
            this.equippedWeapon.Unequip();
        }
        this.equippedWeapon = w;
        if (w != null) {
            w.Equip();
        }
    }

    public void equipArmour(Armour a) {
        if (this.equippedArmour != null) {
            this.equippedArmour.Unequip();
        }
        this.equippedArmour = a;
        if (a != null) {
            a.Equip();
        }
    }

    public Weapon getEquippedWeapon() { return equippedWeapon; }
    public Armour getEquippedArmour() { return equippedArmour; }

    public int getExperience() { return experience; }
    public int getScore() { return score; }
    public void addScore(int pts) { this.score += pts; }

    /* ===== ISavable ===== */

    @Override
    public Map<String, Object> getSaveData() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", name);
        data.put("level", level);
        data.put("health", health);
        data.put("maxHealth", maxHealth);
        data.put("experience", experience);
        data.put("score", score);
        return data;
    }

    @Override
    public void loadSaveData(Map<String, Object> data) {
        if (data.get("name") != null)        this.name = data.get("name").toString();
        if (data.get("level") != null)       this.level = ((Number) data.get("level")).intValue();
        if (data.get("maxHealth") != null)   this.maxHealth = ((Number) data.get("maxHealth")).intValue();
        if (data.get("health") != null)      this.health = ((Number) data.get("health")).intValue();
        if (data.get("experience") != null)  this.experience = ((Number) data.get("experience")).intValue();
        if (data.get("score") != null)       this.score = ((Number) data.get("score")).intValue();
    }
}
