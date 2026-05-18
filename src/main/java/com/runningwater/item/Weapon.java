package com.runningwater.item;

import com.runningwater.core.Entity;
import com.runningwater.core.Item;

// Senjata yang dapat di-equip player.
public class Weapon extends Item {
    private int attackBonus;
    private String weaponType;
    private boolean isEquipped;

    public Weapon(String itemId, String itemName, String description,
                  int attackBonus, String weaponType) {
        super(itemId, itemName, description);
        this.attackBonus = attackBonus;
        this.weaponType = weaponType;
        this.isEquipped = false;
    }

    public void Equip()   { this.isEquipped = true; }
    public void Unequip() { this.isEquipped = false; }

    @Override
    public void Use() {
        Equip();
    }

    @Override
    public void Use(Entity target) {
        target.TakeDamage(attackBonus);
    }

    @Override
    public String GetInfo() {
        String info = super.GetInfo() + " [Atk +" + attackBonus + "]";
        if (isEquipped) info += " [EQUIPPED]";
        return info;
    }

    public int getAttackBonus() { return attackBonus; }
    public String getWeaponType() { return weaponType; }
    public boolean isEquipped() { return isEquipped; }
}
