package com.runningwater.item;

import com.runningwater.core.Entity;
import com.runningwater.core.Item;

// Item yang bisa dipakai berkali-kali; memiliki durability.
public class Tools extends Item {
    private String effectType;
    private int durability;

    public Tools(String itemId, String itemName, String description,
                 String effectType, int durability) {
        super(itemId, itemName, description);
        this.effectType = effectType;
        this.durability = durability;
    }

    @Override
    public void Use() {
        if (durability > 0) durability--;
    }

    @Override
    public void Use(Entity target) {
        if (durability <= 0) return;
        durability--;
        target.TakeDamage(5);
    }

    @Override
    public String GetInfo() {
        return super.GetInfo() + " [Durability " + durability + "]";
    }

    public int getDurability() { return durability; }
    public String getEffectType() { return effectType; }
}
