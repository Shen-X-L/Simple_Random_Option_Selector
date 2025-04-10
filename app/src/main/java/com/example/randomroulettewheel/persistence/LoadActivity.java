package com.example.randomroulettewheel.persistence;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.randomroulettewheel.R;
import com.example.randomroulettewheel.model.ProbabilityArray;
import com.example.randomroulettewheel.simple.SimpleRandomActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoadActivity extends AppCompatActivity {
    private Map<String, ProbabilityArray> probabilityData;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);

        // 初始化RecyclerView
        recyclerView = findViewById(R.id.load_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // 设置布局管理器

        // 加载数据
        probabilityData = SaveAndLoadData.loadAllProbabilityLists(this); // 添加了Context参数

        // 将Map数据转换为列表用于适配器
        List<ProbabilityArray> dataList = new ArrayList<>(probabilityData.values());


    }
}
