package com.runningwater.core;

/**
 * Abstract class untuk seluruh item yang dapat dimiliki/digunakan.
 * Subclass: Weapon, Armour, Tools, Consumables.
 */
public abstract class Item {
    protected String itemId;
    protected String itemName;
    protected String description;

    protected Item(String itemId, String itemName, String description) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.description = description;
    }

    /** Menggunakan item tanpa target spesifik (efek pada self). */
    public abstract void Use();

    /** Menggunakan item dengan target tertentu — overloading dari Use(). */
    public abstract void Use(Entity target);

    public String GetInfo() {
        return itemName + " — " + description;
    }

    public String getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public String getDescription() { return description; }

    /** Override toString agar enak ditampilkan di ChoiceDialog JavaFX. */
    @Override
    public String toString() {
        return GetInfo();
    }
}
