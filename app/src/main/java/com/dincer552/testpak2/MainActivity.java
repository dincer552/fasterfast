package com.dincer552.testpak2;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import java.util.Random;

public final class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(new MiniGame());
    }

    final class MiniGame extends View {
        private final Paint paint = new Paint(3);
        private final Random random = new Random();
        private float playerX, playerY, obstacleX, obstacleY, speed = 8f;
        private int score = 0, best = 0;
        private boolean started = false, over = false;
        private long lastFrame;

        MiniGame() { super(MainActivity.this); setBackgroundColor(Color.rgb(12, 18, 28)); }

        private void reset() {
            playerX = getWidth() * .22f; playerY = getHeight() * .72f;
            obstacleX = getWidth() + 80; obstacleY = getHeight() * .72f;
            score = 0; speed = 8f; started = true; over = false;
            lastFrame = System.currentTimeMillis(); invalidate();
        }

        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            float w=getWidth(), h=getHeight(), ground=h*.78f;
            paint.setColor(Color.rgb(43,58,76)); c.drawRect(0,ground,w,h,paint);
            paint.setColor(Color.rgb(50,220,150)); c.drawCircle(playerX,playerY-28,27,paint);
            paint.setColor(Color.rgb(255,170,60)); c.drawRoundRect(obstacleX-22,ground-55,obstacleX+22,ground,12,12,paint);
            paint.setTextAlign(Paint.Align.CENTER); paint.setColor(Color.WHITE); paint.setTextSize(30);
            c.drawText("MINI ZIPLA",w/2,h*.12f,paint);
            paint.setTextSize(22); c.drawText("SKOR: "+score+"    REKOR: "+best,w/2,h*.20f,paint);
            if (!started || over) {
                paint.setColor(Color.rgb(50,220,150)); paint.setTextSize(25);
                c.drawText(over ? "OYUN BİTTİ" : "EKRANA DOKUN VE BAŞLA",w/2,h*.48f,paint);
                paint.setColor(Color.WHITE); paint.setTextSize(18);
                c.drawText(over ? "Tekrar oynamak için dokun" : "Engellerin üzerinden zıpla",w/2,h*.55f,paint);
                return;
            }
            long now=System.currentTimeMillis();
            float dt=Math.min(32,now-lastFrame)/16f; lastFrame=now;
            obstacleX -= speed*dt;
            if (obstacleX < -40) { obstacleX=w+40; score++; best=Math.max(best,score); speed=Math.min(19,speed+.35f); }
            if (Math.abs(obstacleX-playerX)<42 && playerY>ground-62) { over=true; started=false; }
            if (playerY < ground-65) playerY += 13*dt;
            postInvalidateDelayed(16);
        }

        @Override public boolean onTouchEvent(MotionEvent e) {
            if (e.getAction()!=MotionEvent.ACTION_DOWN) return true;
            if (!started) { reset(); return true; }
            float ground=getHeight()*.78f;
            if (playerY>=ground-65) playerY=ground-125;
            invalidate(); return true;
        }
    }
}
