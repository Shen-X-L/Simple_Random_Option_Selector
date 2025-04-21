
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
    private WheelStopListener wheelStopListener;//停止时选项回调接口
    private WheelAngleVelocityListener wheelAngleVelocityListener;//速度回调接口
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
            if(i == array.size() - 1 && array.size() != 1 && array.size() % color.length == 1){
                //轮盘选项赋值
                wheelView.addSector(color[1],(float)array.getProbability(i) * 360,array.getOptionName(i));
            }else{
                wheelView.addSector(color[i % color.length],(float)array.getProbability(i) * 360,array.getOptionName(i));
            }
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
    //摩擦力的加速度
    private double calculateFrictionAcceleration() {
        //return -Math.signum(angleVelocity) * 0.01;
        if(Math.abs(angleVelocity) > 100) return -angleVelocity * 0.05;
        else return -angleVelocity * 0.005;
    }
    //计算触摸带来的加速度
    private double calculateTouchAcceleration(Vector2D previousPoint,Vector2D currentPoint,double deltaTime){
        Vector2D center = new Vector2D(wheelView.getWidth() / 2f, wheelView.getHeight() / 2f);
        Vector2D v1 = previousPoint.subtract(center);//指向中心的向量
        Vector2D v2 = currentPoint.subtract(previousPoint).divide(deltaTime);//速度向量
        double userVelocity = v1.normalize().cross(v2);//切线速度 顺时针为正
        double userAngleVelocity = userVelocity / v1.magnitude();
        //方向相反
        if(userAngleVelocity * angleVelocity < 0) {
            if (Math.abs(angleVelocity) > 500) return -Math.signum(angleVelocity) * 1;
            else return -Math.signum(angleVelocity) * 0.5;
        }else if(Math.abs(userAngleVelocity) < Math.abs(angleVelocity)) return -Math.signum(angleVelocity) * 0.25;
        else return Math.signum(userAngleVelocity) * 0.5;
    }
    //更新数据
    private void update() {
        // 应用摩擦力
        applyAcceleration(calculateFrictionAcceleration());
        // 如果速度很小，停止旋转
        wheelAngleVelocityListener.getWheelAngleVelocity(angleVelocity);
        if (Math.abs(angleVelocity) < 0.1) {
            angleVelocity = 0;
            stop();
            // 计算选中的选项
            if (wheelStopListener != null) {
                String selectedOption = calculateSelectedOption();
                wheelStopListener.onWheelStopped(selectedOption);
            }
            return;
        }
        wheelStopListener.onWheelStopped("正在旋转");
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
    //轮盘停止返回选项回调接口
    public interface WheelStopListener {
        void onWheelStopped(String selectedOption);
    }
    //设置轮盘停止返回选项回调接口
    public void setWheelStopListener(WheelStopListener listener) {
        this.wheelStopListener = listener;
    }
    //返回轮盘速度回调接口
    public interface  WheelAngleVelocityListener{
        void getWheelAngleVelocity(double angleVelocity);
    }
    //设置轮盘停止返回选项回调接口
    public void setWheelAngleVelocityListener(WheelAngleVelocityListener listener) {
        this.wheelAngleVelocityListener = listener;
    }
    //计算当前选中的选项
    private String calculateSelectedOption() {
        // 将角度转换到0-360范围
        double normalizedAngle = (360 - (angle + 90) % 360) % 360;
        // 区间下界
        float cumulativeAngle = 0;

        for (int i = 0; i < array.size(); i++) {
            // 区间长度
            float sectorAngle = (float) array.getProbability(i) * 360;
            if (normalizedAngle >= cumulativeAngle && normalizedAngle < cumulativeAngle + sectorAngle) {
                return array.getOptionName(i);
            }
            cumulativeAngle += sectorAngle;
        }
        return "未知选项";
    }
    //定义触摸回调接口
    public void setupTouchListener() {
        wheelView.setWheelTouchListener(new WheelView.OnTouchListener() {
            @Override
            public void onTouchStart() {
                // 触摸开始时可以停止自动减速
                isSpinning = true; // 保持旋转状态
                handler.post(updateRunnable);// 开始动画循环
            }
            @Override
            public void onTouchMove(Vector2D previousPoint, Vector2D currentPoint, double deltaTime) {
                // 计算用户手势产生的角速度 并应用
                applyAcceleration(calculateTouchAcceleration(
                        previousPoint,currentPoint,deltaTime));
            }

            @Override
            public void onTouchEnd() {
                // 触摸结束时恢复自动减速
                isSpinning = true;
                handler.post(updateRunnable);// 开始动画循环
            }
        });
    }
}
