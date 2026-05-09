package com.runningwater.screen;

import com.runningwater.entity.PlayChar;
import com.runningwater.game.Stages;
import com.runningwater.manager.GameManager;
import com.runningwater.system.InputHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Layar Peta Stage — menggambar tile map sederhana dan status player.
 * Menggantikan game_screen.fxml + GameController.
 */
public class GameScreen implements Screen {

    private static final double W = 900;
    private static final double H = 600;

    private double time       = 0;
    private String message    = "";
    private double msgTimer   = 0;

    // Player sprite sederhana — berjalan di atas peta
    private double playerX    = 200;
    private double playerY    = 340;
    private double walkOffset = 0;

    // Warna tile per stage
    private static final Color[][] STAGE_PALETTE = {
        { Color.web("#1a3a1a"), Color.web("#2a5a2a"), Color.web("#4a8a4a") }, // stage 1: hijau
        { Color.web("#3a2a0a"), Color.web("#6a4a1a"), Color.web("#9a7a3a") }, // stage 2: coklat
        { Color.web("#1a1a3a"), Color.web("#2a2a6a"), Color.web("#4a4a9a") }, // stage 3: biru gelap
    };

    @Override
    public void handleInput(InputHandler input) {
        GameManager gm = GameManager.getInstance();

        if (input.isJustPressed(KeyCode.ENTER) || input.isJustPressed(KeyCode.Z)) {
            handleEngageCombat(gm);
        }
        if (input.isJustPressed(KeyCode.S)) {
            handleSave(gm);
        }
        if (input.isJustPressed(KeyCode.ESCAPE)) {
            gm.getUIManager().ShowMenu(new MainMenuScreen(), true);
        }
    }

    private void handleEngageCombat(GameManager gm) {
        Stages stage = gm.getActiveStage();
        if (stage.isAllEnemiesDefeated()) {
            // Semua musuh mati — naik stage
            gm.advanceToNextStage();
            if ("VICTORY".equals(gm.getGameState())) {
                gm.getUIManager().ShowMenu(new GameOverScreen(), true);
            } else {
                showMessage("Memasuki " + gm.getActiveStage().getStageName() + "!");
            }
        } else {
            gm.getCombatSystem().StartCombat(gm.getPlayer(), stage.getEnemyList());
            gm.getUIManager().ShowMenu(new CombatScreen(), true);
        }
    }

    private void handleSave(GameManager gm) {
        // Sinkronkan score
        int delta = gm.getScoringSystem().GetScore() - gm.getPlayer().getScore();
        if (delta > 0) gm.getPlayer().addScore(delta);
        boolean ok = gm.save();
        showMessage(ok ? "Game Tersimpan!" : "Gagal menyimpan!");
    }

    private void showMessage(String msg) {
        message  = msg;
        msgTimer = 2.5;
    }

    @Override
    public void update(double dt) {
        time += dt;
        walkOffset = Math.sin(time * 3.5) * 5;
        if (msgTimer > 0) msgTimer -= dt;
    }

