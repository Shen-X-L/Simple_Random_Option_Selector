package com.example.randomroulettewheel.persistence;


import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.randomroulettewheel.R;
import com.example.randomroulettewheel.model.ProbabilityArray;

import java.util.Map;

public class LoadActivity extends AppCompatActivity {
    private LinearLayout container;//主容器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);

        // 初始化RecyclerView
        container = findViewById(R.id.load_container);
        // 加载数据
        Map<String, ProbabilityArray> probabilityData = SaveAndLoadData.loadAllProbabilityLists(this);
        //添加窗口
        for(Map.Entry<String,ProbabilityArray> entry: probabilityData.entrySet()){
            View item = createProbabilityItem(entry.getKey(), entry.getValue());
            container.addView(item);
        }
    }
    public View createProbabilityItem(String str, ProbabilityArray array) {
        // 1. 创建外层容器（占父容器1/4宽度）
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        row.setOrientation(LinearLayout.HORIZONTAL);

        // 2. 添加图片按钮
        ImageButton imageButton = new ImageButton(this);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1  // 权重
        );
        imageButton.setLayoutParams(imgParams);
        imageButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageButton.setImageResource(R.drawable.baseline_content_paste_24);
        imageButton.setBackground(null);
        imageButton.setOnClickListener(v -> {
            backToMainActivity(array);
        });


        // 3. 文字部分（占2/3宽度）
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                4  // 权重
        );
        textView.setLayoutParams(textParams);
        textView.setText(str);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        textView.setPadding(8, 0, 8, 0);


        // 4.2 删除按钮（占1/3宽度）
        ImageButton deleteButton = new ImageButton(this);
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1  // 权重
        );
        deleteButton.setLayoutParams(deleteParams);
        deleteButton.setImageResource(android.R.drawable.ic_menu_delete);
        deleteButton.setBackground(null);
        deleteButton.setOnClickListener(v -> {
            container.removeView(row); // 从父容器移除
            SaveAndLoadData.deleteProbabilityArray(this, str); // 删除持久化数据
        });

        row.addView(imageButton);
        row.addView(textView);
        row.addView(deleteButton);
        return row;
    }
    public void backToMainActivity(ProbabilityArray array) {
        Intent intent = new Intent();
        intent.putExtra("probability_array", array);
        setResult(RESULT_OK, intent);
        finish();
    }
}
