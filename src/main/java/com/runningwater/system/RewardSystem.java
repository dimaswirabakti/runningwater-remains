package com.runningwater.system;

import com.runningwater.core.GameSystem;
import com.runningwater.core.Item;
import com.runningwater.item.Armour;
import com.runningwater.item.Consumables;
import com.runningwater.item.Weapon;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Mengendalikan loot yang diterima player setelah memenangkan combat.
 */
public class RewardSystem extends GameSystem {
    private final List<Item> rewardPool;
    private final Random random;
    private float dropRate;

    public RewardSystem() {
        super("RewardSystem");
        this.rewardPool = new ArrayList<>();
        this.dropRate = 0.7f;
        this.random = new Random();
    }

    @Override
    public void Initialize() {
        this.isActive = true;
        rewardPool.clear();
        rewardPool.add(new Weapon("w_dagger",  "Dagger",       "A sharp, small blade.",   5,  "blade"));
        rewardPool.add(new Weapon("w_sword",   "Iron Sword",   "A reliable iron sword.",  10, "sword"));
        rewardPool.add(new Weapon("w_axe",     "Battle Axe",   "Heavy and brutal.",       14, "axe"));
        rewardPool.add(new Armour("a_leather", "Leather Armour","Light protection.",       3,  "light"));
        rewardPool.add(new Armour("a_chain",   "Chain Mail",   "Solid metal armour.",     6,  "medium"));
        rewardPool.add(new Consumables("c_potion",     "Healing Potion", "Restores 30 HP.",  "heal", 30));
        rewardPool.add(new Consumables("c_bigpotion",  "Greater Potion", "Restores 60 HP.",  "heal", 60));
    }

    @Override
    public void UpdateSystem() {}

    /** OVERLOADING — versi 1: pakai dropRate default. */
    public Item GrantReward() {
        return GrantReward(this.dropRate);
    }

    /** OVERLOADING — versi 2: terima bonusRate (misal saat boss kill). */
    public Item GrantReward(float bonusRate) {
        if (random.nextFloat() <= bonusRate) {
            return RollLoot();
        }
        return null;
    }

    /** Memilih item acak dari rewardPool. */
    public Item RollLoot() {
        if (rewardPool.isEmpty()) return null;
        return rewardPool.get(random.nextInt(rewardPool.size()));
    }

    public float getDropRate() { return dropRate; }
    public void setDropRate(float dropRate) { this.dropRate = dropRate; }
}
