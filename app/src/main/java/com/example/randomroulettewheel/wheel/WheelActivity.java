package com.example.randomroulettewheel.wheel;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.randomroulettewheel.R;
import com.example.randomroulettewheel.model.ProbabilityArray;
import com.example.randomroulettewheel.model.Wheel;
import com.example.randomroulettewheel.views.WheelView;

import java.util.Random;

public class WheelActivity extends AppCompatActivity {
    private final Random random = new Random();
    private ProbabilityArray probabilityArray;
    private TextView resultText;
    private Wheel wheel;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //初始化视图
        setContentView(R.layout.activity_wheel_random);

        // 初始化控件
        resultText = findViewById(R.id.result_text);
        WheelView wheelView = findViewById(R.id.wheel);
        Button rotateButton = findViewById(R.id.btn_rotate);

        probabilityArray = getIntent().getParcelableExtra("probability_array");
        if (probabilityArray == null || probabilityArray.size() == 0) {
            resultText.setText("错误：无有效数据");
            return;
        }

        // 初始化管理器
        wheel = new Wheel(wheelView,probabilityArray);
        // 初始化触摸监听
        wheel.setupTouchListener();

        // 旋转按钮点击事件
        rotateButton.setOnClickListener(v -> {
            // 随机初始速度 (500-1000度/秒)
            double initialVelocity = 500 + Math.random() * 500;
            wheel.rotateWheel(initialVelocity);
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wheel != null) {
            wheel.stop();
        }
    }

}
