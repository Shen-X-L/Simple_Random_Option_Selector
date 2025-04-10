package com.example.randomroulettewheel;


import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.example.randomroulettewheel.model.ProbabilityArray;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DynamicContainerManager {
    //
    private final Activity activity;
    //容器管理类
    private final Context context;
    //上下文
    private final LinearLayout parentContainer;
    //父容器句柄
    private final ProbabilityArray dataArray;
    //概率对象数组
    private final List<LinearLayout> containerViews = new ArrayList<>();
    //监听对象数组
    private final List<EditText> probabilityInputs = new ArrayList<>();
    private final List<EditText> weightInputs = new ArrayList<>();
    //监听对象数组
    private final List<SeekBar> seekBars = new ArrayList<>();
    //是否是回调更新
    private boolean isCallBack = false;
    //监听对象数组
    public DynamicContainerManager(Activity activity, LinearLayout parentContainer,ProbabilityArray dataArray) {
        this.activity = activity;
        this.context = activity;
        this.parentContainer = parentContainer;
        this.dataArray=dataArray;
        parentContainer.setOrientation(LinearLayout.VERTICAL);

        // 注册数据变化监听器
        dataArray.addDataChangeListener(new ProbabilityArray.DataChangeListener() {
            @Override
            public void onDataChanged() {
                updateAllDisplays(); // 数据变化时更新所有UI
            }
        });
    }
    //更新UI时暂停
    private final AtomicBoolean isUpdatingUI = new AtomicBoolean(false);
    //
    private final Handler handler = new Handler(Looper.getMainLooper());
    // 添加同步方法
    private void updateAllDisplays() {
        if (isUpdatingUI.get()) return;

        isUpdatingUI.set(true);
        try {
            activity.runOnUiThread(() -> {  // 使用 activity 调用
                isCallBack = true;
                updateAllProbabilityDisplays();
                updateAllWeightDisplays();
                isCallBack = false;
            });
        } finally {
            isUpdatingUI.set(false);
        }
    }

    private void updateAllProbabilityDisplays() {
        for (int i = 0; i < seekBars.size(); i++) {
            SeekBar seekBar = seekBars.get(i);
            seekBar.setProgress((int)(dataArray.getProbability(i) * 1000));
        }
        for (int i = 0; i < probabilityInputs.size(); i++) {
            probabilityInputs.get(i).setText(
                    String.format("%.4f", dataArray.getProbability(i))
            );
        }
    }

    private void updateAllWeightDisplays() {
        for (int i = 0; i < weightInputs.size(); i++) {
            weightInputs.get(i).setText(
                    String.format("%.4f", dataArray.getWeight(i))
            );
        }
    }
    public void addNewContainer(View addButton) {
        dataArray.add();
        // 创建容器视图
        LinearLayout container = createContainerView(dataArray,dataArray.getObject(dataArray.size()-1));

        // 获取 add_button 在父布局中的位置索引
        int index = parentContainer.indexOfChild(addButton);
        parentContainer.addView(container,index);
    }
    public LinearLayout createContainerView(ProbabilityArray array,ProbabilityArray.Probability object){
        LinearLayout outerLayout = new LinearLayout(context);
        //设置ID
        outerLayout.setId(View.generateViewId());
        //设置布局
        LinearLayout.LayoutParams outerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        outerParams.topMargin = 8;
        outerLayout.setLayoutParams(outerParams);
        outerLayout.setOrientation(LinearLayout.VERTICAL);
        outerLayout.setGravity(Gravity.CENTER);
        outerLayout.setBackgroundColor(Color.parseColor("#9afc86"));

        // 创建第一排水平布局
        LinearLayout row1 = new LinearLayout(context);
        row1.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        row1.setOrientation(LinearLayout.HORIZONTAL);


        // 添加第一排的名字
        EditText textInput = new EditText(context);
        textInput.setId(View.generateViewId());
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                2
        );
        textInput.setLayoutParams(textParams);
        textInput.setHint("选项名字输入");
        textInput.setInputType(InputType.TYPE_CLASS_TEXT);
        //监听输入
        textInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}// 可留空（不需要时）
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}// 可留空（不需要时）
            @Override
            public void afterTextChanged(Editable s) {
                object.setOptionName(s.toString()); // 控件 → 数据
            }
        });


        //限制double类型数字的输入方式
        InputFilter filter = (source, start, end, dest, dstart, dend) -> {
            // 拒绝非数字和小数点的输入
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (!Character.isDigit(c) && c != '.') {
                    return "";
                }
            }
            // 防止多个小数点
            if (source.toString().contains(".") && dest.toString().contains(".")) {
                return "";
            }
            // 防止小数点开头（如 ".123"）
            if (source.equals(".") && dstart == 0) {
                return "0."; // 自动补零
            }
            return null;
        };


        // 添加第一排的概率
        EditText numberInput1 = new EditText(context);
        numberInput1.setId(View.generateViewId());
        LinearLayout.LayoutParams num1Params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        numberInput1.setLayoutParams(num1Params);
        numberInput1.setHint("概率");
        //限制字符类型
        numberInput1.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        //检测文件输入类型
        numberInput1.setFilters(new InputFilter[]{filter});
        //设置初始值
        double initialProbability = object.getProbability();
        numberInput1.setText(String.format("%.4f", initialProbability)); // 保留 4 位小数
        //监听输入
        numberInput1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            //更改后调用
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (!s.toString().isEmpty()) {
                        double value = Double.parseDouble(s.toString());
                        if (value < 0 || value > 1) {
                            s.clear(); // 清除非法输入
                            return;
                        }
                        if(isCallBack)object.setProbability(value);
                        else array.setProbability(object, value);
                    }
                } catch (IllegalArgumentException e) {
                    Log.e("IllegalArgumentException", "输入值超出范围: " + e.getMessage(), e);
                } catch(IndexOutOfBoundsException e){
                    Log.e("IndexOutOfBoundsException", "索引超出范围: " + e.getMessage(), e);
                }
            }
        });
        probabilityInputs.add(numberInput1);

        //添加第一排的权重
        EditText numberInput2 = new EditText(context);
        numberInput2.setId(View.generateViewId());
        LinearLayout.LayoutParams num2Params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        numberInput2.setLayoutParams(num2Params);
        numberInput2.setHint("权重");
        //限制字符类型
        numberInput2.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        //检测文件输入类型
        numberInput2.setFilters(new InputFilter[]{filter});
        //设置初始值
        double initialWeight = object.getWeight();
        numberInput2.setText(String.format("%.4f", initialWeight)); // 保留 4 位小数
        //监听输入
        numberInput2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (!s.toString().isEmpty()) {
                        //获取值
                        double value = Double.parseDouble(s.toString());
                        if (value < 0) {
                            s.clear();
                            return;
                        }
                        if(isCallBack)object.setWeight(value);
                        else array.setWeight(object,value);
                    }
                } catch (IllegalArgumentException e) {
                    Log.e("IllegalArgumentException", "输入值超出范围: " + e.getMessage(), e);
                    array.setWeight(object,0);
                } catch(IndexOutOfBoundsException e){
                    Log.e("IndexOutOfBoundsException", "索引超出范围: " + e.getMessage(), e);
                }
            }
        });
        weightInputs.add(numberInput2);

        // 添加控件到容器
        row1.addView(textInput);
        row1.addView(numberInput1);
        row1.addView(numberInput2);


        // 创建第二排水平布局
        LinearLayout row2 = new LinearLayout(context);
        LinearLayout.LayoutParams row2Params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        row2Params.topMargin = 12;
        row2.setLayoutParams(row2Params);
        row2.setOrientation(LinearLayout.HORIZONTAL);


        // 添加第二排的固定切换
        CheckBox checkBox = new CheckBox(context);
        checkBox.setId(View.generateViewId());
        LinearLayout.LayoutParams checkBoxParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.5f
        );
        checkBox.setLayoutParams(checkBoxParams);
        checkBox.setText("固定");

        // 设置 CheckBox 的样式（可选）
        checkBox.setButtonTintList(ColorStateList.valueOf(Color.BLUE)); // 设置勾选颜色
        checkBox.setTextColor(Color.BLACK); // 设置文字颜色

        // 绑定锁定/解锁事件
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            array.setLock(object, isChecked); // 更新锁定状态
        });



        // 概率滑条
        SeekBar seekBar = new SeekBar(context);
        seekBar.setId(View.generateViewId());
        LinearLayout.LayoutParams seekBarParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                7.5f
        );
        seekBar.setLayoutParams(seekBarParams);
        // 设置 SeekBar 最大值
        seekBar.setMax(1000); // 扩大精度范围
        // 初始化设置滑条
        activity.runOnUiThread(() -> {
            seekBar.setProgress((int)(array.getProbability(object) * 1000));
        });
        // SeekBar 监听
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try{
                    if (fromUser) {
                        // 计算允许的最大进度（基于未锁定概率总和）
                        int temp = 0;
                        try{
                            temp = array.canItMove(object);
                        }catch (IllegalStateException e) {
                            Log.e("IllegalStateException", "与预期值不符: " + e.getMessage(), e);
                        }catch (IllegalArgumentException e) {
                            Log.e("IllegalArgumentException", "输入值超出范围: " + e.getMessage(), e);
                        }
                        switch(temp){
                            case 0b00:
                                //全锁 锁定到原值
                                seekBar.setProgress((int)(array.getProbability(object)*1000));
                                return;
                            case 0b01:
                                //仅右解锁 不太可能遇到
                                break;
                            case 0b10:
                                int maxProbability10 = (int)(array.maxProbability(object)*1000);
                                if(progress > maxProbability10){
                                    progress = (int)(maxProbability10);
                                    seekBar.setProgress(progress);
                                    return;
                                }
                                break;
                            case 0b11:
                                break;
                        }
                        // 更新概率
                        double newProbability = progress / 1000.0;
                        array.setProbability(object, newProbability);
                    }
                }catch (IllegalArgumentException e) {
                    Log.e("IllegalArgumentException", "输入值超出范围: " + e.getMessage(), e);
                } catch(IndexOutOfBoundsException e){
                    Log.e("IndexOutOfBoundsException", "索引超出范围: " + e.getMessage(), e);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekBars.add(seekBar);

        // 删除按钮
        ImageButton deleteButton = new ImageButton(context);
        deleteButton.setId(View.generateViewId());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams.weight = 0;
        deleteButton.setLayoutParams(buttonParams);
        deleteButton.setImageResource(android.R.drawable.ic_menu_delete);
        deleteButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        //绑定删除按钮事件
        final LinearLayout finalOuterLayout = outerLayout;
        deleteButton.setOnClickListener(v -> {
            parentContainer.removeView(finalOuterLayout);
            seekBars.remove(seekBar); // 同步清理其他控件
            probabilityInputs.remove(numberInput1);
            weightInputs.remove(numberInput2);
            try{
                array.remove(object);
            }catch (IllegalArgumentException e) {
                Log.e("IllegalArgumentException", "输入值超出范围: " + e.getMessage(), e);
            } catch(IndexOutOfBoundsException e){
                Log.e("IndexOutOfBoundsException", "索引超出范围: " + e.getMessage(), e);
            }
        });

        // 添加元素
        row2.addView(checkBox);
        row2.addView(seekBar);
        row2.addView(deleteButton);

        // 将两排添加到外层布局
        outerLayout.addView(row1);
        outerLayout.addView(row2);

        return outerLayout;
    }
    public ProbabilityArray getProbabilityArray() {
        return dataArray; // 直接返回内部维护的概率数组
    }
}
