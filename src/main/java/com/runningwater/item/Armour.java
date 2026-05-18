package com.runningwater.item;

import com.runningwater.core.Entity;
import com.runningwater.core.Item;

public class Armour extends Item {
    private int defenseBonus;
    private String armourType;
    private boolean isEquipped;

    public Armour(String itemId, String itemName, String description,
                  int defenseBonus, String armourType) {
        super(itemId, itemName, description);
        this.defenseBonus = defenseBonus;
        this.armourType = armourType;
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
        // Armor tidak punya efek pada target lain, overriding dengan no-op.
    }

    @Override
    public String GetInfo() {
        String info = super.GetInfo() + " [Def +" + defenseBonus + "]";
        if (isEquipped) info += " [EQUIPPED]";
        return info;
    }

    public int getDefenseBonus() { return defenseBonus; }
    public String getArmourType() { return armourType; }
    public boolean isEquipped() { return isEquipped; }
}
