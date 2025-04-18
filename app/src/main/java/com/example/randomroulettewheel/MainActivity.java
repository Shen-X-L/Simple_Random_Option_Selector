package com.example.randomroulettewheel;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.randomroulettewheel.easycontrols.DialogUtils;
import com.example.randomroulettewheel.model.ProbabilityArray;
import com.example.randomroulettewheel.persistence.LoadActivity;
import com.example.randomroulettewheel.simple.SimpleRandomActivity;
import com.google.android.material.button.MaterialButton;

//MainActivity 是一个 Android 应用程序的主活动（Activity），
//它继承自 AppCompatActivity。这个类的主要作用是管理应用程序的用户界面和生命周期。
public class MainActivity extends AppCompatActivity {
    private LinearLayout container;//主容器
    private ImageButton addButton;//添加按钮
    private DrawerLayout drawerLayout;//主侧边框
    private LinearLayout sidebar;//侧边容器
    ProbabilityArray dataArray = new ProbabilityArray();
    public DynamicContainerManager manager;
    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK) {
                ProbabilityArray array = result.getData().getParcelableExtra("probability_array");
                // 处理返回的数据
                manager.changeDataArray(addButton,array);
            }
        }
    );
    //Activity 的生命周期方法，在 Activity 首次创建时调用。
    //savedInstanceState 用于保存 Activity 被销毁前的状态（如屏幕旋转时）。
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //作用：将 activity_main.xml 布局文件加载为当前 Activity 的界面。
        //R.layout.activity_main 是编译时生成的资源 ID。
        // 确保这行在初始化视图之前
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        if (drawerLayout == null) {
            throw new IllegalStateException("DrawerLayout not found in layout");
        }

        //构建主容器 按钮 动态概率对象设置类
        container = findViewById(R.id.container);
        addButton = findViewById(R.id.add_button);
        manager=new DynamicContainerManager(MainActivity.this,container,dataArray);
        // 添加按钮点击事件
        addButton.setOnClickListener(v -> {manager.addNewContainer(addButton);});

        // 设置侧边栏跳转按钮点击事件
        sidebar = findViewById(R.id.sidebar);
        //普通抽奖按钮
        MaterialButton simpleRandomButton = sidebar.findViewById(R.id.simple_random_button);
        simpleRandomButton.setOnClickListener(v -> {
            // 关闭侧边栏
            drawerLayout.closeDrawer(sidebar);
            //创建一个显式 Intent 连接页面并跳转
            Intent intent = new Intent(MainActivity.this, SimpleRandomActivity.class);
            //向 Intent 中添加附加数据,需实现 Parcelable 或 Serializable 接口
            intent.putExtra("probability_array", dataArray);
            //执行 Activity 跳转
            startActivity(intent);
        });
        //保存当前配置
        ImageButton saveButton = sidebar.findViewById(R.id.save_button);
        //弹出弹框设置配置名字
        saveButton.setOnClickListener(v -> {
            if(dataArray.size() <= 0){
                //无配置 不储存
                return;
            }
            DialogUtils.showSaveDialog(
                    this,               // Context
                    dataArray,       // 要保存的ProbabilityArray
                    savedName -> {      // 保存成功回调
                        Toast.makeText(this, "已保存为: " + savedName, Toast.LENGTH_SHORT).show();
                    }
            );
        });
        //跳转页面读取配置
        ImageButton loadButton = sidebar.findViewById(R.id.load_button);
        loadButton.setOnClickListener(v -> {
            // 关闭侧边栏
            drawerLayout.closeDrawer(sidebar);
            //创建一个显式 Intent 连接页面并跳转
            Intent intent = new Intent(MainActivity.this, LoadActivity.class);
            //执行跳转
            launcher.launch(intent);
        });


        // 设置Drawer监听器（完整实现所有方法）
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {}// 侧边栏滑动时调用
            @Override
            public void onDrawerOpened(@NonNull View drawerView) {}// 侧边栏完全打开时调用
            @Override
            public void onDrawerClosed(@NonNull View drawerView) {}// 侧边栏完全关闭时调用
            @Override
            public void onDrawerStateChanged(int newState) {}// 侧边栏状态改变时调用
        });

        // 启用ActionBar的返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 处理ActionBar的返回按钮点击
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}