package com.runningwater.system;

import java.util.ArrayList;
import java.util.List;

import com.runningwater.core.Entity;
import com.runningwater.core.GameSystem;
import com.runningwater.entity.Enemies;
import com.runningwater.entity.PlayChar;

// Mengelola alur combat.
public class CombatSystem extends GameSystem {
    private final List<Entity> turnOrder;
    private final List<String> combatLog;
    private List<Enemies> currentEnemies;
    private PlayChar player;
    private int roundCount;

    public CombatSystem() {
        super("CombatSystem");
        this.turnOrder = new ArrayList<>();
        this.combatLog = new ArrayList<>();
        this.currentEnemies = new ArrayList<>();
    }

    @Override
    public void Initialize() {
        this.isActive = true;
        this.roundCount = 0;
    }

    @Override
    public void UpdateSystem() {
        // Combat berbasis event (per klik tombol Attack).
    }

    public void StartCombat(PlayChar player, List<Enemies> enemies) {
        this.player = player;
        this.currentEnemies = enemies;
        this.roundCount = 0;
        this.combatLog.clear();
        this.turnOrder.clear();
        this.turnOrder.add(player);
        this.turnOrder.addAll(enemies);
        log("=== Combat dimulai melawan " + enemies.size() + " musuh ===");
    }

    // Player menyerang target pertama yang masih hidup.
    public void PlayerAttack() {
        PlayerAttack(0);
    }

    // Player menyerang target terpilih, melewati enemy yang sudah mati.
    public void PlayerAttack(int targetIndex) {
        if (currentEnemies.isEmpty()) return;
        Enemies target = findAliveByIndex(targetIndex);
        if (target == null) return;
        int hpBefore = target.getHealth();
        player.Attack(target);
        int dmg = hpBefore - target.getHealth();
        log(player.getName() + " menyerang " + target.getName() + " (-" + dmg + " HP)");
        if (!target.isAlive()) {
            log("  > " + target.getName() + " telah dikalahkan!");
        }
    }

    // Setiap enemy yang hidup menyerang player.
    public void EnemyTurn() {
        for (Enemies e : currentEnemies) {
            if (!e.isAlive()) continue;
            int hpBefore = player.getHealth();
            e.Attack(player);
            int dmg = hpBefore - player.getHealth();
            log(e.getName() + " menyerang " + player.getName() + " (-" + dmg + " HP)");
            if (!player.isAlive()) {
                log("  > " + player.getName() + " telah jatuh!");
                break;
            }
        }
    }

    public void ProcessTurn() {
        roundCount++;
    }

    public void EndCombat() {
        this.isActive = false;
        log("=== Combat berakhir setelah " + roundCount + " ronde ===");
    }

    public boolean allEnemiesDefeated() {
        for (Enemies e : currentEnemies) {
            if (e.isAlive()) return false;
        }
        return true;
    }

    private Enemies findFirstAlive() {
        for (Enemies e : currentEnemies) {
            if (e.isAlive()) return e;
        }
        return null;
    }

    private Enemies findAliveByIndex(int targetIndex) {
        if (currentEnemies.isEmpty()) return null;
        int n = currentEnemies.size();
        int start = (targetIndex % n + n) % n;
        for (int i = 0; i < n; i++) {
            int idx = (start + i) % n;
            Enemies enemy = currentEnemies.get(idx);
            if (enemy.isAlive()) return enemy;
        }
        return null;
    }

    private void log(String msg) {
        combatLog.add(msg);
    }

    public List<String> getCombatLog() { return combatLog; }
    public List<Enemies> getCurrentEnemies() { return currentEnemies; }
    public int getRoundCount() { return roundCount; }
}