    @Override
    public void render(GraphicsContext gc) {
        GameManager gm    = GameManager.getInstance();
        PlayChar    player = gm.getPlayer();
        Stages      stage  = gm.getActiveStage();
        int         si     = Math.min(stage.getStageLevel() - 1, STAGE_PALETTE.length - 1);
        Color[]     pal    = STAGE_PALETTE[si];

        // ── Background ───────────────────────────────────────────────────────
        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, pal[0].darker()), new Stop(1, pal[0])));
        gc.fillRect(0, 0, W, H);

        // ── Tile map ─────────────────────────────────────────────────────────
        drawTileMap(gc, pal);

        // ── Musuh (indikator di peta) ─────────────────────────────────────────
        int alive = 0;
        for (var e : stage.getEnemyList()) if (e.isAlive()) alive++;
        double ex = 600, ey = 310;
        for (int i = 0; i < alive; i++) {
            double ox = ex + i * 55;
            drawEnemyMarker(gc, ox, ey, stage.getEnemyList().get(i).getName());
        }

        // ── Player sprite ──────────────────────────────────────────────────────
        drawPlayerSprite(gc, playerX, playerY + walkOffset);

        // ── HUD ───────────────────────────────────────────────────────────────
        drawHUD(gc, player, stage, gm);

        // ── Stage title ───────────────────────────────────────────────────────
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Helvetica Neue", FontWeight.BOLD, 18));
        gc.setFill(Color.web("#c8e8f8", 0.85));
        gc.fillText("Stage " + stage.getStageLevel() + " — " + stage.getStageName(), W / 2, 38);

        // ── Pesan sementara ───────────────────────────────────────────────────
        if (msgTimer > 0) {
            double alpha = Math.min(1.0, msgTimer);
            gc.setFill(Color.web("#000000", 0.55 * alpha));
            gc.fillRoundRect(W / 2 - 180, H / 2 - 28, 360, 50, 10, 10);
            gc.setFont(Font.font("Helvetica Neue", FontWeight.BOLD, 18));
            gc.setFill(Color.web("#4fc3f7", alpha));
            gc.fillText(message, W / 2, H / 2 + 6);
        }

        // ── Hint ──────────────────────────────────────────────────────────────
        gc.setFont(Font.font("Helvetica Neue", FontWeight.NORMAL, 12));
        gc.setFill(Color.web("#2a4a60"));
        String hint = alive > 0 ? "ENTER = Masuk Combat   S = Save   ESC = Menu"
                                : "ENTER = Lanjut ke Stage berikutnya   S = Save";
        gc.fillText(hint, W / 2, H - 14);
    }

    private void drawTileMap(GraphicsContext gc, Color[] pal) {
        int tileW = 60, tileH = 32;
        int cols = 15, rows = 4;
        int startY = 370;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double tx = c * tileW;
                double ty = startY + r * tileH;
                // Isometrik ringan: geser tiap baris
                double isoShift = r * 8;
                Color tileFill = (r == 0) ? pal[2] : (r == 1) ? pal[1] : pal[0];
                gc.setFill(tileFill);
                gc.fillRect(tx + isoShift, ty, tileW - 1, tileH - 1);
                gc.setStroke(tileFill.darker());
                gc.setLineWidth(0.5);
                gc.strokeRect(tx + isoShift, ty, tileW - 1, tileH - 1);
            }
        }
        // Dekorasi pohon/batu sederhana
        drawDecoration(gc, pal[2]);
    }

    private void drawDecoration(GraphicsContext gc, Color leafCol) {
        double[] treeX = {80, 320, 750, 840};
        double[] treeY = {340, 345, 342, 338};
        for (int i = 0; i < treeX.length; i++) {
            // Batang
            gc.setFill(Color.web("#4a3010"));
            gc.fillRect(treeX[i] - 5, treeY[i], 10, 30);
            // Daun
            gc.setFill(leafCol);
            gc.fillOval(treeX[i] - 20, treeY[i] - 28, 40, 36);
            gc.setFill(leafCol.brighter());
            gc.fillOval(treeX[i] - 14, treeY[i] - 34, 28, 26);
        }
    }

    private void drawPlayerSprite(GraphicsContext gc, double x, double y) {
        // Bayangan
        gc.setFill(Color.web("#000000", 0.3));
        gc.fillOval(x - 20, y + 32, 40, 12);
        // Kaki
        gc.setFill(Color.web("#1a3a6a"));
        gc.fillRoundRect(x - 14, y + 22, 10, 18, 4, 4);
        gc.fillRoundRect(x + 4,  y + 22, 10, 18, 4, 4);
        // Badan
        gc.setFill(Color.web("#2a6abf"));
        gc.fillRoundRect(x - 16, y + 4, 32, 22, 8, 8);
        // Kepala
        gc.setFill(Color.web("#f5c890"));
        gc.fillOval(x - 14, y - 16, 28, 26);
        // Mata
        gc.setFill(Color.web("#1a1a2a"));
        gc.fillOval(x + 2, y - 9, 6, 6);
        // Senjata sederhana
        gc.setFill(Color.web("#aaaaaa"));
        gc.fillRect(x + 16, y + 6, 4, 18);
        gc.setFill(Color.web("#cccccc"));
        gc.fillRect(x + 12, y + 4, 12, 5);
    }

    private void drawEnemyMarker(GraphicsContext gc, double x, double y, String name) {
        // Bayangan
        gc.setFill(Color.web("#000000", 0.25));
        gc.fillOval(x - 18, y + 28, 36, 10);
        // Tubuh musuh
        gc.setFill(Color.web("#8b1a1a"));
        gc.fillOval(x - 16, y - 10, 32, 44);
        // Mata
        gc.setFill(Color.web("#ff4444"));
        gc.fillOval(x - 8, y, 8, 8);
        gc.fillOval(x + 2,  y, 8, 8);
        // Nama
        gc.setFont(Font.font("Helvetica Neue", FontWeight.NORMAL, 10));
        gc.setFill(Color.web("#ff8888"));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(name, x, y + 46);
    }

    private void drawHUD(GraphicsContext gc, PlayChar player, Stages stage, GameManager gm) {
        // Panel HUD kiri bawah
        double panX = 14, panY = H - 115, panW = 270, panH = 100;
        gc.setFill(Color.web("#000000", 0.55));
        gc.fillRoundRect(panX, panY, panW, panH, 10, 10);
        gc.setStroke(Color.web("#1a4060"));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(panX, panY, panW, panH, 10, 10);

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Helvetica Neue", FontWeight.BOLD, 14));
        gc.setFill(Color.web("#e0e9f0"));
        gc.fillText(player.getName() + "  Lv." + player.getLevel(), panX + 12, panY + 20);

        // HP bar
        double barW = panW - 24, barH = 14;
        double barX = panX + 12, barY = panY + 30;
        gc.setFill(Color.web("#1a0000"));
        gc.fillRoundRect(barX, barY, barW, barH, 4, 4);
        double hpFrac = (double) player.getHealth() / player.getMaxHealth();
        Color hpCol = hpFrac > 0.5 ? Color.web("#2ecc71") : hpFrac > 0.25 ? Color.web("#f39c12") : Color.web("#e74c3c");
        gc.setFill(hpCol);
        gc.fillRoundRect(barX, barY, barW * hpFrac, barH, 4, 4);
        gc.setFont(Font.font("Helvetica Neue", FontWeight.NORMAL, 11));
        gc.setFill(Color.web("#e0e9f0"));
        gc.fillText(player.getHealth() + " / " + player.getMaxHealth() + " HP", barX + 4, barY + 11);

        // XP bar
        double xpFrac = (double) player.getExperience() / (player.getLevel() * 100.0);
        double xpY = barY + barH + 6;
        gc.setFill(Color.web("#001a2a"));
        gc.fillRoundRect(barX, xpY, barW, 8, 3, 3);
        gc.setFill(Color.web("#3498db"));
        gc.fillRoundRect(barX, xpY, barW * xpFrac, 8, 3, 3);
        gc.setFill(Color.web("#7ab8d8"));
        gc.setFont(Font.font("Helvetica Neue", FontWeight.NORMAL, 10));
        gc.fillText("XP " + player.getExperience() + " / " + (player.getLevel() * 100), barX + 2, xpY + 7);

        // Score
        gc.setFont(Font.font("Helvetica Neue", FontWeight.BOLD, 13));
        gc.setFill(Color.web("#f0d060"));
        gc.fillText("Score: " + gm.getScoringSystem().GetScore(), panX + 12, panY + 84);

        // Musuh tersisa
        long alive = stage.getEnemyList().stream().filter(e -> e.isAlive()).count();
        gc.setFill(alive > 0 ? Color.web("#ff7777") : Color.web("#77ff77"));
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText(alive > 0 ? "Musuh: " + alive : "Stage Clear!", panX + panW - 10, panY + 84);
    }
}
