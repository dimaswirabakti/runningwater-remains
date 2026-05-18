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

public class Main extends Application {

    private static final double WIDTH  = 900;
    private static final double HEIGHT = 600;

    private long lastNanoTime = -1;

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        StackPane root = new StackPane(canvas);
        root.setStyle("-fx-background-color: black;");

        Scene scene = new Scene(root, WIDTH, HEIGHT, Color.BLACK);

        // INISIALISASI GAMEMANAGER & SYSTEMS
        GameManager   gm    = GameManager.getInstance();
        InputHandler  input = gm.getInputHandler();
        UIManager     ui    = gm.getUIManager();

        gm.Initialize();
        AssetManager.getInstance().loadAll();   // muat semua PNG (kosong untuk sekarang)

        // DAFTARKAN INPUT KE SCENE 
        scene.setOnKeyPressed(input::handleKeyPressed);
        scene.setOnKeyReleased(input::handleKeyReleased);

        // LAYAR PERTAMA
        ui.ShowMenu(new MainMenuScreen(), false);

        // GAME LOOP
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

        // STAGE SETUP
        primaryStage.setTitle("Runningwater Remains");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
