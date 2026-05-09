package com.runningwater.engine;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Mengelola animasi berbasis frame.
 * Untuk versi ini animasi digambar sebagai bentuk geometri berwarna karena tidak ada sprite PNG eksternal.
 */
public class AnimationPlayer {

    public enum AnimState { IDLE, ATTACK, HURT, DEAD }

    private AnimState state      = AnimState.IDLE;
    private double    timer      = 0;
    private int       frame      = 0;

    // Durasi tiap frame dalam detik
    private static final double IDLE_FRAME_DUR   = 0.5;
    private static final double ATTACK_FRAME_DUR = 0.08;
    private static final double HURT_FRAME_DUR   = 0.1;
    private static final double DEAD_FRAME_DUR   = 0.12;

    // Jumlah frame tiap animasi
    private static final int IDLE_FRAMES   = 2;
    private static final int ATTACK_FRAMES = 5;
    private static final int HURT_FRAMES   = 3;
    private static final int DEAD_FRAMES   = 4;

    private double offsetX = 0; // geser horizontal saat animasi serangan

    public void setState(AnimState newState) {
        if (this.state != newState) {
            this.state  = newState;
            this.frame  = 0;
            this.timer  = 0;
            this.offsetX = 0;
        }
    }

    public void update(double dt) {
        double dur = frameDuration();
        timer += dt;
        if (timer >= dur) {
            timer -= dur;
            int maxFrames = maxFrames();
            frame++;
            if (frame >= maxFrames) {
                if (state == AnimState.ATTACK || state == AnimState.HURT) {
                    state  = AnimState.IDLE;
                    frame  = 0;
                    offsetX = 0;
                } else if (state == AnimState.DEAD) {
                    frame = maxFrames - 1; // beku di frame terakhir
                } else {
                    frame = 0; // loop idle
                }
            }
            // Animasi serangan: maju lalu mundur
            if (state == AnimState.ATTACK) {
                offsetX = (frame < ATTACK_FRAMES / 2) ? frame * 12.0 : (ATTACK_FRAMES - frame) * 12.0;
            }
        }
    }

    /**
     * Gambar karakter sebagai shapes geometri berwarna.
     *
     * @param gc      GraphicsContext kanvas
     * @param cx      pusat X
     * @param cy      pusat Y
     * @param w       lebar area karakter
     * @param h       tinggi area karakter
     * @param baseCol warna utama karakter
     * @param flipX   true = cermin horizontal (untuk musuh di sisi kanan)
     */
    public void render(GraphicsContext gc, double cx, double cy,
                       double w, double h, Color baseCol, boolean flipX) {
        double dx = flipX ? -offsetX : offsetX;
        double x  = cx - w / 2 + dx;

        gc.save();

        // -- Efek hurt: flash putih
        Color drawCol = baseCol;
        if (state == AnimState.HURT) {
            drawCol = (frame % 2 == 0) ? Color.WHITE : baseCol;
        }

        // -- Efek dead: fade ke abu-abu + miring
        if (state == AnimState.DEAD) {
            double t = (double) frame / DEAD_FRAMES;
            drawCol = baseCol.interpolate(Color.GRAY, t);
            gc.translate(cx, cy + h * 0.4);
            gc.rotate(flipX ? -70 * t : 70 * t);
            gc.translate(-cx, -(cy + h * 0.4));
        }

        // -- Idle bob: naik-turun kecil
        double bob = 0;
        if (state == AnimState.IDLE) {
            bob = Math.sin(frame * Math.PI) * 3.0;
        }

        double bodyY = cy - h / 2 + bob;

        // Kaki
        gc.setFill(drawCol.darker());
        gc.fillRoundRect(x + w * 0.2, bodyY + h * 0.7, w * 0.22, h * 0.3, 6, 6);
        gc.fillRoundRect(x + w * 0.58, bodyY + h * 0.7, w * 0.22, h * 0.3, 6, 6);

        // Badan
        gc.setFill(drawCol);
        gc.fillRoundRect(x + w * 0.15, bodyY + h * 0.35, w * 0.7, h * 0.38, 10, 10);

        // Kepala
        gc.fillOval(x + w * 0.2, bodyY, w * 0.6, h * 0.4);

        // Mata
        gc.setFill(Color.WHITE);
        if (!flipX) {
            gc.fillOval(x + w * 0.55, bodyY + h * 0.1, w * 0.15, h * 0.15);
            gc.setFill(Color.BLACK);
            gc.fillOval(x + w * 0.58, bodyY + h * 0.12, w * 0.08, h * 0.09);
        } else {
            gc.fillOval(x + w * 0.3, bodyY + h * 0.1, w * 0.15, h * 0.15);
            gc.setFill(Color.BLACK);
            gc.fillOval(x + w * 0.34, bodyY + h * 0.12, w * 0.08, h * 0.09);
        }

        gc.restore();
    }

    public boolean isDead()    { return state == AnimState.DEAD; }
    public boolean isIdle()    { return state == AnimState.IDLE; }
    public AnimState getState(){ return state; }

    private double frameDuration() {
        switch (state) {
            case ATTACK: return ATTACK_FRAME_DUR;
            case HURT:   return HURT_FRAME_DUR;
            case DEAD:   return DEAD_FRAME_DUR;
            default:     return IDLE_FRAME_DUR;
        }
    }

    private int maxFrames() {
        switch (state) {
            case ATTACK: return ATTACK_FRAMES;
            case HURT:   return HURT_FRAMES;
            case DEAD:   return DEAD_FRAMES;
            default:     return IDLE_FRAMES;
        }
    }
}
