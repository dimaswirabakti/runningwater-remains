package com.runningwater;

import com.runningwater.engine.AssetManager;
import com.runningwater.manager.GameManager;
import com.runningwater.screen.MainMenuScreen;
import com.runningwater.system.InputHandler;
import com.runningwater.system.UIManager;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Entry point JavaFX — Canvas + AnimationTimer game loop.
 *
 * Alur tiap frame (60 FPS):
 *   1. handleInput  — baca tombol yang sedang/baru ditekan
 *   2. update       — perbarui logika (animasi, timer, state)
 *   3. render       — gambar segalanya ke kanvas dari nol
 *   4. clearJustPressed — reset "baru ditekan" agar tidak terbawa ke frame berikutnya
 */
public class Main extends Application {

    private static final double WIDTH  = 900;
    private static final double HEIGHT = 600;

    private long lastNanoTime = -1;

    @Override
    public void start(Stage primaryStage) {
        // ── 1. Kanvas ────────────────────────────────────────────────────────
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        StackPane root = new StackPane(canvas);
        root.setStyle("-fx-background-color: black;");

        Scene scene = new Scene(root, WIDTH, HEIGHT, Color.BLACK);

        // ── 2. Inisialisasi GameManager & Systems ────────────────────────────
        GameManager   gm    = GameManager.getInstance();
        InputHandler  input = gm.getInputHandler();
        UIManager     ui    = gm.getUIManager();

        gm.Initialize();
        AssetManager.getInstance().loadAll();   // muat semua PNG (kosong untuk sekarang)

        // ── 3. Daftarkan input ke Scene ──────────────────────────────────────
        scene.setOnKeyPressed(input::handleKeyPressed);
        scene.setOnKeyReleased(input::handleKeyReleased);

        // ── 4. Layar pertama ─────────────────────────────────────────────────
        ui.ShowMenu(new MainMenuScreen(), false);

        // ── 5. Game Loop ─────────────────────────────────────────────────────
        GraphicsContext gc = canvas.getGraphicsContext2D();

        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Delta time dalam detik
                if (lastNanoTime < 0) { lastNanoTime = now; return; }
                double dt = Math.min((now - lastNanoTime) / 1_000_000_000.0, 0.05);
                lastNanoTime = now;

                // Bersihkan kanvas
                gc.clearRect(0, 0, WIDTH, HEIGHT);

                // Update & render screen aktif
                var currentScreen = ui.getCurrentScreen();
                if (currentScreen != null) {
                    currentScreen.handleInput(input);
                    currentScreen.update(dt);
                    currentScreen.render(gc);
                }

                // Update & render efek fade transisi
                ui.updateFade(dt);
                ui.renderFade(gc, WIDTH, HEIGHT);

                // Bersihkan "just pressed" di akhir frame
                input.clearJustPressed();

                // Polymorphism: update semua system backend
                gm.Tick();
            }
        };
        gameLoop.start();

        // ── 6. Stage setup ───────────────────────────────────────────────────
        primaryStage.setTitle("Runningwater Remains");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
