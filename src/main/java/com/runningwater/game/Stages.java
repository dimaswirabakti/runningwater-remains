package com.runningwater.game;

import com.runningwater.core.ISavable;
import com.runningwater.entity.Enemies;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stage atau level dalam game.
public class Stages implements ISavable {
    private String stageName;
    private int stageLevel;
    private boolean isCleared;
    private List<Enemies> enemyList;

    public Stages(int stageLevel) {
        this.stageLevel = stageLevel;
        this.isCleared = false;
        this.enemyList = new ArrayList<>();
        LoadEnvironment();
    }

    public void LoadEnvironment() {
        enemyList.clear();
        switch (stageLevel) {
            case 1:
                stageName = "The Withered Banks";
                enemyList.add(new Enemies("Slime",   30, 1, 5, "slime"));
                enemyList.add(new Enemies("Slime",   30, 1, 5, "slime"));
                break;
            case 2:
                stageName = "Drying Riverbed";
                enemyList.add(new Enemies("Bandit",  50, 2, 9,  "humanoid"));
                enemyList.add(new Enemies("Wolf",    40, 2, 12, "beast"));
                break;
            case 3:
                stageName = "Last Spring's Grave";
                enemyList.add(new Enemies("Stone Sentinel", 80, 3, 14, "construct"));
                enemyList.add(new Enemies("Wraith",         60, 3, 18, "spirit"));
                break;
            default:
                stageName = "Unknown Region";
                enemyList.add(new Enemies("Mystery", 100, stageLevel, 20, "unknown"));
        }
    }

    public void Reset() {
        this.isCleared = false;
        LoadEnvironment();
    }

    public void markCleared() {
        this.isCleared = true;
    }

    public boolean isAllEnemiesDefeated() {
        for (Enemies e : enemyList) {
            if (e.isAlive()) return false;
        }
        return true;
    }

    public String getStageName() { return stageName; }
    public int getStageLevel() { return stageLevel; }
    public boolean isCleared() { return isCleared; }
    public List<Enemies> getEnemyList() { return enemyList; }

    // ISavable

    @Override
    public Map<String, Object> getSaveData() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("stageLevel", stageLevel);
        data.put("isCleared", isCleared ? 1 : 0);
        return data;
    }

    @Override
    public void loadSaveData(Map<String, Object> data) {
        if (data.get("stageLevel") != null) {
            this.stageLevel = ((Number) data.get("stageLevel")).intValue();
            LoadEnvironment();
        }
        if (data.get("isCleared") != null) {
            this.isCleared = ((Number) data.get("isCleared")).intValue() == 1;
        }
    }
}
