package com.example.randomroulettewheel.views;// views/ColorWheelView.java


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.randomroulettewheel.model.Vector2D;

import java.util.ArrayList;
import java.util.List;

public class WheelView extends View {
    private final Paint pointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG); // 指针画笔
    private final float pointerWidth = 10f; // 指针宽度
    private final float pointerLength = 40f; // 指针长度
    private final List<Sector> sectors = new ArrayList<>();//扇形信息List
    private float angle = 0f;//角度
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);//创建画笔 参数启用抗锯齿，使绘制边缘更平滑。
    public PointF center = new PointF();//中心的
    private float radius;//半径
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);//文字渲染画笔
    private long lastTouchTime = 0;//时间戳
    private Vector2D lastTouchPoint = null;//触摸点向量
    private OnTouchListener touchListener;//桥梁角色，将 WheelView 接收到的触摸事件传递给 Wheel 处理
    //扇形类
    public static class Sector {
        public final int color;//颜色
        public final float sweepAngle;//扇区角度
        public final String optionName;
        public Sector(int color, float sweepAngle,String optionName) {
            this.color = color;
            this.sweepAngle = sweepAngle;
            this.optionName = optionName;
        }
    }
    //构造函数
    public WheelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        //设置字符相关
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(40);
        textPaint.setTextAlign(Paint.Align.CENTER);
        // 初始化指针画笔
        pointerPaint.setStyle(Paint.Style.FILL);
    }
    //添加扇形
    public void addSector(int color, float sweepAngle,String optionName) {
        sectors.add(new Sector(color, sweepAngle, optionName));
        //触发视图重绘
        invalidate();
    }
    //设置角度
    public void setRotation(float angle) {
        this.angle = angle;
        invalidate();
    }
    //改变长宽时调用
    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        center.set(w / 2f, h / 2f);
        radius = Math.min(w, h) / 2f * 0.9f;
        textPaint.setTextSize(radius / 10f);
    }
    //设置触摸回调接口
    public void setWheelTouchListener(OnTouchListener listener) {
        this.touchListener = listener;
        setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                long currentTime = System.currentTimeMillis();
                Vector2D currentPoint = new Vector2D(event.getX(), event.getY());

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 记录第一次触摸
                        lastTouchTime = currentTime;
                        lastTouchPoint = currentPoint;
                        if (touchListener != null) {
                            touchListener.onTouchStart();
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // 计算时间差和坐标差
                        if (lastTouchPoint != null && lastTouchTime > 0) {
                            long deltaTime = currentTime - lastTouchTime;
                            // 限制60fps
                            if (deltaTime < 16) return true;
                            if (touchListener != null) {
                                touchListener.onTouchMove(lastTouchPoint, currentPoint, deltaTime / 1000.0);
                            }
                        }
                        // 更新记录
                        lastTouchTime = currentTime;
                        lastTouchPoint = currentPoint;
                        break;

                    case MotionEvent.ACTION_UP:
                        if (touchListener != null) {
                            touchListener.onTouchEnd();
                        }
                        // 重置记录
                        lastTouchTime = 0;
                        lastTouchPoint = null;
                        break;
                }
                return true;
            }
        });
    }

    // 触摸监听接口
    public interface OnTouchListener {
        void onTouchStart();
        void onTouchMove(Vector2D previousPoint, Vector2D currentPoint, double deltaTime);
        void onTouchEnd();
    }
    //开始绘制
    @Override
    protected void onDraw(Canvas canvas) {
        //保存视图
        canvas.save();
        //角度
        float startAngle = angle;
        //遍历绘制
        for (Sector sector : sectors) {
            paint.setColor(sector.color);
            canvas.drawArc(
                    center.x - radius, center.y - radius,//扇形所在矩形的左上角坐标
                    center.x + radius, center.y + radius,// 扇形所在矩形的右下角坐标
                    startAngle, sector.sweepAngle,// 起始角度（0°为x轴方向）扫过的角度（顺时针为正）
                    true, paint// 是否连接到圆心（true=扇形，false=弧形） 绘制使用的画笔
            );

            // 绘制文字
            float middleAngle = startAngle + sector.sweepAngle / 2;
            float textX = center.x + (float) (radius * 0.6 * Math.cos(Math.toRadians(middleAngle)));
            float textY = center.y + (float) (radius * 0.6 * Math.sin(Math.toRadians(middleAngle)));
            canvas.drawText(sector.optionName, textX, textY, textPaint);

            startAngle += sector.sweepAngle;
        }
        //绘制指针
        drawPointer(canvas);
        // 恢复画布状态
        canvas.restore();
    }
    private void drawPointer(Canvas canvas) {
        // 确保指针可见的配置
        pointerPaint.setColor(Color.RED); // 改为醒目的颜色
        pointerPaint.setStyle(Paint.Style.FILL);

        // 指针起点（圆顶部中点）
        float startX = center.x;
        float startY = center.y - radius;

        // 指针终点（向上延伸）
        float endX = center.x;
        float endY = startY - pointerLength;

        // 绘制指针线段
        canvas.drawLine(startX, startY, endX, endY, pointerPaint);

        // 绘制三角形箭头
        Path path = new Path();
        path.moveTo(endX - pointerWidth / 2, endY); // 左顶点
        path.lineTo(endX + pointerWidth / 2, endY); // 右顶点
        path.lineTo(endX, endY - pointerWidth);     // 上顶点
        path.close();
        canvas.drawPath(path, pointerPaint);
    }
}