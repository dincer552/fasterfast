package com.dincer552.testpak2;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public final class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(12, 18, 28));
        getWindow().setNavigationBarColor(Color.rgb(12, 18, 28));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(new MiniGame());
    }

    final class MiniGame extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final SharedPreferences preferences =
                getSharedPreferences("mini_zipla_scores", MODE_PRIVATE);

        private float playerX, playerY, jumpVelocity;
        private float obstacleX, speed = 7.5f;
        private int score = 0;
        private int best = 0;
        private boolean started = false;
        private boolean over = false;
        private boolean initialized = false;
        private long lastFrame = 0L;

        MiniGame() {
            super(MainActivity.this);
            setBackgroundColor(Color.rgb(12, 18, 28));
            best = preferences.getInt("best_score", 0);
        }

        private float groundY() {
            return getHeight() * 0.78f;
        }

        @Override protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
            super.onSizeChanged(width, height, oldWidth, oldHeight);
            if (width <= 0 || height <= 0) return;
            playerX = width * 0.22f;
            playerY = groundY();
            obstacleX = width + 80f;
            initialized = true;
        }

        private void reset() {
            playerX = getWidth() * 0.22f;
            playerY = groundY();
            jumpVelocity = 0f;
            obstacleX = getWidth() + 80f;
            score = 0;
            speed = 7.5f;
            started = true;
            over = false;
            lastFrame = System.currentTimeMillis();
            invalidate();
        }

        private void drawCenteredText(Canvas canvas, String value, float y, float size, int color) {
            paint.setColor(color);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(size);
            canvas.drawText(value, getWidth() / 2f, y, paint);
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (!initialized) {
                playerX = getWidth() * 0.22f;
                playerY = groundY();
                obstacleX = getWidth() + 80f;
                initialized = true;
            }

            float width = getWidth();
            float height = getHeight();
            float ground = groundY();

            canvas.drawColor(Color.rgb(12, 18, 28));

            paint.setColor(Color.rgb(24, 35, 49));
            canvas.drawCircle(width * 0.18f, height * 0.32f, width * 0.20f, paint);
            canvas.drawCircle(width * 0.88f, height * 0.22f, width * 0.14f, paint);

            paint.setColor(Color.rgb(39, 54, 72));
            canvas.drawRect(0, ground, width, height, paint);
            paint.setColor(Color.rgb(65, 88, 111));
            canvas.drawRect(0, ground, width, ground + 5f, paint);

            // Obstacle
            paint.setColor(Color.rgb(255, 170, 60));
            canvas.drawRoundRect(obstacleX - 19f, ground - 58f, obstacleX + 19f,
                    ground, 10f, 10f, paint);

            // Player
            paint.setColor(Color.rgb(50, 220, 150));
            canvas.drawRoundRect(playerX - 21f, playerY - 49f, playerX + 21f,
                    playerY, 12f, 12f, paint);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(playerX + 8f, playerY - 34f, 3.2f, paint);

            float titleSize = Math.max(22f, Math.min(34f, width * 0.052f));
            float scoreSize = Math.max(16f, Math.min(23f, width * 0.034f));
            drawCenteredText(canvas, "MINI ZIPLA", height * 0.12f, titleSize, Color.WHITE);
            drawCenteredText(canvas, "SKOR: " + score + "     REKOR: " + best,
                    height * 0.20f, scoreSize, Color.rgb(207, 220, 232));

            if (!started) {
                drawCenteredText(canvas, over ? "OYUN BİTTİ" : "DOKUN VE BAŞLA",
                        height * 0.48f, Math.max(21f, width * 0.043f),
                        Color.rgb(50, 220, 150));
                drawCenteredText(canvas,
                        over ? "Yeniden başlamak için dokun" : "Zıplamak için ekrana dokun",
                        height * 0.55f, Math.max(15f, width * 0.030f), Color.WHITE);
                return;
            }

            long now = System.currentTimeMillis();
            float delta = Math.max(0.5f, Math.min(2f, (now - lastFrame) / 16f));
            lastFrame = now;

            obstacleX -= speed * delta;
            if (obstacleX < -45f) {
                obstacleX = width + 45f;
                score++;
                speed = Math.min(19f, speed + 0.30f);
                if (score > best) {
                    best = score;
                    preferences.edit().putInt("best_score", best).apply();
                }
            }

            // Smooth jump arc with gravity and a safe landing clamp.
            if (jumpVelocity != 0f || playerY < ground) {
                jumpVelocity += 0.90f * delta;
                playerY += jumpVelocity * delta;
                if (playerY >= ground) {
                    playerY = ground;
                    jumpVelocity = 0f;
                }
            }

            boolean horizontalHit = Math.abs(obstacleX - playerX) < 40f;
            boolean playerOnGround = playerY > ground - 58f;
            if (horizontalHit && playerOnGround) {
                over = true;
                started = false;
                postInvalidate();
            } else {
                postInvalidateDelayed(16L);
            }
        }

        @Override public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() != MotionEvent.ACTION_DOWN) return true;

            if (!started) {
                reset();
                return true;
            }

            if (playerY >= groundY() - 1f && jumpVelocity == 0f) {
                jumpVelocity = -16.5f;
            }
            invalidate();
            return true;
        }
    }
}
