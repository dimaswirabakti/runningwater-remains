# Panduan Mengganti Karakter Shape Geometri dengan Sprite Custom

## Prasyarat

- Project `rwr-v2` sudah berjalan dengan baik
- File PNG karakter sudah siap (lihat Tahap 1)

---

## Tahap 1 — Siapkan File Gambar

### Format
Gunakan **PNG** dengan background transparan (bukan JPG).

### Ukuran
Gunakan ukuran yang sama untuk semua frame satu karakter.
Ukuran yang disarankan: **96×96 px** atau **128×128 px** per frame.

### Penamaan File
Ikuti konvensi penamaan berikut agar cocok dengan kode yang sudah ada:

```
{karakter}_{state}_{index}.png
```

Contoh untuk player:

```
player_idle_0.png
player_idle_1.png
player_attack_0.png
player_attack_1.png
player_attack_2.png
player_attack_3.png
player_attack_4.png
player_hurt_0.png
player_hurt_1.png
player_hurt_2.png
player_dead_0.png
player_dead_1.png
player_dead_2.png
player_dead_3.png
```

Contoh untuk enemy (sesuaikan dengan `enemyType` di `Enemies.java`):

```
enemy_slime_idle_0.png
enemy_slime_idle_1.png
enemy_slime_attack_0.png
...
enemy_bandit_idle_0.png
...
enemy_wolf_idle_0.png
...
enemy_construct_idle_0.png
...
enemy_spirit_idle_0.png
...
```

### Jumlah Frame per State
Jumlah file per state harus cocok dengan konstanta di `AnimationPlayer.java`:

| State    | Jumlah Frame | Konstanta              |
|----------|-------------|------------------------|
| `idle`   | 2           | `IDLE_FRAMES = 2`      |
| `attack` | 5           | `ATTACK_FRAMES = 5`    |
| `hurt`   | 3           | `HURT_FRAMES = 3`      |
| `dead`   | 4           | `DEAD_FRAMES = 4`      |

Jika jumlah frame berbeda, ubah nilai konstanta tersebut di `AnimationPlayer.java`
sesuai jumlah frame yang kamu buat.

---

## Tahap 2 — Masukkan File ke Project

### Lokasi Folder
Taruh semua file PNG di dalam folder berikut:

```
rwr-v2/
└── src/main/resources/assets/sprites/
```

---

## Tahap 3 — Ubah Kode

### 3a. `AssetManager.java`

Buka file:
```
src/main/java/com/runningwater/engine/AssetManager.java
```

Temukan method `loadAll()` yang saat ini kosong, lalu isi dengan pemanggilan `load()`
untuk setiap file PNG:

```java
public void loadAll() {
    // ── Player ────────────────────────────────────────────────────────────
    load("player_idle_0",    "/assets/sprites/player_idle_0.png");
    load("player_idle_1",    "/assets/sprites/player_idle_1.png");
    load("player_attack_0",  "/assets/sprites/player_attack_0.png");
    load("player_attack_1",  "/assets/sprites/player_attack_1.png");
    load("player_attack_2",  "/assets/sprites/player_attack_2.png");
    load("player_attack_3",  "/assets/sprites/player_attack_3.png");
    load("player_attack_4",  "/assets/sprites/player_attack_4.png");
    load("player_hurt_0",    "/assets/sprites/player_hurt_0.png");
    load("player_hurt_1",    "/assets/sprites/player_hurt_1.png");
    load("player_hurt_2",    "/assets/sprites/player_hurt_2.png");
    load("player_dead_0",    "/assets/sprites/player_dead_0.png");
    load("player_dead_1",    "/assets/sprites/player_dead_1.png");
    load("player_dead_2",    "/assets/sprites/player_dead_2.png");
    load("player_dead_3",    "/assets/sprites/player_dead_3.png");

    // ── Enemy: Slime ──────────────────────────────────────────────────────
    load("enemy_slime_idle_0",   "/assets/sprites/enemy_slime_idle_0.png");
    load("enemy_slime_idle_1",   "/assets/sprites/enemy_slime_idle_1.png");
    load("enemy_slime_attack_0", "/assets/sprites/enemy_slime_attack_0.png");
    // tambahkan state lainnya...

    // ── Enemy: Bandit ─────────────────────────────────────────────────────
    load("enemy_bandit_idle_0",  "/assets/sprites/enemy_bandit_idle_0.png");
    // tambahkan state lainnya...

    // Ulangi pola yang sama untuk: wolf, construct, spirit
}
```

---

### 3b. `AnimationPlayer.java`

Buka file:
```
src/main/java/com/runningwater/engine/AnimationPlayer.java
```

**Langkah 1 — Tambahkan field Image array** setelah deklarasi field yang sudah ada:

```java
// Tambahkan field ini di dalam class AnimationPlayer:
private Image[] idleFrames;
private Image[] attackFrames;
private Image[] hurtFrames;
private Image[] deadFrames;
private boolean spritesLoaded = false;
```

**Langkah 2 — Tambahkan method `loadSprites()`** di dalam class:

```java
/**
 * Muat sprite PNG untuk karakter ini dari AssetManager.
 * @param prefix contoh: "player" atau "enemy_slime"
 */
public void loadSprites(String prefix) {
    AssetManager am = AssetManager.getInstance();
    idleFrames   = loadFrameArray(am, prefix + "_idle_",   IDLE_FRAMES);
    attackFrames = loadFrameArray(am, prefix + "_attack_", ATTACK_FRAMES);
    hurtFrames   = loadFrameArray(am, prefix + "_hurt_",   HURT_FRAMES);
    deadFrames   = loadFrameArray(am, prefix + "_dead_",   DEAD_FRAMES);
    spritesLoaded = true;
}

private Image[] loadFrameArray(AssetManager am, String prefix, int count) {
    Image[] arr = new Image[count];
    for (int i = 0; i < count; i++) {
        arr[i] = am.get(prefix + i); // bisa null jika file belum ada
    }
    return arr;
}

private Image getCurrentImage() {
    Image[] frames;
    switch (state) {
        case ATTACK: frames = attackFrames; break;
        case HURT:   frames = hurtFrames;   break;
        case DEAD:   frames = deadFrames;   break;
        default:     frames = idleFrames;   break;
    }
    if (frames == null || frame >= frames.length || frames[frame] == null) return null;
    return frames[frame];
}
```

