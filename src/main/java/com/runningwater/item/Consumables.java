package com.runningwater.item;

import com.runningwater.core.Entity;
import com.runningwater.core.Item;

/**
 * Item sekali pakai (potion, scroll, dst).
 */
public class Consumables extends Item {
    private String effectType; // "heal" atau "damage"
    private int effectValue;
    private boolean isUsed;

    public Consumables(String itemId, String itemName, String description,
                       String effectType, int effectValue) {
        super(itemId, itemName, description);
        this.effectType = effectType;
        this.effectValue = effectValue;
        this.isUsed = false;
    }

    @Override
    public void Use() {
        // Tanpa target — tidak ada efek; hanya tandai sebagai sudah digunakan.
        if (!isUsed) {
            this.isUsed = true;
        }
    }

    @Override
    public void Use(Entity target) {
        if (isUsed) return;
        if ("heal".equals(effectType)) {
            target.heal(effectValue);
        } else if ("damage".equals(effectType)) {
            target.TakeDamage(effectValue);
        }
        this.isUsed = true;
    }

    @Override
    public String GetInfo() {
        if (isUsed) {
            return super.GetInfo() + " [USED]";
        }
        return super.GetInfo() + " [" + effectType + " " + effectValue + "]";
    }

    public boolean isUsed() { return isUsed; }
    public String getEffectType() { return effectType; }
    public int getEffectValue() { return effectValue; }
}
