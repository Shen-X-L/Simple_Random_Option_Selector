package com.example.randomroulettewheel.easycontrols;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;

import com.example.randomroulettewheel.model.ProbabilityArray;
import com.example.randomroulettewheel.persistence.SaveAndLoadData;

//保存弹窗类
public class DialogUtils {
    public static void showSaveDialog(Context context, ProbabilityArray array, OnSaveListener listener) {
        // 1. 创建输入框
        EditText input = new EditText(context);
        input.setHint("请输入保存名称");

        // 2. 构建AlertDialog
        // 系统标准弹窗，支持自定义布局
        new AlertDialog.Builder(context)
                .setTitle("保存概率配置")
                .setView(input)
                .setPositiveButton("保存", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        SaveAndLoadData.saveProbabilityArray(context, name, array);
                        listener.onSaved(name); // 回调保存成功
                    } else {
                        Toast.makeText(context, "名称不能为空", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    public interface OnSaveListener {
        void onSaved(String savedName);
    }
}