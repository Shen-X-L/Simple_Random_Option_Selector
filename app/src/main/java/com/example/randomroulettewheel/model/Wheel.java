
package com.example.randomroulettewheel.model;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;

import com.example.randomroulettewheel.views.WheelView;

public class Wheel {
    private static final double FRICTION = 0.99; // 摩擦系数
    private static final long FRAME_RATE = 16; // 约60fps
    private final ProbabilityArray array;//数据
    private final WheelView wheelView;//轮盘视图
    private final Handler handler;//用于在主线程（UI线程）上调度和执行动画帧更新
    private final Runnable updateRunnable;//定义每一帧的动画逻辑（更新角度、应用摩擦力等）
    public double angleVelocity = 0;//角速度
    public double angle = 0;//角度
    public int[] color = {Color.RED,Color.YELLOW,Color.GREEN,Color.CYAN,Color.BLUE,Color.MAGENTA};//色彩值
    private boolean isSpinning = false;

    //初始化
    public Wheel(WheelView wheelView, ProbabilityArray array) {
        //设置轮盘视图
        this.wheelView = wheelView;
        //设置数据
        this.array = array;

        this.handler = new Handler(Looper.getMainLooper());

        this.updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isSpinning) {
                    update();
                    wheelView.setRotation((float) angle); // 更新UI
                    handler.postDelayed(this, FRAME_RATE);// 循环调用
                }
            }
        };
        //初始化
        initWheel();
    }
    //初始化
    private void initWheel() {
        for(int i = 0;i < array.size();++i){
            //轮盘选项赋值
            wheelView.addSector(color[i % 6],(float)array.getProbability(i) * 360,array.getOptionName(i));
        }
    }
    //赋值速度启动
    public void rotateWheel(double initialVelocity) {
        //设置速度
        this.angleVelocity = initialVelocity;
        //如果停止 换成启动
        if (!isSpinning) {
            isSpinning = true;
            handler.post(updateRunnable);// 开始动画循环
        }
    }
    //叠加加速度
    public void applyAcceleration(double acceleration) {
        //速度上叠加速度
        angleVelocity += acceleration;
        if (!isSpinning) {
            //如果停止 换成启动
            isSpinning = true;
            handler.post(updateRunnable);// 开始动画循环
        }
    }
    //更新数据
    private void update() {
        // 应用摩擦力
        angleVelocity *= FRICTION;
        // 如果速度很小，停止旋转
        if (Math.abs(angleVelocity) < 0.1) {
            angleVelocity = 0;
            isSpinning = false;
            // 这里可以添加选中结果的逻辑
            return;
        }
        // 更新角度
        angle += angleVelocity;
        angle %= 360; // 保持角度在0-360范围内
        // 更新视图
        wheelView.setRotation((float) angle);
    }
    //停止
    public void stop() {
        isSpinning = false;
        handler.removeCallbacks(updateRunnable);// 停止动画循环
    }

    public void setupTouchListener() {
        wheelView.setWheelTouchListener(new WheelView.OnTouchListener() {
            @Override
            public void onTouchStart() {
                // 触摸开始时可以停止自动减速
                isSpinning = true; // 保持旋转状态
            }
            @Override
            public void onTouchMove(Vector2D previousPoint, Vector2D currentPoint, double deltaTime) {
                // 计算用户手势产生的角速度
                Vector2D center = new Vector2D(wheelView.getWidth() / 2f, wheelView.getHeight() / 2f);
                Vector2D v1 = previousPoint.subtract(center).normalize();
                Vector2D v2 = currentPoint.subtract(previousPoint).divide(deltaTime);
                double userAngleVelocity = v1.cross(v2);

                // 应用加速度 (灵敏度可以调整)
                double acceleration = userAngleVelocity * 0.1;
                applyAcceleration(acceleration);
            }

            @Override
            public void onTouchEnd() {
                // 触摸结束时恢复自动减速
                isSpinning = true;
            }
        });
    }
}
