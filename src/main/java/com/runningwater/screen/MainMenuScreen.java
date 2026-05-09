package com.runningwater.screen;

import com.runningwater.manager.GameManager;
import com.runningwater.system.InputHandler;
import javafx.application.Platform;
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
 * Layar Main Menu — digambar manual ke Canvas.
 */
public class MainMenuScreen implements Screen {

    private static final double W = 900;
    private static final double H = 600;

    private final String[] items    = {"New Game", "Load Game", "Quit"};
    private int            selected = 0;
    private double         titleBob = 0;
    private double         time     = 0;

    // Warna tema
    private static final Color COL_BG_TOP    = Color.web("#0a1628");
    private static final Color COL_BG_BOT    = Color.web("#061020");
    private static final Color COL_TITLE     = Color.web("#4fc3f7");
    private static final Color COL_SELECTED  = Color.web("#4fc3f7");
    private static final Color COL_NORMAL    = Color.web("#7a9bb5");
    private static final Color COL_SUBTITLE  = Color.web("#3a5a70");
    private static final Color COL_BOX_SEL   = Color.web("#0d2a40");
    private static final Color COL_BOX_NRM   = Color.web("#07192b");
    private static final Color COL_BORDER    = Color.web("#1a4060");

    @Override
    public void handleInput(InputHandler input) {
        if (input.isJustPressed(KeyCode.UP)) {
            selected = (selected - 1 + items.length) % items.length;
        }
        if (input.isJustPressed(KeyCode.DOWN)) {
            selected = (selected + 1) % items.length;
        }
        if (input.isJustPressed(KeyCode.ENTER) || input.isJustPressed(KeyCode.SPACE)) {
            executeSelected();
        }
    }

    @Override
    public void update(double dt) {
        time += dt;
        titleBob = Math.sin(time * 1.4) * 5.0;
    }

    @Override
    public void render(GraphicsContext gc) {
        // Background gradient
        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, COL_BG_TOP), new Stop(1, COL_BG_BOT)));
        gc.fillRect(0, 0, W, H);

        // Dekorasi: garis horizontal samar
        gc.setStroke(Color.web("#0d2a40"));
        gc.setLineWidth(1);
        for (int i = 0; i < H; i += 40) {
            gc.strokeLine(0, i, W, i);
        }

        // Partikel air sederhana (titik-titik bergerak)
        gc.setFill(Color.web("#1a4a6a", 0.4));
        for (int i = 0; i < 20; i++) {
            double px = (i * 137.5 + time * 20) % W;
            double py = (i * 89.3  + time * 30) % H;
            gc.fillOval(px, py, 3, 3);
        }

        // Judul
        gc.setFont(Font.font("Helvetica Neue", FontWeight.BOLD, 52));
        gc.setTextAlign(TextAlignment.CENTER);

        // Shadow judul
        gc.setFill(Color.web("#000000", 0.5));
        gc.fillText("RUNNINGWATER REMAINS", W / 2 + 3, 145 + titleBob + 3);

        gc.setFill(COL_TITLE);
        gc.fillText("RUNNINGWATER REMAINS", W / 2, 145 + titleBob);

        // Subtitle
        gc.setFont(Font.font("Helvetica Neue", FontWeight.NORMAL, 15));
        gc.setFill(COL_SUBTITLE);
        gc.fillText("A Turn-Based RPG  •  Java / JavaFX Canvas", W / 2, 178 + titleBob);

        // Garis pemisah
        gc.setStroke(Color.web("#1a4060"));
        gc.setLineWidth(1.5);
        gc.strokeLine(W / 2 - 180, 200, W / 2 + 180, 200);

        // Menu items
        double menuStartY = 255;
        double boxW = 280, boxH = 50, boxX = W / 2 - boxW / 2;

        for (int i = 0; i < items.length; i++) {
            double y = menuStartY + i * 72;
            boolean sel = (i == selected);

            // Kotak latar
            gc.setFill(sel ? COL_BOX_SEL : COL_BOX_NRM);
            gc.fillRoundRect(boxX, y, boxW, boxH, 8, 8);

            // Border
            gc.setStroke(sel ? COL_SELECTED : COL_BORDER);
            gc.setLineWidth(sel ? 2.0 : 1.0);
            gc.strokeRoundRect(boxX, y, boxW, boxH, 8, 8);

            // Tanda pilih
            if (sel) {
                gc.setFill(COL_SELECTED);
                gc.fillText("▶", boxX + 22, y + boxH / 2 + 6);
            }

            // Label
            gc.setFont(Font.font("Helvetica Neue",
                sel ? FontWeight.BOLD : FontWeight.NORMAL, 17));
            gc.setFill(sel ? COL_SELECTED : COL_NORMAL);
            gc.fillText(items[i], W / 2, y + boxH / 2 + 6);
        }

        // Hint keyboard
        gc.setFont(Font.font("Helvetica Neue", FontWeight.NORMAL, 12));
        gc.setFill(Color.web("#2a4a60"));
        gc.fillText("↑ ↓  Navigasi     ENTER  Pilih", W / 2, H - 28);
    }

    private void executeSelected() {
        GameManager gm = GameManager.getInstance();
        switch (selected) {
            case 0: // New Game
                String name = "Hero"; // nama default — dialog input ditampilkan di GameScreen
                gm.startNewGame(name);
                gm.getUIManager().ShowMenu(new GameScreen(), true);
                break;
            case 1: // Load Game
                if (gm.hasSave()) {
                    boolean ok = gm.load();
                    if (ok) {
                        gm.getUIManager().ShowMenu(new GameScreen(), true);
                    }
                }
                break;
            case 2: // Quit
                Platform.exit();
                break;
        }
    }
}
