package com.runningwater.screen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.runningwater.core.Item;
import com.runningwater.engine.AnimationPlayer;
import com.runningwater.engine.AnimationPlayer.AnimState;
import com.runningwater.entity.Enemies;
import com.runningwater.entity.PlayChar;
import com.runningwater.item.Armour;
import com.runningwater.item.Consumables;
import com.runningwater.item.Weapon;
import com.runningwater.manager.GameManager;
import com.runningwater.system.CombatSystem;
import com.runningwater.system.InputHandler;
import com.runningwater.system.RewardSystem;
import com.runningwater.system.ScoringSystem;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

// Layar Combat visual turn-based.
public class CombatScreen implements Screen {

    private static final double W = 900;
    private static final double H = 600;

    private static final String[] ACTIONS = {"Attack", "Ability", "Use Item", "Flee"};
    private int selectedAction = 0;

    private final AnimationPlayer playerAnim = new AnimationPlayer();
    private final List<AnimationPlayer> enemyAnims = new ArrayList<>();

    private boolean waitingForInput = true;
    private double  screenShake     = 0;
    private double  time            = 0;

    private final List<String> logLines = new ArrayList<>();
    private final Set<Enemies> rewarded = new HashSet<>();

    private boolean abilityUsed = false;
    private int selectedEnemyIndex = 0;

    private boolean pickingItem = false;
    private int     itemIndex   = 0;

    public CombatScreen() {
        GameManager gm = GameManager.getInstance();
        for (int i = 0; i < gm.getCombatSystem().getCurrentEnemies().size(); i++) {
            enemyAnims.add(new AnimationPlayer());
        }
        logLines.addAll(gm.getCombatSystem().getCombatLog());
        trimLog();
    }

    @Override
    public void handleInput(InputHandler input) {
        if (!waitingForInput) return;

        if (pickingItem) {
            handleItemPickerInput(input);
            return;
        }

        if (input.isJustPressed(KeyCode.LEFT) && selectedAction == 0) {
            moveSelectedEnemy(-1);
        }
        if (input.isJustPressed(KeyCode.RIGHT) && selectedAction == 0) {
            moveSelectedEnemy(1);
        }
        if (input.isJustPressed(KeyCode.UP)) {
            selectedAction = (selectedAction - 1 + ACTIONS.length) % ACTIONS.length;
        }
        if (input.isJustPressed(KeyCode.DOWN)) {
            selectedAction = (selectedAction + 1) % ACTIONS.length;
        }
        if (input.isJustPressed(KeyCode.ENTER) || input.isJustPressed(KeyCode.SPACE)) {
            executeAction();
        }
    }

    private void handleItemPickerInput(InputHandler input) {
        List<Item> inv = GameManager.getInstance().getPlayer().getInventory();
        if (inv.isEmpty()) { pickingItem = false; return; }

        if (input.isJustPressed(KeyCode.UP))   itemIndex = (itemIndex - 1 + inv.size()) % inv.size();
        if (input.isJustPressed(KeyCode.DOWN)) itemIndex = (itemIndex + 1) % inv.size();
        if (input.isJustPressed(KeyCode.ENTER) || input.isJustPressed(KeyCode.SPACE)) {
            applyItem(inv.get(itemIndex));
            pickingItem = false;
        }
        if (input.isJustPressed(KeyCode.ESCAPE)) {
            pickingItem = false;
        }
    }

