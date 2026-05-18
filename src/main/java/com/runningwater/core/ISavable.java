package com.runningwater.core;

import java.util.Map;

public interface ISavable {
    Map<String, Object> getSaveData();
    void loadSaveData(Map<String, Object> data);
}
