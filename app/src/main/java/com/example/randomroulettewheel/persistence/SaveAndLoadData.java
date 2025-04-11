package com.example.randomroulettewheel.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.randomroulettewheel.model.ProbabilityArray;

import java.util.HashMap;
import java.util.Map;

public class SaveAndLoadData {
    // 存储
    public static void saveProbabilityArray(Context context, String listKey, ProbabilityArray array) {
        SharedPreferences prefs = context.getSharedPreferences("probability_data", Context.MODE_PRIVATE);
        prefs.edit().putString(listKey, array.toJson()).apply(); // 使用不同key区分列表
    }

    // 获取所有存储的ProbabilityArray
    public static Map<String, ProbabilityArray> loadAllProbabilityLists(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("probability_data", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll(); // 获取所有键值对
        Map<String, ProbabilityArray> result = new HashMap<>();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            try {
                String rawJson = (String) entry.getValue();
                ProbabilityArray array = ProbabilityArray.fromJson(rawJson);
                result.put(entry.getKey(), array);
            } catch (Exception e) {
                Log.e("Storage", "Failed to parse: " + entry.getKey(), e);
            }
        }
        return result;
    }

    public static void deleteProbabilityArray(Context context, String str) {

    }
}
