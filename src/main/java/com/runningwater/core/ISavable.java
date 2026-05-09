package com.runningwater.core;

import java.util.Map;

/**
 * Kontrak untuk objek yang state-nya bisa disimpan dan dimuat ulang.
 * Diimplementasikan oleh PlayChar, Stages, dan GameManager.
 */
public interface ISavable {
    Map<String, Object> getSaveData();
    void loadSaveData(Map<String, Object> data);
}
