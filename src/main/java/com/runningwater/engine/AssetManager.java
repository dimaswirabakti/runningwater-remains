package com.runningwater.engine;

import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;

public class AssetManager {

    private static AssetManager instance;
    private final Map<String, Image> images = new HashMap<>();

    private AssetManager() {}

    public static AssetManager getInstance() {
        if (instance == null) instance = new AssetManager();
        return instance;
    }

    /**
     * Muat semua asset gambar di awal game.
     * Untuk sekarang semua elemen digambar geometri,
     * method ini disiapkan agar mudah ditambahkan PNG sprite di masa depan.
     *
     * Contoh penambahan sprite:
     *   load("player_idle", "/assets/sprites/player_idle.png");
     */
    public void loadAll() {
        // Tidak ada PNG eksternal, semua digambar shapes.
        // Tambahkan load(...) di sini saat asset PNG tersedia.
    }

    public void load(String key, String resourcePath) {
        try {
            Image img = new Image(
                AssetManager.class.getResourceAsStream(resourcePath)
            );
            if (!img.isError()) {
                images.put(key, img);
            }
        } catch (Exception e) {
            System.err.println("AssetManager: gagal load " + resourcePath);
        }
    }

    public Image get(String key) {
        return images.get(key);
    }

    public boolean has(String key) {
        return images.containsKey(key);
    }
}
