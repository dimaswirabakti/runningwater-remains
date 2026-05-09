# Runningwater Remains

Game sederhana berbasis Java + JavaFX yang dibuat sebagai proyek tugas akhir
mata kuliah **Praktikum Pemrograman Berorientasi Objek**.

Game ini mendemonstrasikan ketujuh konsep OOP wajib:

| Konsep | Bukti di kode |
|---|---|
| **Encapsulation** | Semua field `private`/`protected`, akses lewat method publik. Lihat `ScoringSystem`, `PlayChar`, `SaveSystem`. |
| **Inheritance** | `PlayChar`/`Enemies` extends `Entity`; `Weapon`/`Armour`/`Tools`/`Consumables` extends `Item`; semua manager extends `GameSystem`. |
| **Polymorphism** | `GameManager.Tick()` iterasi `List<GameSystem>` dan memanggil `UpdateSystem()` — runtime memilih implementasi yang sesuai. |
| **Method Overloading** | `ScoringSystem.AddScore(int)` vs `AddScore(int, float)`; `RewardSystem.GrantReward()` vs `GrantReward(float)`; `UIManager.ShowMenu(String)` vs `ShowMenu(String, boolean)`; `Item.Use()` vs `Use(Entity)`. |
| **Method Overriding** | `Enemies.TakeDamage()` dan `PlayChar.TakeDamage()` override `Entity.TakeDamage()`; semua subclass `GameSystem` override `Initialize()` dan `UpdateSystem()`. |
| **Abstract Class** | `Entity`, `Item`, `GameSystem`. |
| **Interface** | `ISavable` — diimplementasikan oleh `PlayChar`, `Stages`, `GameManager`; dikonsumsi oleh `SaveSystem`. |

---

## 🚀 Cara Instalasi & Menjalankan

### macOS (termasuk Apple Silicon / M1 M2 M3 M4)