**Langkah 3 — Ubah method `render()`.**
Temukan baris paling awal di dalam method `render()`, lalu tambahkan blok sprite
SEBELUM kode shapes geometri yang sudah ada:

```java
public void render(GraphicsContext gc, double cx, double cy,
                   double w, double h, Color baseCol, boolean flipX) {

    // ── Coba render pakai sprite PNG ──────────────────────────────────────
    Image frame = spritesLoaded ? getCurrentImage() : null;
    if (frame != null) {
        gc.save();
        if (flipX) {
            // Cermin horizontal untuk musuh di sisi kanan layar
            gc.translate(cx + w / 2, cy - h / 2);
            gc.scale(-1, 1);
            gc.drawImage(frame, -w, 0, w, h);
        } else {
            gc.drawImage(frame, cx - w / 2, cy - h / 2, w, h);
        }
        gc.restore();
        return; // sprite berhasil digambar, lewati shapes di bawah
    }
}
```

> Blok fallback (shapes geometri) sengaja dipertahankan. Jika ada sprite yang
> belum dibuat atau file tidak ditemukan, game tetap berjalan tanpa crash.

---

### 3c. `CombatScreen.java`

Buka file:
```
src/main/java/com/runningwater/screen/CombatScreen.java
```

Temukan constructor `CombatScreen()`, lalu tambahkan pemanggilan `loadSprites()`
setelah setiap `AnimationPlayer` dibuat:

```java
public CombatScreen() {
    // Load sprite player
    playerAnim.loadSprites("player");

    // Load sprite tiap enemy
    GameManager gm = GameManager.getInstance();
    for (Enemies e : gm.getCombatSystem().getCurrentEnemies()) {
        AnimationPlayer ap = new AnimationPlayer();
        ap.loadSprites("enemy_" + e.getEnemyType()); // misal: "enemy_slime"
        enemyAnims.add(ap);
    }

    // ... sisa kode constructor yang sudah ada ...
}
```

---

### 3d. `GameScreen.java` (Opsional)

Karakter di layar peta stage juga digambar dengan shapes. Untuk menggantinya,
buka file:
```
src/main/java/com/runningwater/screen/GameScreen.java
```

Temukan method `drawPlayerSprite()` dan `drawEnemyMarker()`, lalu tambahkan
cabang sprite di awal tiap method:

```java
private void drawPlayerSprite(GraphicsContext gc, double x, double y) {
    Image img = AssetManager.getInstance().get("player_idle_0");
    if (img != null) {
        gc.drawImage(img, x - 24, y - 48, 48, 64);
        return;
    }
    // shapes lama sebagai fallback (kode yang sudah ada)
    ...
}

private void drawEnemyMarker(GraphicsContext gc, double x, double y, String name) {
    // Cari tipe enemy dari nama — sesuaikan dengan data stage aktif
    Image img = AssetManager.getInstance().get("enemy_slime_idle_0"); // sesuaikan
    if (img != null) {
        gc.drawImage(img, x - 20, y - 20, 40, 50);
        // Tetap gambar nama enemy di bawah sprite
        gc.setFont(Font.font("Helvetica Neue", FontWeight.NORMAL, 10));
        gc.setFill(Color.web("#ff8888"));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(name, x, y + 38);
        return;
    }
    // shapes lama sebagai fallback
    ...
}
```

---

## Ringkasan Urutan Kerja

```
1. Desain & export karakter sebagai PNG transparan
   → ikuti konvensi penamaan: {karakter}_{state}_{index}.png

2. Taruh semua file PNG di:
   src/main/resources/assets/sprites/

3. Daftarkan di AssetManager.java → method loadAll()
   → load("player_idle_0", "/assets/sprites/player_idle_0.png")

4. Di AnimationPlayer.java:
   → tambah field Image[] untuk tiap state
   → tambah method loadSprites() dan getCurrentImage()
   → ubah render() — tambah blok sprite sebelum shapes fallback

5. Di CombatScreen.java → constructor:
   → playerAnim.loadSprites("player")
   → tiap enemy ap.loadSprites("enemy_" + tipe)

6. (Opsional) Di GameScreen.java:
   → ubah drawPlayerSprite() dan drawEnemyMarker()

7. mvn javafx:run
```

---

## Tips Penting

**Background transparan:** Harus transparan, bukan background berwarna putih.

**Jumlah frame harus cocok:** jika kamu membuat lebih atau kurang dari jumlah frame
default, sesuaikan konstanta `IDLE_FRAMES`, `ATTACK_FRAMES`, dst. di `AnimationPlayer.java`.

**Ukuran konsisten:** semua frame untuk satu karakter harus berukuran sama.
Misalnya semua frame player 96×96 px.

**Fallback tetap berjalan:** jangan hapus kode shapes geometri di `render()`.
Jika ada sprite yang belum selesai, game tetap bisa dijalankan menggunakan shapes
sebagai placeholder.

**Import yang dibutuhkan:** pastikan `import javafx.scene.image.Image;` sudah ada
di bagian atas `AnimationPlayer.java` setelah penambahan field `Image[]`.