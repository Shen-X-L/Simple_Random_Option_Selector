package com.example.randomroulettewheel.model;

import android.graphics.PointF;

public class Vector2D {
    public double x;
    public double y;
    public Vector2D(){
        x = 0;
        y = 0;
    }
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
    public Vector2D(PointF point){
        this.x = point.x;
        this.y = point.y;
    }
    // Getter/Setter
    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    // 向量加
    public Vector2D add(Vector2D other) {
        return new Vector2D(this.x + other.x, this.y + other.y);
    }
    // 向量减
    public Vector2D subtract(Vector2D other) {
        return new Vector2D(this.x - other.x, this.y - other.y);
    }
    // 向量乘
    public Vector2D multiply(double scalar) {
        return new Vector2D(this.x * scalar, this.y * scalar);
    }
    // 向量除
    public Vector2D divide(double scalar){
        return new Vector2D(this.x / scalar, this.y / scalar);
    }
    // 点积
    public double dot(Vector2D other) {
        return this.x * other.x + this.y * other.y;
    }
    // 叉积
    public double cross(Vector2D other) {
        return x * other.y - y * other.x;
    }
    // 模长
    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }
    // 归一化
    public Vector2D normalize() {
        double mag = magnitude();
        return new Vector2D(x / mag, y / mag);
    }

    // 通过模长与角度创建
    public static Vector2D fromPolar(double magnitude, double angle) {
        return new Vector2D(
                magnitude * Math.cos(angle),
                magnitude * Math.sin(angle)
        );
    }

    // 向量旋转
    public Vector2D rotate(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vector2D(
                x * cos - y * sin,
                x * sin + y * cos
        );
    }

    // 距离计算
    public double distanceTo(Vector2D other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // 角度计算
    public double angle() {
        return Math.atan2(y, x);
    }

    // 角度差
    public double angleBetween(Vector2D other) {
        return Math.acos(dot(other) / (magnitude() * other.magnitude()));
    }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f)", x, y);
    }
}