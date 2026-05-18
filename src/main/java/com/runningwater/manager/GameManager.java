package com.runningwater.manager;

import com.runningwater.core.GameSystem;
import com.runningwater.core.ISavable;
import com.runningwater.entity.PlayChar;
import com.runningwater.game.Stages;
import com.runningwater.system.CombatSystem;
import com.runningwater.system.InputHandler;
import com.runningwater.system.RewardSystem;
import com.runningwater.system.SaveSystem;
import com.runningwater.system.ScoringSystem;
import com.runningwater.system.UIManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Koordinator pusat seluruh game.
public class GameManager implements ISavable {
    private static GameManager instance;

    private String gameState = "MENU";
    private Stages activeStage;
    private PlayChar player;

    private final List<GameSystem> activeSystems;
    private final UIManager     uiManager;
    private final InputHandler  inputHandler;
    private final CombatSystem  combatSystem;
    private final ScoringSystem scoringSystem;
    private final RewardSystem  rewardSystem;
    private final SaveSystem    saveSystem;

    private GameManager() {
        activeSystems = new ArrayList<>();
        uiManager     = new UIManager();
        inputHandler  = new InputHandler();
        combatSystem  = new CombatSystem();
        scoringSystem = new ScoringSystem();
        rewardSystem  = new RewardSystem();
        saveSystem    = new SaveSystem();

        activeSystems.add(uiManager);
        activeSystems.add(inputHandler);
        activeSystems.add(combatSystem);
        activeSystems.add(scoringSystem);
        activeSystems.add(rewardSystem);
        activeSystems.add(saveSystem);
    }

    public static GameManager getInstance() {
        if (instance == null) instance = new GameManager();
        return instance;
    }

    public void Initialize() {
        // POLYMORPHISM: tiap System menjalankan Initialize() versinya sendiri.
        for (GameSystem sys : activeSystems) sys.Initialize();
    }

    public void Tick() {
        // POLYMORPHISM: panggilan method yang sama, perilaku berbeda per subclass.
        for (GameSystem sys : activeSystems) sys.UpdateSystem();
    }

    public void startNewGame(String playerName) {
        this.player      = new PlayChar(playerName.isEmpty() ? "Hero" : playerName);
        this.activeStage = new Stages(1);
        this.scoringSystem.resetScore();
        this.gameState   = "PLAYING";
    }

    public void advanceToNextStage() {
        int next = activeStage.getStageLevel() + 1;
        if (next > 3) {
            this.gameState = "VICTORY";
        } else {
            this.activeStage = new Stages(next);
            this.gameState   = "PLAYING";
        }
    }

    public void PauseGame() { this.gameState = "PAUSED"; }
    public void Quit()      { this.gameState = "QUIT"; }

    public boolean save() {
        Map<String, ISavable> map = new LinkedHashMap<>();
        if (player      != null) map.put("PlayChar", player);
        if (activeStage != null) map.put("Stages", activeStage);
        map.put("GameManager", this);
        return saveSystem.SaveGame(map);
    }

    public boolean load() {
        if (player      == null) player      = new PlayChar("Hero");
        if (activeStage == null) activeStage = new Stages(1);
        Map<String, ISavable> map = new LinkedHashMap<>();
        map.put("PlayChar", player);
        map.put("Stages", activeStage);
        map.put("GameManager", this);
        boolean ok = saveSystem.LoadGame(map);
        if (ok) {
            this.gameState = "PLAYING";
            scoringSystem.resetScore();
            scoringSystem.AddScore(player.getScore());
        }
        return ok;
    }

    public boolean hasSave() { return saveSystem.hasSave(); }

    // ISavable

    @Override
    public Map<String, Object> getSaveData() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("gameState", gameState);
        return data;
    }

    @Override
    public void loadSaveData(Map<String, Object> data) {
        if (data.get("gameState") != null) this.gameState = data.get("gameState").toString();
    }

    // Getters

    public PlayChar       getPlayer()         { return player; }
    public Stages         getActiveStage()    { return activeStage; }
    public String         getGameState()      { return gameState; }
    public UIManager      getUIManager()      { return uiManager; }
    public InputHandler   getInputHandler()   { return inputHandler; }
    public CombatSystem   getCombatSystem()   { return combatSystem; }
    public ScoringSystem  getScoringSystem()  { return scoringSystem; }
    public RewardSystem   getRewardSystem()   { return rewardSystem; }
    public SaveSystem     getSaveSystem()     { return saveSystem; }
}
