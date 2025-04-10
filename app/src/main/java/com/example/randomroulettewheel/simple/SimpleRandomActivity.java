package com.example.randomroulettewheel.simple;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.randomroulettewheel.R;
import com.example.randomroulettewheel.model.ProbabilityArray;
import com.google.android.material.button.MaterialButton;

public class SimpleRandomActivity extends AppCompatActivity {
    private ProbabilityArray probabilityArray;
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_random);

        // 初始化视图
        resultText = findViewById(R.id.result_text);
        MaterialButton drawButton = findViewById(R.id.draw_button);
        MaterialButton backButton = findViewById(R.id.back_button);

        // 返回按钮
        backButton.setOnClickListener(v -> finish());

        // 接收传递的概率数组
        probabilityArray = getIntent().getParcelableExtra("probability_array");
        if (probabilityArray == null || probabilityArray.size() == 0) {
            resultText.setText("错误：无有效数据");
            drawButton.setEnabled(false);
            return;
        }

        // 抽奖按钮点击事件
        drawButton.setOnClickListener(v -> {
            ProbabilityArray.Probability selected = drawRandomProbability();
            showResult(selected);
        });
    }

    // 随机抽奖方法
    private ProbabilityArray.Probability drawRandomProbability() {
        double random = Math.random(); // 生成[0,1)随机数
        double cumulativeProb = 0;// 概率和
        // 选取概率对象
        for (int i = 0; i < probabilityArray.size(); i++) {
            ProbabilityArray.Probability p = probabilityArray.getObject(i);
            cumulativeProb += p.getProbability();
            if (random <= cumulativeProb) {
                return p;
            }
        }
        return probabilityArray.getObject(0); // 兜底返回第一个
    }

    // 显示结果
    private void showResult(ProbabilityArray.Probability selected) {
        String result = String.format("抽中: %s\n概率: %.2f%%",
                selected.getOptionName(),
                selected.getProbability() * 100);
        resultText.setText(result);
    }
}