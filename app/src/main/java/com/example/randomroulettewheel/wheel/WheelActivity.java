package com.example.randomroulettewheel.wheel;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.randomroulettewheel.R;
import com.example.randomroulettewheel.model.ProbabilityArray;
import com.example.randomroulettewheel.model.Wheel;
import com.example.randomroulettewheel.views.WheelView;
import com.google.android.material.button.MaterialButton;

import java.util.Random;

public class WheelActivity extends AppCompatActivity implements Wheel.WheelStopListener,Wheel.WheelAngleVelocityListener {
    private final Random random = new Random();
    private ProbabilityArray probabilityArray;
    private TextView resultText;
    private TextView angleVelocityText;
    private Wheel wheel;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //初始化视图
        setContentView(R.layout.activity_wheel_random);

        // 初始化控件
        resultText = findViewById(R.id.result_text);
        angleVelocityText = findViewById(R.id.angle_velocity_text);
        WheelView wheelView = findViewById(R.id.wheel);
        Button rotateButton = findViewById(R.id.btn_rotate);
        MaterialButton backButton = findViewById(R.id.back_button);

        backButton.setOnClickListener(v ->{
            finish();
        });


        probabilityArray = getIntent().getParcelableExtra("probability_array");
        if (probabilityArray == null || probabilityArray.size() == 0) {
            resultText.setText("错误：无有效数据");
            return;
        }

        // 初始化管理器
        wheel = new Wheel(wheelView,probabilityArray);
        // 初始化触摸监听
        wheel.setupTouchListener();
        wheel.setWheelStopListener(this); // 设置回调监听器
        wheel.setWheelAngleVelocityListener(this);

        // 旋转按钮点击事件
        rotateButton.setOnClickListener(v -> {
            // 随机初始速度 (500-1000度/秒)
            double initialVelocity = 200 + Math.random() * 200;
            wheel.rotateWheel(initialVelocity);
        });

    }
    // 实现回调方法
    @Override
    public void onWheelStopped(String selectedOption) {
        runOnUiThread(() -> {
            resultText.setText("选中: " + selectedOption);
        });
    }
    @Override
    public void getWheelAngleVelocity(double angleVelocity){
        runOnUiThread(() -> {
            //angleVelocityText.setText("速度为: " + angleVelocity);
            angleVelocityText.setText("");
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wheel != null) {
            wheel.stop();
            wheel.setWheelStopListener(null); // 避免内存泄漏
        }
    }

}