**Prasyarat:**
- JDK 17 atau lebih baru — unduh di [adoptium.net](https://adoptium.net) → pilih **macOS**, arsitektur **aarch64** untuk chip M-series, atau **x64** untuk Intel Mac
- IntelliJ IDEA — unduh di [jetbrains.com/idea](https://www.jetbrains.com/idea/download) (Community Edition gratis)
- Maven sudah termasuk di dalam IntelliJ, tidak perlu instalasi terpisah
  **Langkah instalasi:**

1. Extract file ZIP project → akan muncul folder `rwr-v2`
2. Buka IntelliJ IDEA → **File → Open** → pilih folder `rwr-v2`
3. IntelliJ akan otomatis mendeteksi `pom.xml` dan memulai Maven sync — tunggu hingga selesai (lihat progress bar di pojok kanan bawah)
4. Saat pertama kali sync, Maven mengunduh JavaFX dari internet (termasuk native library `mac-aarch64` untuk chip M-series secara otomatis)
5. Setelah sync selesai, buka panel **Maven** di sisi kanan IntelliJ
6. Ekspansi `rwr-v2 → Plugins → javafx` → klik dua kali **`javafx:run`**
7. Game akan terbuka
   **Alternatif lewat Terminal:**
```bash
cd rwr-v2
mvn javafx:run
```
 
---

### Windows (Windows 10 / 11)

**Prasyarat:**
- JDK 17 atau lebih baru — unduh di [adoptium.net](https://adoptium.net) → pilih **Windows**, arsitektur **x64**
    - Saat instalasi, centang opsi **"Add to PATH"** dan **"Set JAVA_HOME"**
- IntelliJ IDEA — unduh di [jetbrains.com/idea](https://www.jetbrains.com/idea/download) (Community Edition gratis)
- Maven sudah termasuk di dalam IntelliJ, tidak perlu instalasi terpisah
  **Langkah instalasi:**

1. Extract file ZIP project → akan muncul folder `rwr-v2`
2. Buka IntelliJ IDEA → **File → Open** → pilih folder `rwr-v2`
3. IntelliJ akan otomatis mendeteksi `pom.xml` dan memulai Maven sync — tunggu hingga selesai (lihat progress bar di pojok kanan bawah)
4. Saat pertama kali sync, Maven mengunduh JavaFX dari internet (native library `win` untuk Windows diunduh otomatis)
5. Setelah sync selesai, buka panel **Maven** di sisi kanan IntelliJ
6. Ekspansi `rwr-v2 → Plugins → javafx` → klik dua kali **`javafx:run`**
7. Game akan terbuka
   **Alternatif lewat Command Prompt atau PowerShell:**
```cmd
cd rwr-v2
mvn javafx:run
```

> **Catatan Windows:** Jika muncul pesan *"'mvn' is not recognized"*, pastikan IntelliJ digunakan untuk menjalankan game (bukan terminal sistem). IntelliJ menyertakan Maven-nya sendiri secara internal.

---

## 🎮 Cara Bermain

### Main Menu
- **New Game** — masukkan nama karakter, langsung mulai stage 1
- **Load Game** — muat save sebelumnya (auto-disabled jika belum ada save)
- **Quit** — keluar

### Game Screen (peta stage)
- **Engage Combat** atau tekan **ENTER** — masuk ke combat
- **Save** atau tekan **S** — simpan game
- **Main Menu** — kembali ke menu utama

### Combat Screen
- **Attack (Z)** — serang musuh pertama yang masih hidup
- **Ability (A)** — pulihkan 15 HP
- **Item (X)** — buka inventory, pilih item untuk dipakai/equip
- **Flee** — kabur tanpa XP/loot

### Mekanika
- Setiap musuh dikalahkan: **+50 score (×1.5 multiplier) + 40 XP**
- Setelah semua musuh stage mati: dapat 0–2 loot acak, naik ke stage berikutnya
- Naik level: max HP +20, full heal otomatis
- Total **3 stage**. Mengalahkan stage 3 → **Victory**.

---

## 📁 Struktur Project

```
runningwater-remains/
├── pom.xml                                  ← Maven config
├── README.md
└── src/main/
    ├── java/com/runningwater/
    │   ├── Main.java                        ← JavaFX entry point
    │   ├── core/                            ← FONDASI OOP
    │   │   ├── ISavable.java                  (interface)
    │   │   ├── Entity.java                    (abstract)
    │   │   ├── Item.java                      (abstract)
    │   │   ├── GameSystem.java                (abstract)
    │   │   └── Vector2.java
    │   ├── entity/
    │   │   ├── PlayChar.java
    │   │   └── Enemies.java
    │   ├── item/
    │   │   ├── Weapon.java
    │   │   ├── Armour.java
    │   │   ├── Tools.java
    │   │   └── Consumables.java
    │   ├── system/
    │   │   ├── CombatSystem.java
    │   │   ├── ScoringSystem.java
    │   │   ├── RewardSystem.java
    │   │   ├── SaveSystem.java
    │   │   ├── UIManager.java
    │   │   └── InputHandler.java
    │   ├── game/
    │   │   └── Stages.java
    │   ├── manager/
    │   │   └── GameManager.java
    │   └── ui/                              ← JavaFX Controllers
    │       ├── MainMenuController.java
    │       ├── GameController.java
    │       ├── CombatController.java
    │       └── GameOverController.java
    └── resources/
        ├── fxml/
        │   ├── main_menu.fxml
        │   ├── game_screen.fxml
        │   ├── combat_screen.fxml
        │   └── game_over.fxml
        └── css/
            └── style.css
```

---

## 🧪 Troubleshooting

**Q: "Error: JavaFX runtime components are missing".**
A: Jangan jalankan dengan `java -jar` polos. Pakai `mvn javafx:run`.

**Q: Maven tidak menemukan `javafx-maven-plugin`.**
A: Pastikan terhubung ke internet saat pertama build. Maven mengunduhnya
ke `~/.m2/repository`. Jika gagal, jalankan `mvn -U javafx:run` (force update).

**Q: Save file ada di mana?**
A: Di folder kerja saat menjalankan game, dengan nama `runningwater_save.txt`.
Format teks polos (mirip INI) — bisa dibuka dengan editor apa saja.

**Q: Tidak ada file save tetapi tombol Load aktif.**
A: Reload IntelliJ (kontroler mengecek `runningwater_save.txt` di working
directory saat startup). Tombol akan disabled jika file belum ada.

---