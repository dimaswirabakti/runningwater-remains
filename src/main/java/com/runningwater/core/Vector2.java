package com.runningwater.core;

/**
 * Vektor 2 dimensi sederhana untuk merepresentasikan posisi/arah.
 */
public class Vector2 {
    private float x;
    private float y;

    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2() {
        this(0f, 0f);
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
}
