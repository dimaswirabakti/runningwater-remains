package com.runningwater.system;

import com.runningwater.core.GameSystem;
import com.runningwater.core.ISavable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SaveSystem extends GameSystem {
    private final String saveFilePath;

    public SaveSystem() {
        super("SaveSystem");
        this.saveFilePath = "runningwater_save.txt";
    }

    @Override
    public void Initialize() {
        this.isActive = true;
    }

    @Override
    public void UpdateSystem() {}

    public boolean hasSave() {
        return new File(saveFilePath).exists();
    }

    public boolean SaveGame(Map<String, ISavable> objects) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(saveFilePath))) {
            for (Map.Entry<String, ISavable> entry : objects.entrySet()) {
                pw.println("[" + entry.getKey() + "]");
                Map<String, Object> data = entry.getValue().getSaveData();
                for (Map.Entry<String, Object> e : data.entrySet()) {
                    pw.println(e.getKey() + "=" + e.getValue());
                }
                pw.println();
            }
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean LoadGame(Map<String, ISavable> objects) {
        File f = new File(saveFilePath);
        if (!f.exists()) return false;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            String currentSection = null;
            Map<String, Object> currentData = new LinkedHashMap<>();

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    flushSection(currentSection, currentData, objects);
                    currentSection = null;
                    currentData = new LinkedHashMap<>();
                } else if (line.startsWith("[") && line.endsWith("]")) {
                    flushSection(currentSection, currentData, objects);
                    currentSection = line.substring(1, line.length() - 1);
                    currentData = new LinkedHashMap<>();
                } else if (line.contains("=")) {
                    int idx = line.indexOf('=');
                    String key = line.substring(0, idx);
                    String val = line.substring(idx + 1);
                    Object parsed;
                    try {
                        parsed = Integer.parseInt(val);
                    } catch (NumberFormatException nfe) {
                        parsed = val;
                    }
                    currentData.put(key, parsed);
                }
            }
            // Flush section terakhir bila file tidak diakhiri baris kosong
            flushSection(currentSection, currentData, objects);
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void flushSection(String section,
                              Map<String, Object> data,
                              Map<String, ISavable> objects) {
        if (section != null && objects.containsKey(section) && !data.isEmpty()) {
            objects.get(section).loadSaveData(data);
        }
    }
}