    private void executeAction() {
        GameManager   gm     = GameManager.getInstance();
        PlayChar      player = gm.getPlayer();
        CombatSystem  combat = gm.getCombatSystem();
        ScoringSystem score  = gm.getScoringSystem();

        switch (selectedAction) {
            case 0: // Attack
                waitingForInput = false;
                playerAnim.setState(AnimState.ATTACK);
                screenShake = 0.25;
                combat.PlayerAttack(selectedEnemyIndex);
                abilityUsed = false; // reset ability cooldown after attacking
                score.AddScore(30);
                checkEnemyKills(gm);
                syncLog(combat);
                javafx.animation.PauseTransition pt =
                    new javafx.animation.PauseTransition(javafx.util.Duration.millis(600));
                pt.setOnFinished(e -> {
                    if (player.isAlive() && !combat.allEnemiesDefeated()) {
                        combat.EnemyTurn();
                        triggerEnemyHurtAnims(combat);
                        syncLog(combat);
                        if (!player.isAlive()) playerAnim.setState(AnimState.DEAD);
                    }
                    combat.ProcessTurn();
                    waitingForInput = true;
                    checkEndConditions(gm);
                });
                pt.play();
                break;

            case 1: // Ability (heal)
                if (abilityUsed) {
                    addLog("✦  Ability sudah digunakan!");
                } else {
                    player.UseAbility();
                    abilityUsed = true;
                    score.AddScore(10, 1.0f);
                    addLog("✦  " + player.getName() + " menggunakan Ability! +30 HP");
                }
                break;

            case 2: // Item
                List<Item> inv = gm.getPlayer().getInventory();
                if (inv.isEmpty()) {
                    addLog("Inventory kosong!");
                } else {
                    pickingItem = true;
                    itemIndex   = 0;
                }
                break;

            case 3: // Flee
                addLog("Kabur dari pertarungan...");
                combat.EndCombat();
                gm.getUIManager().ShowMenu(new GameScreen(), true);
                break;
        }
    }

    private void applyItem(Item item) {
        GameManager  gm     = GameManager.getInstance();
        PlayChar     player = gm.getPlayer();
        CombatSystem combat = gm.getCombatSystem();

        if (item instanceof Weapon) {
            player.equipWeapon((Weapon) item);
            item.Use();
            addLog("⚔  Equipped: " + item.getItemName());
        } else if (item instanceof Armour) {
            player.equipArmour((Armour) item);
            item.Use();
            addLog("🛡  Equipped: " + item.getItemName());
        } else if (item instanceof Consumables) {
            Consumables c = (Consumables) item;
            if (!c.isUsed()) {
                c.Use(player);
                player.removeItem(item);
                addLog("🧪  " + item.getItemName() + " digunakan!");
            }
        } else {
            item.Use(player);
            addLog("🔧  " + item.getItemName() + " digunakan!");
        }

        checkEndConditions(gm);
    }

    private void checkEnemyKills(GameManager gm) {
        for (Enemies e : gm.getCombatSystem().getCurrentEnemies()) {
            if (!e.isAlive() && !rewarded.contains(e)) {
                rewarded.add(e);
                gm.getScoringSystem().AddScore(50, 1.5f);
                gm.getPlayer().GainXP(40);
                int idx = gm.getCombatSystem().getCurrentEnemies().indexOf(e);
                if (idx >= 0 && idx < enemyAnims.size()) {
                    enemyAnims.get(idx).setState(AnimState.DEAD);
                }
            }
        }
    }

    private void triggerEnemyHurtAnims(CombatSystem combat) {
        if (!GameManager.getInstance().getPlayer().isAlive()) {
            playerAnim.setState(AnimState.DEAD);
        } else {
            playerAnim.setState(AnimState.HURT);
        }
    }

    private void moveSelectedEnemy(int delta) {
        CombatSystem combat = GameManager.getInstance().getCombatSystem();
        List<Enemies> enemies = combat.getCurrentEnemies();
        if (enemies.isEmpty()) return;

        int n = enemies.size();
        int start = (selectedEnemyIndex % n + n) % n;
        for (int i = 1; i <= n; i++) {
            int next = (start + delta * i + n) % n;
            if (enemies.get(next).isAlive()) {
                selectedEnemyIndex = next;
                return;
            }
        }
    }

    private void ensureSelectedEnemyAlive() {
        CombatSystem combat = GameManager.getInstance().getCombatSystem();
        List<Enemies> enemies = combat.getCurrentEnemies();
        if (enemies.isEmpty()) return;
        if (selectedEnemyIndex < 0 || selectedEnemyIndex >= enemies.size()) {
            selectedEnemyIndex = 0;
        }
        if (!enemies.get(selectedEnemyIndex).isAlive()) {
            moveSelectedEnemy(1);
        }
    }

