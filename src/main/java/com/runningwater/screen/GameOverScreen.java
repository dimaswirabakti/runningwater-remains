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

// Layar Game Over atau Victory.
public class GameOverScreen implements Screen {

    private static final double W = 900;
    private static final double H = 600;

    private final String[] items = {"Play Again", "Main Menu", "Quit"};
    private int    selected = 0;
    private double time     = 0;

    private final boolean victory;
    private final int     finalScore;
    private final int     highScore;

    public GameOverScreen() {
        GameManager gm = GameManager.getInstance();
        this.victory    = "VICTORY".equals(gm.getGameState());
        this.finalScore = gm.getScoringSystem().GetScore();
        this.highScore  = gm.getScoringSystem().getHighScore();
    }

    @Override
    public void handleInput(InputHandler input) {
        if (input.isJustPressed(KeyCode.UP))   selected = (selected - 1 + items.length) % items.length;
        if (input.isJustPressed(KeyCode.DOWN)) selected = (selected + 1) % items.length;
        if (input.isJustPressed(KeyCode.ENTER) || input.isJustPressed(KeyCode.SPACE)) {
            executeSelected();
        }
    }

    @Override
    public void update(double dt) { time += dt; }

    @Override
    public void render(GraphicsContext gc) {
        // Background
        Color bgTop = victory ? Color.web("#061828") : Color.web("#180606");
        Color bgBot = victory ? Color.web("#020e1a") : Color.web("#0e0202");
        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, bgTop), new Stop(1, bgBot)));
        gc.fillRect(0, 0, W, H);

        // Partikel (bintang / percik api)
        for (int i = 0; i < 35; i++) {
            double px = (i * 137.5 + time * (victory ? 15 : 8)) % W;
            double py = (i * 89.3  + time * (victory ? 20 : 12)) % H;
            Color pCol = victory ? Color.web("#4fc3f7", 0.5) : Color.web("#ff4444", 0.4);
            gc.setFill(pCol);
            gc.fillOval(px, py, 3, 3);
        }

        gc.setTextAlign(TextAlignment.CENTER);

        // Judul besar
        String title = victory ? "VICTORY!" : "GAME OVER";
        Color titleCol = victory ? Color.web("#4fc3f7") : Color.web("#ff4444");
        double bob = Math.sin(time * 1.8) * 6;

        gc.setFont(Font.font("Helvetica Neue", FontWeight.BOLD, 62));
        gc.setFill(Color.web("#000000", 0.4));
        gc.fillText(title, W / 2 + 4, 145 + bob + 4);
        gc.setFill(titleCol);
        gc.fillText(title, W / 2, 145 + bob);

        // Subtitle
        String sub = victory
            ? "Sungai mengalir kembali. Perjalananmu selesai."
            : "Kamu jatuh sebelum sungai pulih.";
        gc.setFont(Font.font("Helvetica Neue", FontWeight.NORMAL, 16));
        gc.setFill(Color.web(victory ? "#7ab8d8" : "#d87a7a"));
        gc.fillText(sub, W / 2, 182 + bob);

        // Skor
        gc.setFont(Font.font("Helvetica Neue", FontWeight.BOLD, 22));
        gc.setFill(Color.web("#f0d060"));
        gc.fillText("Final Score: " + finalScore, W / 2, 235);
        gc.setFont(Font.font("Helvetica Neue", FontWeight.NORMAL, 15));
        gc.setFill(Color.web("#a09040"));
        gc.fillText("High Score:  " + highScore, W / 2, 262);

        // Garis
        gc.setStroke(Color.web(victory ? "#1a4060" : "#601a1a"));
        gc.setLineWidth(1.5);
        gc.strokeLine(W / 2 - 180, 282, W / 2 + 180, 282);

        // Menu
        double menuY = 310;
        double boxW = 220, boxH = 46;
        for (int i = 0; i < items.length; i++) {
            boolean sel = (i == selected);
            double bx = W / 2 - boxW / 2;
            double by = menuY + i * 62;
            Color boxFill   = sel ? Color.web(victory ? "#0d2a40" : "#2a0d0d") : Color.web("#07192b");
            Color boxBorder = sel ? titleCol : Color.web("#1a4060");

            gc.setFill(boxFill);
            gc.fillRoundRect(bx, by, boxW, boxH, 8, 8);
            gc.setStroke(boxBorder);
            gc.setLineWidth(sel ? 2 : 1);
            gc.strokeRoundRect(bx, by, boxW, boxH, 8, 8);

            gc.setFont(Font.font("Helvetica Neue", sel ? FontWeight.BOLD : FontWeight.NORMAL, 16));
            gc.setFill(sel ? titleCol : Color.web("#6a9abb"));
            gc.fillText(items[i], W / 2, by + boxH / 2 + 6);
        }

        // Hint
        gc.setFont(Font.font("Helvetica Neue", FontWeight.NORMAL, 12));
        gc.setFill(Color.web("#2a4060"));
        gc.fillText("↑ ↓  Navigasi     ENTER  Pilih", W / 2, H - 18);
    }

    private void executeSelected() {
        GameManager gm = GameManager.getInstance();
        switch (selected) {
            case 0: // Play Again
                gm.startNewGame("Hero");
                gm.getUIManager().ShowMenu(new GameScreen(), true);
                break;
            case 1: // Main Menu
                gm.getUIManager().ShowMenu(new MainMenuScreen(), true);
                break;
            case 2: // Quit
                Platform.exit();
                break;
        }
    }
}