    private void checkEndConditions(GameManager gm) {
        if (!gm.getPlayer().isAlive()) {
            javafx.animation.PauseTransition pt =
                new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
            pt.setOnFinished(e -> gm.getUIManager().ShowMenu(new GameOverScreen(), true));
            pt.play();
        } else if (gm.getCombatSystem().allEnemiesDefeated()) {
            grantRewards(gm);
        }
    }

    private void grantRewards(GameManager gm) {
        RewardSystem reward = gm.getRewardSystem();
        PlayChar     player = gm.getPlayer();

        Item loot  = reward.GrantReward();
        Item bonus = reward.GrantReward(0.95f);

        if (loot  != null) { player.addItem(loot);  addLog("🎁 Loot: "  + loot.GetInfo()); }
        if (bonus != null) { player.addItem(bonus); addLog("⭐ Bonus: " + bonus.GetInfo()); }

        gm.getCombatSystem().EndCombat();
        gm.getActiveStage().markCleared();

        int delta = gm.getScoringSystem().GetScore() - player.getScore();
        if (delta > 0) player.addScore(delta);

        javafx.animation.PauseTransition pt =
            new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.8));
        pt.setOnFinished(e -> gm.getUIManager().ShowMenu(new GameScreen(), true));
        pt.play();
        waitingForInput = false;
    }

    private void syncLog(CombatSystem combat) {
        List<String> all = combat.getCombatLog();
        if (!all.isEmpty()) {
            String last = all.get(all.size() - 1);
            if (logLines.isEmpty() || !logLines.get(logLines.size() - 1).equals(last)) {
                logLines.add(last);
                trimLog();
            }
        }
    }

    private void addLog(String msg) {
        logLines.add(msg);
        trimLog();
    }

    private void trimLog() {
        while (logLines.size() > 6) logLines.remove(0);
    }



    @Override
    public void update(double dt) {
        time += dt;
        if (screenShake > 0) screenShake = Math.max(0, screenShake - dt * 3);

        playerAnim.update(dt);
        for (AnimationPlayer ap : enemyAnims) ap.update(dt);
    }



    @Override
    public void render(GraphicsContext gc) {
        GameManager  gm     = GameManager.getInstance();
        PlayChar     player = gm.getPlayer();
        CombatSystem combat = gm.getCombatSystem();


        double shakeX = 0, shakeY = 0;
        if (screenShake > 0) {
            shakeX = (Math.random() - 0.5) * screenShake * 16;
            shakeY = (Math.random() - 0.5) * screenShake * 10;
        }
        gc.save();
        gc.translate(shakeX, shakeY);


        drawBackground(gc);


        drawGround(gc);


        List<Enemies> enemies = combat.getCurrentEnemies();
        int aliveCount = 0;
        for (Enemies e : enemies) if (e.isAlive()) aliveCount++;

        double enemyStartX = 560 + (enemies.size() == 1 ? 50 : 0);
        ensureSelectedEnemyAlive();
        for (int i = 0; i < enemies.size(); i++) {
            Enemies e = enemies.get(i);
            double ex = enemyStartX + i * 110;
            double ey = 230;
            AnimationPlayer anim = enemyAnims.size() > i ? enemyAnims.get(i) : new AnimationPlayer();
            Color enemyCol = getEnemyColor(e.getEnemyType());
            anim.render(gc, ex, ey, 72, 80, enemyCol, true);
            drawEntityHPBar(gc, ex - 36, ey + 50, 72, e.getHealth(), e.getMaxHealth(),
                Color.web("#cc2222"), e.getName());
            if (waitingForInput && selectedAction == 0 && i == selectedEnemyIndex && e.isAlive()) {
                drawEnemySelection(gc, ex, ey);
            }
        }


        playerAnim.render(gc, 210, 240, 80, 90, Color.web("#3a7ad5"), false);
        drawEntityHPBar(gc, 170, 295, 80, player.getHealth(), player.getMaxHealth(),
            Color.web("#22aa55"), player.getName());

        gc.restore();


        drawCombatLog(gc);


        if (waitingForInput && !pickingItem) {
            drawActionMenu(gc);
        }


        if (pickingItem) {
            drawItemPicker(gc, player);
        }


        drawTopHUD(gc, gm, combat);
    }

    private void drawBackground(GraphicsContext gc) {
        // Langit malam
        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#060c18")),
            new Stop(0.6, Color.web("#0d1a30")),
            new Stop(1, Color.web("#1a0a0a"))));
        gc.fillRect(0, 0, W, H);

        // Bintang
        gc.setFill(Color.WHITE);
        for (int i = 0; i < 60; i++) {
            double sx = (i * 137.5 + 17) % W;
            double sy = (i * 79.3 + 5) % (H * 0.55);
            double blink = 0.4 + 0.6 * Math.abs(Math.sin(time * 1.2 + i));
            gc.setFill(Color.web("#ffffff", blink));
            gc.fillOval(sx, sy, 2, 2);
        }

        // Bulan
        gc.setFill(Color.web("#e8d8a0"));
        gc.fillOval(W - 120, 30, 70, 70);
        gc.setFill(Color.web("#060c18"));
        gc.fillOval(W - 105, 25, 65, 65);

        // Gunung siluet
        gc.setFill(Color.web("#080e1c"));
        double[] mx = {0, 150, 280, 420, 550, 680, 780, 900, 900, 0};
        double[] my = {300, 150, 220, 120, 200, 140, 220, 160, 310, 310};
        gc.fillPolygon(mx, my, mx.length);
    }

    private void drawGround(GraphicsContext gc) {
        // Platform tanah
        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#2a1a0a")),
            new Stop(1, Color.web("#1a0a00"))));
        gc.fillRect(0, 310, W, H - 310);

        // Garis rumput
        gc.setFill(Color.web("#2a4a1a"));
        gc.fillRect(0, 310, W, 14);
        gc.setFill(Color.web("#3a6a2a"));
        gc.fillRect(0, 310, W, 6);
    }

    private void drawEntityHPBar(GraphicsContext gc, double x, double y, double w,
                                  int hp, int maxHp, Color fill, String name) {
        double frac = maxHp > 0 ? (double) hp / maxHp : 0;
        gc.setFill(Color.web("#1a0000"));
        gc.fillRoundRect(x, y, w, 10, 4, 4);
        gc.setFill(fill);
        gc.fillRoundRect(x, y, w * frac, 10, 4, 4);
        gc.setStroke(Color.web("#444444"));
        gc.setLineWidth(0.5);
        gc.strokeRoundRect(x, y, w, 10, 4, 4);
        gc.setFont(Font.font("Helvetica Neue", FontWeight.NORMAL, 10));
        gc.setFill(Color.web("#cccccc"));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(hp + "/" + maxHp, x + w / 2, y + 9);
    }

    private Color getEnemyColor(String type) {
        switch (type) {
            case "slime":     return Color.web("#3aaa3a");
            case "humanoid":  return Color.web("#8a6a3a");
            case "beast":     return Color.web("#7a4a2a");
            case "construct": return Color.web("#6a6a8a");
            case "spirit":    return Color.web("#8a3a8a");
            default:          return Color.web("#aa3a3a");
        }
    }

    private void drawCombatLog(GraphicsContext gc) {
        double logX = 14, logY = 330, logW = W - 28, logH = 95;
        gc.setFill(Color.web("#000000", 0.6));
        gc.fillRoundRect(logX, logY, logW, logH, 8, 8);
        gc.setStroke(Color.web("#1a3050"));
        gc.setLineWidth(1);
        gc.strokeRoundRect(logX, logY, logW, logH, 8, 8);

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Menlo", FontWeight.NORMAL, 12));
        for (int i = 0; i < logLines.size(); i++) {
            double alpha = 0.5 + 0.5 * ((double)(i + 1) / logLines.size());
            gc.setFill(Color.web("#b0d0e8", alpha));
            gc.fillText(logLines.get(i), logX + 12, logY + 16 + i * 14);
        }
    }

    private void drawActionMenu(GraphicsContext gc) {
        double menuY = H - 98, spacing = W / ACTIONS.length;
        for (int i = 0; i < ACTIONS.length; i++) {
            boolean sel = (i == selectedAction);
            double bx = i * spacing + 8, bw = spacing - 16, bh = 60;

            gc.setFill(sel ? Color.web("#0d2a40") : Color.web("#07192b"));
            gc.fillRoundRect(bx, menuY, bw, bh, 8, 8);
            gc.setStroke(sel ? Color.web("#4fc3f7") : Color.web("#1a4060"));
            gc.setLineWidth(sel ? 2 : 1);
            gc.strokeRoundRect(bx, menuY, bw, bh, 8, 8);

            // Ikon
            String[] icons = {"⚔", "✦", "🎒", "🏃"};
            gc.setFont(Font.font("Helvetica Neue", FontWeight.BOLD, 18));
            gc.setFill(sel ? Color.web("#4fc3f7") : Color.web("#5a8aaa"));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(icons[i], bx + bw / 2, menuY + 25);

            gc.setFont(Font.font("Helvetica Neue", FontWeight.NORMAL, 12));
            gc.setFill(sel ? Color.web("#e0f0ff") : Color.web("#6a9abb"));
            String actionLabel = ACTIONS[i];
            if (i == 1 && abilityUsed) actionLabel += " (used)";
            gc.fillText(actionLabel, bx + bw / 2, menuY + 46);
        }
        gc.setFont(Font.font("Helvetica Neue", FontWeight.NORMAL, 11));
        gc.setFill(Color.web("#2a4a60"));
        if (selectedAction == 0) {
            gc.fillText("← →  Pilih Musuh   ↑ ↓  Pilih Aksi   ENTER  Konfirmasi", W / 2, H - 8);
        } else {
            gc.fillText("↑ ↓  Pilih     ENTER  Konfirmasi", W / 2, H - 8);
        }
    }

    private void drawEnemySelection(GraphicsContext gc, double ex, double ey) {
        gc.setStroke(Color.web("#5fe8ff"));
        gc.setLineWidth(3);
        gc.strokeRoundRect(ex - 46, ey - 10, 92, 108, 12, 12);
    }

    private void drawItemPicker(GraphicsContext gc, PlayChar player) {
        List<Item> inv = player.getInventory();
        double pw = 400, ph = Math.min(inv.size() * 42 + 50, 300);
        double px = (W - pw) / 2, py = (H - ph) / 2;

        gc.setFill(Color.web("#000000", 0.75));
        gc.fillRect(0, 0, W, H);
        gc.setFill(Color.web("#071828"));
        gc.fillRoundRect(px, py, pw, ph, 12, 12);
        gc.setStroke(Color.web("#4fc3f7"));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(px, py, pw, ph, 12, 12);

        gc.setFont(Font.font("Helvetica Neue", FontWeight.BOLD, 15));
        gc.setFill(Color.web("#4fc3f7"));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Pilih Item", W / 2, py + 26);

        for (int i = 0; i < inv.size(); i++) {
            boolean sel = (i == itemIndex);
            double iy = py + 40 + i * 40;
            if (sel) {
                gc.setFill(Color.web("#0d2a40"));
                gc.fillRoundRect(px + 8, iy + 2, pw - 16, 34, 6, 6);
            }
            gc.setFont(Font.font("Helvetica Neue", sel ? FontWeight.BOLD : FontWeight.NORMAL, 13));
            gc.setFill(sel ? Color.web("#4fc3f7") : Color.web("#88aabb"));
            gc.fillText(inv.get(i).GetInfo(), W / 2, iy + 22);
        }
        gc.setFont(Font.font("Helvetica Neue", FontWeight.NORMAL, 11));
        gc.setFill(Color.web("#2a4a60"));
        gc.fillText("↑ ↓  Pilih   ENTER  Gunakan   ESC  Batal", W / 2, py + ph - 10);
    }

    private void drawTopHUD(GraphicsContext gc, GameManager gm, CombatSystem combat) {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Helvetica Neue", FontWeight.BOLD, 14));
        gc.setFill(Color.web("#ffffff", 0.7));
        gc.fillText("Ronde " + combat.getRoundCount(), W / 2, 22);

        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFont(Font.font("Helvetica Neue", FontWeight.BOLD, 13));
        gc.setFill(Color.web("#f0d060"));
        gc.fillText("Score: " + gm.getScoringSystem().GetScore(), W - 16, 22);
    }
}
