package com.dincer552.testpak2;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Random;

public final class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(255, 226, 190));
        getWindow().setNavigationBarColor(Color.rgb(255, 239, 218));
        getWindow().getDecorView().setSystemUiVisibility(0);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(new SunnySipGame());
    }

    final class SunnySipGame extends View {
        private static final float W = 390f;
        private static final float H = 844f;
        private static final int C_ARRIVING = 0;
        private static final int C_WAITING = 1;
        private static final int C_ANGRY = 2;
        private static final int C_BANGING = 3;
        private static final int C_DRINKING = 4;
        private static final int C_LEAVING = 5;
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint t = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Random random = new Random();
        private final ArrayList<Customer> customers = new ArrayList<>();
        private final ArrayList<FloatParticle> particles = new ArrayList<>();
        private final int[] ingredientColors = {
                Color.rgb(157, 224, 255), Color.rgb(255, 221, 83),
                Color.rgb(241, 91, 132), Color.rgb(112, 79, 62),
                Color.rgb(255, 244, 220), Color.rgb(67, 179, 120),
                Color.rgb(255, 157, 55)
        };
        private final String[] ingredientNames = {
                "BUZ", "LİMON", "ÇİLEK", "KAHVE", "SÜT", "NANE", "PORTAKAL"
        };
        private final String[] drinkNames = {
                "Limonata", "Çilek Fizz", "Buzlu Kahve", "Tropik Rüya"
        };
        private final String[] drinkShort = {
                "LİMONATA", "ÇİLEK FIZZ", "BUZLU KAHVE", "TROPİK"
        };
        private final int[] drinkColors = {
                Color.rgb(255, 216, 91), Color.rgb(247, 104, 143),
                Color.rgb(154, 105, 77), Color.rgb(255, 159, 69)
        };
        private final int[][] recipes = {
                {0, 1, 5}, {0, 2, 5}, {0, 3, 4}, {0, 6, 2}
        };
        private int selectedCustomer = 0;
        private int coins = 0;
        private int served = 0;
        private int level = 1;
        private int combo = 0;
        private int[] glassIngredients = new int[0];
        private boolean mixed = false;
        private int recipeStep = 0;
        private int quality = 100;
        private int qualityMistakes = 0;
        private static final int MINIMUM_PAID_QUALITY = 60;
        private String message = "Hoş geldin! İlk siparişini hazırla.";
        private long messageUntil = System.currentTimeMillis() + 3500L;
        private long lastFrame = System.currentTimeMillis();
        private float sceneTime = 0f;
        private float scale = 1f;
        private float offsetX = 0f;
        private float offsetY = 0f;

        SunnySipGame() {
            super(MainActivity.this);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            for (int i = 0; i < 3; i++) customers.add(makeCustomer(i));
        }

        private Customer makeCustomer(int slot) {
            int drink = random.nextInt(drinkNames.length);
            String[] names = {"MİNA", "ARAS", "LARA", "DENİZ", "EMİR", "ELA", "MERT", "ADA"};
            int[] skin = {Color.rgb(224, 164, 119), Color.rgb(190, 123, 83),
                    Color.rgb(245, 196, 151), Color.rgb(133, 85, 62)};
            int[] shirts = {Color.rgb(255, 132, 116), Color.rgb(85, 171, 145),
                    Color.rgb(103, 145, 220), Color.rgb(243, 190, 81)};
            Customer customer = new Customer(names[random.nextInt(names.length)],
                    skin[random.nextInt(skin.length)],
                    shirts[random.nextInt(shirts.length)],
                    drink, 1f, random.nextFloat() * 6.28f, slot);
            customer.entryX = (slot == 1) ? W + 78f : -78f;
            customer.exitX = (slot == 1) ? -86f : W + 86f;
            customer.x = customer.entryX;
            customer.enterDelay = slot * 0.32f;
            customer.state = C_ARRIVING;
            return customer;
        }

        private float seatX(int slot) {
            return 70f + slot * 125f;
        }

        private boolean customerCanOrder(Customer customer) {
            return customer.state == C_WAITING || customer.state == C_ANGRY
                    || customer.state == C_BANGING;
        }

        private boolean selectedCustomerCanOrder() {
            return selectedCustomer >= 0 && selectedCustomer < customers.size()
                    && customerCanOrder(customers.get(selectedCustomer));
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float sx = getWidth() / W;
            float sy = getHeight() / H;
            scale = Math.min(sx, sy);
            offsetX = (getWidth() - W * scale) * 0.5f;
            offsetY = (getHeight() - H * scale) * 0.5f;
            canvas.save();
            canvas.translate(offsetX, offsetY);
            canvas.scale(scale, scale);

            long now = System.currentTimeMillis();
            float dt = Math.min(0.05f, Math.max(0.008f, (now - lastFrame) / 1000f));
            lastFrame = now;
            sceneTime += dt;
            update(dt, now);

            drawBackground(canvas);
            drawHeader(canvas);
            drawCustomers(canvas);
            drawOrderTicket(canvas);
            drawCounter(canvas);
            drawGlass(canvas);
            drawIngredients(canvas);
            drawActions(canvas);
            drawParticles(canvas);
            drawToast(canvas, now);

            canvas.restore();
            postInvalidateDelayed(16L);
        }

        private void update(float dt, long now) {
            for (int i = 0; i < customers.size(); i++) {
                Customer c = customers.get(i);
                c.bob = (float) Math.sin(sceneTime * 2.4f + c.phase) * 2.6f;
                c.blink = (float) Math.max(0f, Math.sin(sceneTime * 1.7f + c.phase * 2f));

                if (c.enterDelay > 0f) {
                    c.enterDelay = Math.max(0f, c.enterDelay - dt);
                    continue;
                }

                switch (c.state) {
                    case C_ARRIVING:
                        c.enterProgress = Math.min(1f, c.enterProgress + dt / 1.35f);
                        float eased = c.enterProgress * c.enterProgress * (3f - 2f * c.enterProgress);
                        c.x = c.entryX + (seatX(i) - c.entryX) * eased;
                        if (c.enterProgress >= 1f) {
                            c.state = C_WAITING;
                            c.x = seatX(i);
                        }
                        break;

                    case C_WAITING:
                        c.patience = Math.max(0f, c.patience - dt * (0.047f + served * 0.00012f));
                        if (c.patience <= 0.46f) {
                            c.state = C_ANGRY;
                            c.angerTimer = 0f;
                        }
                        break;

                    case C_ANGRY:
                        c.angerTimer += dt;
                        if (c.rejecting) {
                            if (c.angerTimer >= 1.6f) {
                                c.state = C_LEAVING;
                                c.leaveReason = true;
                            }
                        } else {
                            c.patience = Math.max(0f, c.patience - dt * 0.055f);
                            if (c.patience <= 0f || c.angerTimer >= 2.7f) {
                                c.state = C_BANGING;
                                c.angerTimer = 0f;
                                c.bangHits = 0;
                                if (i == selectedCustomer) {
                                    showMessage(c.name + " sabırsızlandı! Masaya vuruyor!");
                                }
                            }
                        }
                        break;

                    case C_BANGING:
                        c.angerTimer += dt;
                        c.bangPhase += dt * 15f;
                        c.bangHits = (int) (c.angerTimer * 2.4f);
                        if (c.angerTimer >= 7f) {
                            c.state = C_LEAVING;
                            c.leaveReason = true;
                            if (i == selectedCustomer) {
                                clearGlass();
                                combo = 0;
                                showMessage(c.name + " kızıp kafeden ayrıldı!");
                            }
                        }
                        break;

                    case C_DRINKING:
                        c.drinkTime -= dt;
                        c.sipPhase += dt * 4.2f;
                        if (c.drinkTime <= 0f) {
                            c.state = C_LEAVING;
                            c.leaveReason = false;
                            if (i == selectedCustomer) {
                                showMessage(c.name + " içeceğini bitirdi. Güle güle!");
                            }
                        }
                        break;

                    case C_LEAVING:
                        float direction = c.exitX > c.x ? 1f : -1f;
                        c.x += direction * 205f * dt;
                        c.walkPhase += dt * 11f;
                        if ((direction > 0f && c.x >= c.exitX)
                                || (direction < 0f && c.x <= c.exitX)) {
                            boolean wasSelected = i == selectedCustomer;
                            customers.set(i, makeCustomer(i));
                            if (wasSelected) clearGlass();
                        }
                        break;
                }
            }

            for (int i = particles.size() - 1; i >= 0; i--) {
                FloatParticle f = particles.get(i);
                f.x += f.vx * dt;
                f.y += f.vy * dt;
                f.vy -= 16f * dt;
                f.life -= dt;
                if (f.life <= 0f) particles.remove(i);
            }
        }

        private void drawBackground(Canvas c) {
            p.setShader(new android.graphics.LinearGradient(0, 0, 0, H,
                    new int[]{Color.rgb(130, 218, 238), Color.rgb(205, 239, 226),
                            Color.rgb(255, 225, 181), Color.rgb(246, 192, 139)},
                    null, android.graphics.Shader.TileMode.CLAMP));
            c.drawRect(0, 0, W, H, p);
            p.setShader(null);

            // Bright seaside, soft clouds and a distant island.
            p.setColor(Color.argb(150, 255, 255, 255));
            cloud(c, 47, 91, 0.85f);
            cloud(c, 304, 81, 0.68f);
            p.setColor(Color.rgb(96, 199, 205));
            c.drawOval(250, 115, 448, 210, p);
            p.setColor(Color.rgb(75, 174, 188));
            c.drawOval(-70, 139, 173, 223, p);
            p.setColor(Color.rgb(73, 185, 199));
            c.drawRect(0, 174, W, 252, p);
            p.setColor(Color.argb(90, 255, 255, 255));
            for (int i = 0; i < 7; i++) {
                float x = (i * 73f + sceneTime * 11f) % 440f - 25f;
                c.drawRoundRect(x, 197 + (i % 3) * 13, x + 28, 199 + (i % 3) * 13,
                        2, 2, p);
            }
            p.setColor(Color.rgb(246, 210, 157));
            c.drawRect(0, 235, W, H, p);

            // Palm trunks and leaves.
            p.setColor(Color.rgb(145, 99, 62));
            p.setStrokeWidth(10f);
            p.setStrokeCap(Paint.Cap.ROUND);
            c.drawLine(18, 190, 39, 61, p);
            c.drawLine(360, 196, 342, 53, p);
            p.setStrokeWidth(7f);
            p.setColor(Color.rgb(59, 151, 104));
            for (int i = 0; i < 6; i++) {
                float a = -2.8f + i * 0.56f;
                c.drawLine(39, 65, 39 + (float) Math.cos(a) * 62,
                        65 + (float) Math.sin(a) * 42, p);
                c.drawLine(342, 58, 342 + (float) Math.cos(a) * 57,
                        58 + (float) Math.sin(a) * 38, p);
            }
            p.setStrokeCap(Paint.Cap.BUTT);

            // Café awning.
            p.setColor(Color.rgb(255, 248, 225));
            round(c, 13, 22, 364, 61, 14);
            String[] awning = {"#F47E72", "#F8D66C", "#F47E72", "#F8D66C",
                    "#F47E72", "#F8D66C", "#F47E72"};
            for (int i = 0; i < awning.length; i++) {
                p.setColor(Color.parseColor(awning[i]));
                c.drawRect(18 + i * 50, 28, 65 + i * 50, 55, p);
                Path scallop = new Path();
                scallop.moveTo(18 + i * 50, 54);
                scallop.quadTo(41 + i * 50, 76, 65 + i * 50, 54);
                scallop.close();
                c.drawPath(scallop, p);
            }
            p.setColor(Color.argb(35, 83, 68, 45));
            c.drawRoundRect(14, 61, 376, 69, 4, 4, p);
        }

        private void cloud(Canvas c, float x, float y, float s) {
            c.drawCircle(x, y, 13 * s, p);
            c.drawCircle(x + 14 * s, y - 7 * s, 17 * s, p);
            c.drawCircle(x + 31 * s, y, 12 * s, p);
            c.drawRoundRect(x - 1 * s, y - 1 * s, x + 40 * s, y + 12 * s,
                    7 * s, 7 * s, p);
        }

        private void drawHeader(Canvas c) {
            p.setColor(Color.argb(70, 101, 85, 59));
            round(c, 18, 82, 372, 132, 18);
            p.setColor(Color.rgb(255, 250, 233));
            round(c, 15, 78, 375, 126, 18);
            t.setTextAlign(Paint.Align.LEFT);
            t.setTypeface(android.graphics.Typeface.create("sans-serif-rounded",
                    android.graphics.Typeface.BOLD));
            t.setTextSize(19);
            t.setColor(Color.rgb(88, 103, 74));
            c.drawText("SUNNY SIP BAR", 28, 108, t);
            t.setTextSize(10);
            t.setColor(Color.rgb(160, 139, 102));
            c.drawText("SAHİLİN EN TATLI KAFESİ", 29, 120, t);

            p.setColor(Color.rgb(255, 226, 157));
            round(c, 258, 87, 363, 118, 15);
            drawCoin(c, 273, 102, 8);
            t.setTextAlign(Paint.Align.LEFT);
            t.setTextSize(14);
            t.setColor(Color.rgb(111, 78, 43));
            c.drawText(String.valueOf(coins), 287, 107, t);

            p.setColor(Color.rgb(230, 245, 225));
            round(c, 190, 87, 247, 118, 14);
            t.setTextAlign(Paint.Align.CENTER);
            t.setTextSize(11);
            t.setColor(Color.rgb(76, 133, 91));
            c.drawText("LVL " + level, 218, 107, t);
        }

        private void drawCustomers(Canvas c) {
            t.setTextAlign(Paint.Align.CENTER);
            t.setTypeface(android.graphics.Typeface.create("sans-serif-rounded",
                    android.graphics.Typeface.BOLD));
            t.setTextSize(11);
            t.setColor(Color.rgb(93, 104, 79));
            c.drawText("MÜŞTERİLER • SİPARİŞ VERMEK İÇİN DOKUN", 195, 151, t);

            for (int i = 0; i < customers.size(); i++) {
                Customer customer = customers.get(i);
                float x = customer.x;
                float y = 220 + customer.bob;
                boolean active = i == selectedCustomer;
                boolean seated = customer.state == C_WAITING || customer.state == C_ANGRY
                        || customer.state == C_BANGING || customer.state == C_DRINKING;

                p.setColor(active ? Color.rgb(255, 250, 229) : Color.rgb(247, 232, 203));
                round(c, x - 53, 163, x + 53, 311, 18);
                if (active) {
                    p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth(3);
                    p.setColor(customer.state == C_ANGRY || customer.state == C_BANGING
                            ? Color.rgb(239, 105, 82) : Color.rgb(255, 164, 94));
                    round(c, x - 53, 163, x + 53, 311, 18);
                    p.setStyle(Paint.Style.FILL);
                }

                if (customer.state == C_WAITING || customer.state == C_ANGRY
                        || customer.state == C_BANGING) {
                    p.setColor(customer.state == C_BANGING
                            ? Color.rgb(255, 224, 215) : Color.WHITE);
                    round(c, x - 43, 171, x + 43, 198, 11);
                    if (customer.state == C_BANGING) {
                        t.setTextSize(9);
                        t.setColor(Color.rgb(202, 68, 57));
                        c.drawText("SİPARİŞİM!", x, 188, t);
                    } else {
                        t.setTextSize(9);
                        t.setColor(Color.rgb(112, 100, 79));
                        c.drawText(drinkShort[customer.drink], x, 188, t);
                    }
                } else if (customer.state == C_DRINKING) {
                    p.setColor(Color.rgb(227, 249, 218));
                    round(c, x - 34, 171, x + 34, 198, 11);
                    t.setTextSize(10);
                    t.setColor(Color.rgb(64, 146, 88));
                    c.drawText("YUMM!", x, 188, t);
                } else if (customer.state == C_ARRIVING) {
                    t.setTextSize(9);
                    t.setColor(Color.rgb(112, 100, 79));
                    c.drawText("GELİYOR...", x, 188, t);
                }

                if (customer.state != C_LEAVING) {
                    drawCustomer(c, x, y, customer, i);
                    drawCustomerTable(c, x, y, customer);
                }

                if (seated && customer.state != C_DRINKING) {
                    p.setColor(Color.rgb(225, 216, 190));
                    round(c, x - 36, 296, x + 36, 302, 3);
                    p.setColor(customer.state == C_ANGRY || customer.state == C_BANGING
                            ? Color.rgb(239, 103, 82)
                            : (customer.patience > 0.35f
                            ? Color.rgb(103, 191, 128) : Color.rgb(239, 116, 92)));
                    round(c, x - 36, 296,
                            x - 36 + 72 * clamp(customer.patience, 0, 1), 302, 3);
                }
                if (seated) {
                    t.setTextSize(9);
                    t.setColor(customer.state == C_BANGING
                            ? Color.rgb(196, 69, 59) : Color.rgb(116, 103, 79));
                    c.drawText(customer.name, x, 314, t);
                }
            }
        }

        private void drawCustomerTable(Canvas c, float x, float y, Customer customer) {
            float hit = customer.state == C_BANGING
                    ? 0.5f + 0.5f * (float) Math.sin(customer.bangPhase) : 0f;
            float shake = customer.state == C_BANGING
                    ? (float) Math.sin(customer.bangPhase * 1.03f) * 1.8f : 0f;
            c.save();
            c.translate(shake, hit * 1.8f);

            p.setColor(Color.argb(65, 87, 57, 32));
            c.drawOval(x - 40, y + 58, x + 40, y + 70, p);
            p.setColor(Color.rgb(148, 91, 55));
            c.drawRect(x - 31, y + 45, x - 26, y + 73, p);
            c.drawRect(x + 26, y + 45, x + 31, y + 73, p);
            p.setColor(Color.rgb(184, 112, 66));
            round(c, x - 43, y + 39, x + 43, y + 51, 6);
            p.setColor(Color.rgb(222, 151, 91));
            round(c, x - 43, y + 38, x + 43, y + 44, 5);
            p.setColor(Color.argb(70, 255, 236, 198));
            c.drawLine(x - 32, y + 41, x + 30, y + 41, p);

            p.setStrokeWidth(7f);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setColor(customer.skin);
            if (customer.state == C_BANGING) {
                float lift = hit * 13f;
                c.drawLine(x - 19, y + 22, x - 24, y + 40 - lift, p);
                c.drawLine(x + 19, y + 22, x + 24, y + 40 - lift, p);
                c.drawCircle(x - 24, y + 40 - lift, 6f, p);
                c.drawCircle(x + 24, y + 40 - lift, 6f, p);
                p.setColor(Color.argb(110, 255, 245, 225));
                c.drawCircle(x - 24, y + 45, 3 + hit * 3, p);
                c.drawCircle(x + 24, y + 45, 3 + hit * 3, p);
            } else if (customer.state == C_DRINKING) {
                float sip = 1.5f * (float) Math.sin(customer.sipPhase);
                c.drawLine(x + 20, y + 20, x + 28, y + 11 + sip, p);
                p.setColor(Color.WHITE);
                round(c, x + 23, y + 1 + sip, x + 35, y + 19 + sip, 4);
                p.setColor(drinkColors[customer.drink]);
                round(c, x + 24, y + 8 + sip, x + 34, y + 17 + sip, 3);
                p.setColor(Color.rgb(79, 165, 111));
                c.drawLine(x + 30, y + 2 + sip, x + 34, y - 7 + sip, p);
            } else {
                c.drawLine(x - 19, y + 22, x - 23, y + 41, p);
                c.drawLine(x + 19, y + 22, x + 23, y + 41, p);
                c.drawCircle(x - 23, y + 41, 5f, p);
                c.drawCircle(x + 23, y + 41, 5f, p);
            }
            p.setStrokeCap(Paint.Cap.BUTT);
            c.restore();

            if (customer.state == C_BANGING) {
                float pulse = (float) Math.sin(customer.bangPhase * 2f);
                p.setColor(Color.rgb(255, 99, 78));
                c.drawCircle(x + 37, y - 38, 8 + Math.max(0, pulse) * 2, p);
                t.setTextAlign(Paint.Align.CENTER);
                t.setTextSize(13);
                t.setColor(Color.WHITE);
                c.drawText("!", x + 37, y - 33, t);
            }
        }

        private void drawCustomer(Canvas c, float x, float y, Customer customer, int index) {
            // Body and neck, shaded as soft 3D shapes.
            p.setColor(Color.argb(35, 90, 74, 51));
            c.drawOval(x - 31, y + 42, x + 31, y + 53, p);
            p.setColor(customer.shirt);
            c.drawRoundRect(x - 30, y + 20, x + 30, y + 65, 18, 18, p);
            p.setColor(adjust(customer.shirt, 22));
            c.drawRoundRect(x - 22, y + 24, x - 7, y + 58, 8, 8, p);
            p.setColor(customer.skin);
            c.drawRoundRect(x - 8, y + 10, x + 8, y + 31, 6, 6, p);

            // Ears and face.
            c.drawCircle(x - 23, y - 5, 8, p);
            c.drawCircle(x + 23, y - 5, 8, p);
            c.drawOval(x - 24, y - 35, x + 24, y + 23, p);
            p.setColor(adjust(customer.skin, 24));
            c.drawOval(x - 18, y - 30, x - 5, y - 7, p);
            // Hair cap / hairstyle.
            p.setColor(index == 1 ? Color.rgb(93, 59, 43) : Color.rgb(73, 51, 42));
            Path hair = new Path();
            hair.moveTo(x - 25, y - 9);
            hair.quadTo(x - 31, y - 41, x - 3, y - 40);
            hair.quadTo(x + 23, y - 48, x + 26, y - 12);
            hair.quadTo(x + 13, y - 28, x + 5, y - 22);
            hair.quadTo(x - 8, y - 12, x - 25, y - 9);
            hair.close();
            c.drawPath(hair, p);

            // Expressions change from cheerful to annoyed when service is late.
            boolean angry = customer.state == C_ANGRY || customer.state == C_BANGING;
            p.setColor(Color.rgb(58, 51, 46));
            c.drawCircle(x - 9, y - 5, 2.2f, p);
            c.drawCircle(x + 9, y - 5, 2.2f, p);
            p.setColor(Color.argb(angry ? 135 : 80, 244, 107, 112));
            c.drawCircle(x - 15, y + 5, 5, p);
            c.drawCircle(x + 15, y + 5, 5, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2f);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setColor(Color.rgb(82, 52, 45));
            if (angry) {
                c.drawLine(x - 14, y - 13, x - 5, y - 10, p);
                c.drawLine(x + 5, y - 10, x + 14, y - 13, p);
                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.rgb(110, 52, 49));
                c.drawOval(x - 5, y + 4, x + 5, y + 15, p);
            } else {
                c.drawArc(x - 7, y + 1, x + 7, y + 14, 12, 155, false, p);
            }
            p.setStyle(Paint.Style.FILL);
            p.setStrokeCap(Paint.Cap.BUTT);
        }

        private void drawOrderTicket(Canvas c) {
            p.setColor(Color.argb(45, 104, 83, 55));
            round(c, 24, 325, 366, 385, 14);
            p.setColor(Color.rgb(255, 252, 237));
            round(c, 21, 321, 369, 379, 14);
            Customer current = customers.get(selectedCustomer);
            if (!customerCanOrder(current)) {
                p.setColor(Color.rgb(236, 247, 224));
                round(c, 31, 331, 83, 369, 10);
                t.setTextAlign(Paint.Align.CENTER);
                t.setTextSize(10);
                t.setColor(Color.rgb(85, 139, 91));
                String status = current.state == C_DRINKING ? "İÇİYOR" :
                        (current.state == C_LEAVING ? "GİDİYOR" : "GELİYOR");
                c.drawText(status, 57, 353, t);
                t.setTextAlign(Paint.Align.LEFT);
                t.setTextSize(10);
                t.setColor(Color.rgb(150, 131, 99));
                c.drawText("MÜŞTERİ DURUMU", 94, 342, t);
                t.setTextSize(14);
                t.setColor(Color.rgb(91, 93, 69));
                c.drawText(current.name + " şu anda meşgul", 94, 361, t);
                return;
            }
            p.setColor(drinkColors[current.drink]);
            round(c, 31, 331, 83, 369, 10);
            drawDrinkIcon(c, 57, 350, current.drink, 14);
            t.setTextAlign(Paint.Align.LEFT);
            t.setTextSize(10);
            t.setColor(Color.rgb(150, 131, 99));
            c.drawText(current.state == C_BANGING ? "SABIRSIZ MÜŞTERİ" :
                    (current.state == C_ANGRY ? "MÜŞTERİ BEKLİYOR" : "SİPARİŞ"), 94, 342, t);
            t.setTextSize(15);
            t.setColor(current.state == C_BANGING ? Color.rgb(201, 68, 57) :
                    Color.rgb(91, 93, 69));
            c.drawText(drinkNames[current.drink], 94, 361, t);
            t.setTextSize(10);
            t.setColor(Color.rgb(137, 125, 101));
            c.drawText("3 malzeme • sırayla ekle, sonra karıştır", 220, 351, t);
            t.setTextAlign(Paint.Align.RIGHT);
            t.setTextSize(10);
            t.setColor(Color.rgb(114, 145, 102));
            c.drawText("+" + (12 + level * 2) + " coin", 354, 367, t);
        }

        private void drawCounter(Canvas c) {
            // Warm wood bar and rounded trim.
            p.setColor(Color.argb(40, 91, 64, 38));
            round(c, 10, 405, 380, 637, 24);
            p.setColor(Color.rgb(190, 123, 76));
            round(c, 7, 397, 383, 625, 23);
            p.setColor(Color.rgb(225, 163, 103));
            round(c, 7, 397, 383, 434, 18);
            p.setColor(Color.rgb(160, 96, 58));
            round(c, 7, 608, 383, 635, 12);
            p.setColor(Color.argb(48, 255, 244, 203));
            for (int i = 0; i < 7; i++) {
                float y = 452 + i * 24;
                c.drawLine(22, y, 368, y + (i % 2 == 0 ? 2 : -2), p);
            }

            t.setTextAlign(Paint.Align.CENTER);
            t.setTextSize(12);
            t.setTypeface(android.graphics.Typeface.create("sans-serif-rounded",
                    android.graphics.Typeface.BOLD));
            t.setColor(Color.rgb(255, 248, 225));
            c.drawText("İÇECEĞİNİ HAZIRLA", 195, 422, t);

            // Small decorative coffee machine and citrus bowl.
            p.setColor(Color.rgb(244, 234, 211));
            round(c, 27, 463, 92, 548, 12);
            p.setColor(Color.rgb(103, 145, 145));
            round(c, 36, 473, 83, 512, 8);
            p.setColor(Color.rgb(62, 94, 96));
            round(c, 43, 482, 76, 504, 5);
            p.setColor(Color.rgb(255, 239, 207));
            c.drawCircle(59, 528, 8, p);
            p.setColor(Color.rgb(104, 70, 50));
            c.drawCircle(59, 528, 4, p);

            p.setColor(Color.rgb(255, 232, 179));
            c.drawOval(299, 489, 366, 534, p);
            for (int i = 0; i < 4; i++) {
                p.setColor(ingredientColors[(i + 1) % ingredientColors.length]);
                c.drawCircle(314 + i * 12, 510 + (i % 2) * 5, 7, p);
            }
        }

        private void drawGlass(Canvas c) {
            float gx = 195, gy = 520;
            // Glass shadow, body and rim.
            p.setColor(Color.argb(50, 78, 54, 38));
            c.drawOval(gx - 48, gy + 79, gx + 48, gy + 94, p);
            Path glass = new Path();
            glass.moveTo(gx - 35, gy - 45);
            glass.lineTo(gx + 35, gy - 45);
            glass.lineTo(gx + 25, gy + 70);
            glass.quadTo(gx, gy + 83, gx - 25, gy + 70);
            glass.close();
            p.setColor(Color.argb(100, 255, 255, 255));
            c.drawPath(glass, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(3);
            p.setColor(Color.argb(220, 255, 255, 239));
            c.drawPath(glass, p);
            p.setStyle(Paint.Style.FILL);

            int drinkColor = glassIngredients.length == 0
                    ? Color.argb(70, 255, 255, 255)
                    : drinkColors[customers.get(selectedCustomer).drink];
            Path liquid = new Path();
            liquid.moveTo(gx - 30, gy - 12);
            liquid.lineTo(gx + 30, gy - 12);
            liquid.lineTo(gx + 23, gy + 67);
            liquid.quadTo(gx, gy + 77, gx - 23, gy + 67);
            liquid.close();
            p.setColor(glassIngredients.length == 0 ? Color.argb(28, 255, 255, 255) : drinkColor);
            c.drawPath(liquid, p);
            p.setColor(Color.argb(55, 255, 255, 255));
            c.drawRoundRect(gx - 24, gy - 5, gx - 13, gy + 50, 5, 5, p);

            // Ingredient cubes / fruit visible in the drink.
            for (int i = 0; i < glassIngredients.length; i++) {
                int ingredient = glassIngredients[i];
                float ix = gx - 16 + (i % 2) * 27;
                float iy = gy + 7 + (i / 2) * 21;
                p.setColor(ingredientColors[ingredient]);
                if (ingredient == 0) {
                    c.save();
                    c.rotate(18 + i * 9, ix, iy);
                    round(c, ix - 7, iy - 7, ix + 7, iy + 7, 3);
                    c.restore();
                } else {
                    c.drawCircle(ix, iy, 7, p);
                    p.setColor(Color.argb(110, 255, 255, 255));
                    c.drawCircle(ix - 2, iy - 2, 2.5f, p);
                }
            }

            // Straw and garnish.
            p.setColor(Color.rgb(255, 246, 218));
            p.setStrokeWidth(5);
            p.setStrokeCap(Paint.Cap.ROUND);
            c.drawLine(gx + 15, gy - 38, gx + 30, gy - 92, p);
            p.setStrokeCap(Paint.Cap.BUTT);
            p.setColor(Color.rgb(77, 173, 117));
            c.drawOval(gx + 20, gy - 90, gx + 41, gy - 79, p);
            p.setColor(Color.argb(180, 255, 255, 255));
            c.drawOval(gx - 36, gy - 49, gx + 36, gy - 40, p);

            t.setTextAlign(Paint.Align.CENTER);
            t.setTextSize(10);
            t.setColor(Color.rgb(255, 250, 228));
            String status = mixed ? "HAZIR! KALİTE " + quality + "%"
                    : glassIngredients.length + "/3 MALZEME";
            c.drawText(status, gx, gy + 111, t);

            // Ingredient slots.
            for (int i = 0; i < 3; i++) {
                float x = 151 + i * 44;
                p.setColor(Color.argb(100, 255, 248, 226));
                round(c, x, 593, x + 37, 620, 8);
                if (i < glassIngredients.length) {
                    p.setColor(ingredientColors[glassIngredients[i]]);
                    c.drawCircle(x + 18, 606, 9, p);
                } else {
                    t.setTextSize(10);
                    t.setColor(Color.argb(160, 255, 250, 231));
                    c.drawText(String.valueOf(i + 1), x + 18, 610, t);
                }
            }
        }

        private void drawIngredients(Canvas c) {
            t.setTextAlign(Paint.Align.LEFT);
            t.setTextSize(11);
            t.setTypeface(android.graphics.Typeface.create("sans-serif-rounded",
                    android.graphics.Typeface.BOLD));
            t.setColor(Color.rgb(103, 106, 76));
            c.drawText("RAF", 20, 651, t);

            float[] xs = {18, 108};
            float[] ys = {660, 695, 730, 765};
            for (int i = 0; i < ingredientNames.length; i++) {
                float x = xs[i % 2], y = ys[i / 2];
                boolean next = selectedCustomerCanOrder() && !mixed
                        && recipeStep < 3
                        && recipes[customers.get(selectedCustomer).drink][recipeStep] == i;
                p.setColor(next ? Color.rgb(224, 245, 211) : Color.rgb(255, 251, 236));
                round(c, x, y, x + 82, y + 30, 8);
                if (next) {
                    p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth(1.6f);
                    p.setColor(Color.rgb(93, 170, 105));
                    round(c, x, y, x + 82, y + 30, 8);
                    p.setStyle(Paint.Style.FILL);
                }
                p.setColor(ingredientColors[i]);
                c.drawCircle(x + 13, y + 15, 8, p);
                p.setColor(Color.argb(65, 75, 65, 45));
                c.drawCircle(x + 15, y + 17, 3, p);
                t.setTextAlign(Paint.Align.LEFT);
                t.setTextSize(8f);
                t.setTypeface(android.graphics.Typeface.create("sans-serif-rounded",
                        android.graphics.Typeface.BOLD));
                t.setColor(Color.rgb(92, 91, 73));
                c.drawText(ingredientNames[i], x + 25, y + 18, t);
                if (next) {
                    t.setTextAlign(Paint.Align.RIGHT);
                    t.setTextSize(9);
                    t.setColor(Color.rgb(65, 145, 83));
                    c.drawText("✓", x + 77, y + 19, t);
                }
            }
            drawRecipePanel(c);
        }

        private void drawRecipePanel(Canvas c) {
            float left = 202f;
            p.setColor(Color.argb(55, 83, 65, 44));
            round(c, left, 642, 377, 800, 13);
            p.setColor(Color.rgb(255, 249, 229));
            round(c, left, 639, 377, 797, 13);
            t.setTextAlign(Paint.Align.LEFT);
            t.setTypeface(android.graphics.Typeface.create("sans-serif-rounded",
                    android.graphics.Typeface.BOLD));
            t.setTextSize(10);
            t.setColor(Color.rgb(91, 104, 73));
            c.drawText("HAZIRLAMA SIRASI", left + 9, 655, t);

            boolean active = selectedCustomerCanOrder();
            int drink = active ? customers.get(selectedCustomer).drink : -1;
            for (int step = 0; step < 4; step++) {
                float y = 663 + step * 27;
                boolean done = active && (step < recipeStep || (step == 3 && mixed));
                boolean current = active && !mixed && step == recipeStep;
                p.setColor(done ? Color.rgb(219, 242, 213)
                        : (current ? Color.rgb(255, 229, 174) : Color.rgb(244, 238, 218)));
                round(c, left + 8, y, 369, y + 23, 7);
                p.setColor(done ? Color.rgb(82, 159, 91)
                        : (current ? Color.rgb(211, 143, 48) : Color.rgb(177, 165, 137)));
                c.drawCircle(left + 19, y + 11.5f, 7, p);
                t.setTextAlign(Paint.Align.CENTER);
                t.setTextSize(8);
                t.setColor(Color.WHITE);
                c.drawText(done ? "✓" : String.valueOf(step + 1), left + 19, y + 14, t);
                t.setTextAlign(Paint.Align.LEFT);
                t.setTextSize(8.2f);
                t.setColor(Color.rgb(91, 87, 68));
                String label = step < 3
                        ? (drink >= 0 ? ingredientNames[recipes[drink][step]] : "—")
                        : "KARIŞTIR";
                c.drawText(label, left + 32, y + 15, t);
                if (current) {
                    t.setTextAlign(Paint.Align.RIGHT);
                    t.setTextSize(7);
                    t.setColor(Color.rgb(177, 113, 37));
                    c.drawText("ŞİMDİ", 361, y + 14, t);
                }
            }

            t.setTextAlign(Paint.Align.LEFT);
            t.setTextSize(8);
            t.setColor(Color.rgb(112, 102, 78));
            c.drawText("KALİTE", left + 9, 781, t);
            p.setColor(Color.rgb(225, 216, 190));
            round(c, left + 48, 773, 361, 783, 5);
            int qualityColor = quality >= MINIMUM_PAID_QUALITY
                    ? Color.rgb(92, 176, 111) : Color.rgb(229, 91, 76);
            p.setColor(qualityColor);
            round(c, left + 48, 773, left + 48 + (159 * quality / 100f), 783, 5);
            t.setTextAlign(Paint.Align.RIGHT);
            t.setTextSize(8);
            t.setColor(qualityColor);
            c.drawText(quality + "%", 367, 793, t);
        }

        private void drawActions(Canvas c) {
            boolean available = selectedCustomerCanOrder();
            boolean canMix = available && recipeStep == 3 && !mixed;
            boolean canServe = available && mixed;
            p.setColor(canMix ? Color.rgb(104, 175, 143) : Color.rgb(178, 190, 161));
            round(c, 21, 808, 185, 839, 15);
            p.setColor(canServe ? Color.rgb(242, 131, 105) : Color.rgb(196, 174, 142));
            round(c, 201, 808, 369, 839, 15);
            t.setTextAlign(Paint.Align.CENTER);
            t.setTypeface(android.graphics.Typeface.create("sans-serif-rounded",
                    android.graphics.Typeface.BOLD));
            t.setTextSize(12);
            t.setColor(Color.WHITE);
            c.drawText("KARIŞTIR", 103, 828, t);
            c.drawText("MÜŞTERİYE VER", 285, 828, t);
        }

        private void drawParticles(Canvas c) {
            for (FloatParticle f : particles) {
                p.setColor(Color.argb((int) (220 * clamp(f.life / f.maxLife, 0, 1)),
                        Color.red(f.color), Color.green(f.color), Color.blue(f.color)));
                c.drawCircle(f.x, f.y, f.size, p);
            }
        }

        private void drawToast(Canvas c, long now) {
            if (now > messageUntil) return;
            p.setColor(Color.argb(220, 83, 105, 79));
            round(c, 35, 774, 355, 800, 13);
            t.setTextAlign(Paint.Align.CENTER);
            t.setTextSize(10);
            t.setTypeface(android.graphics.Typeface.create("sans-serif-rounded",
                    android.graphics.Typeface.BOLD));
            t.setColor(Color.WHITE);
            c.drawText(message, 195, 791, t);
        }

        private void drawDrinkIcon(Canvas c, float x, float y, int drink, float size) {
            p.setColor(Color.argb(80, 90, 70, 50));
            c.drawRoundRect(x - size * .7f, y - size, x + size * .7f, y + size,
                    5, 5, p);
            p.setColor(Color.WHITE);
            c.drawRoundRect(x - size * .62f, y - size * .9f,
                    x + size * .62f, y + size * .85f, 4, 4, p);
            p.setColor(drinkColors[drink]);
            c.drawRoundRect(x - size * .52f, y - size * .2f,
                    x + size * .52f, y + size * .72f, 3, 3, p);
            p.setColor(Color.rgb(255, 250, 224));
            c.drawLine(x + 2, y - size * .9f, x + size * .45f, y - size * 1.5f, p);
            p.setColor(Color.rgb(91, 171, 117));
            c.drawCircle(x - 3, y - size * .55f, 3, p);
        }

        private void drawCoin(Canvas c, float x, float y, float r) {
            p.setColor(Color.rgb(255, 192, 64));
            c.drawCircle(x, y, r, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(1.4f);
            p.setColor(Color.rgb(255, 239, 166));
            c.drawCircle(x, y, r - 2, p);
            p.setStyle(Paint.Style.FILL);
            t.setTextAlign(Paint.Align.CENTER);
            t.setTextSize(r * 1.3f);
            t.setColor(Color.rgb(151, 102, 36));
            c.drawText("$", x, y + r * .45f, t);
        }

        private void round(Canvas c, float l, float top, float r, float bottom, float radius) {
            c.drawRoundRect(l, top, r, bottom, radius, radius, p);
        }

        private int adjust(int color, int amount) {
            return Color.rgb(Math.min(255, Color.red(color) + amount),
                    Math.min(255, Color.green(color) + amount),
                    Math.min(255, Color.blue(color) + amount));
        }

        private float clamp(float v, float min, float max) {
            return Math.max(min, Math.min(max, v));
        }

        private void clearGlass() {
            glassIngredients = new int[0];
            mixed = false;
            recipeStep = 0;
            quality = 100;
            qualityMistakes = 0;
        }

        private void showMessage(String value) {
            message = value;
            messageUntil = System.currentTimeMillis() + 2100L;
        }

        private void recordWrongStep(String reason) {
            quality = Math.max(0, quality - 22);
            qualityMistakes++;
            showMessage(reason + " Kalite -" + 22 + "%");
        }

        private void addParticles(float x, float y, int color, int count) {
            for (int i = 0; i < count; i++) {
                float a = random.nextFloat() * 6.28318f;
                float speed = 22 + random.nextFloat() * 75;
                particles.add(new FloatParticle(x, y,
                        (float) Math.cos(a) * speed,
                        (float) Math.sin(a) * speed,
                        0.7f + random.nextFloat() * 0.7f,
                        2 + random.nextFloat() * 3, color));
            }
            if (particles.size() > 100) particles.subList(0, particles.size() - 100).clear();
        }

        private void tap(float x, float y) {
            if (y >= 163 && y <= 311) {
                for (int i = 0; i < customers.size(); i++) {
                    float cx = customers.get(i).x;
                    if (Math.abs(x - cx) < 54) {
                        Customer chosen = customers.get(i);
                        if (!customerCanOrder(chosen)) {
                            showMessage(chosen.state == C_DRINKING
                                    ? "Müşteri içeceğini keyifle içiyor."
                                    : "Müşteri henüz masasına oturmadı.");
                            return;
                        }
                        selectedCustomer = i;
                        clearGlass();
                        showMessage(chosen.state == C_BANGING
                                ? chosen.name + " çok sinirli! Siparişini yetiştir."
                                : chosen.name + " siparişini hazırlıyoruz!");
                        return;
                    }
                }
            }

            if (y >= 656 && y <= 798) {
                if (!selectedCustomerCanOrder()) {
                    showMessage("Önce masaya oturmuş bir müşteriyi seç!");
                    return;
                }
                float[] xs = {18, 108};
                float[] ys = {660, 695, 730, 765};
                for (int i = 0; i < ingredientNames.length; i++) {
                    float bx = xs[i % 2], by = ys[i / 2];
                    if (x >= bx && x <= bx + 82 && y >= by && y <= by + 30) {
                        if (mixed) {
                            showMessage("Yeni içecek için bardağı temizle!");
                            return;
                        }
                        if (recipeStep >= 3) {
                            showMessage("Malzemeler tamam. Şimdi KARIŞTIR!");
                            return;
                        }
                        int expected = recipes[customers.get(selectedCustomer).drink][recipeStep];
                        if (i != expected) {
                            recordWrongStep("Yanlış ürün! Sıradaki: " + ingredientNames[expected] + ".");
                            return;
                        }
                        int[] next = new int[glassIngredients.length + 1];
                        System.arraycopy(glassIngredients, 0, next, 0, glassIngredients.length);
                        next[next.length - 1] = i;
                        glassIngredients = next;
                        recipeStep++;
                        addParticles(195 + random.nextInt(35) - 17,
                                535 + random.nextInt(30), ingredientColors[i], 5);
                        showMessage(ingredientNames[i] + " doğru sırada eklendi!");
                        return;
                    }
                }
            }

            if (y >= 808 && y <= 844) {
                if (!selectedCustomerCanOrder()) {
                    showMessage("Bu müşteri şu anda sipariş alamıyor.");
                    return;
                }
                if (x < 195) {
                    if (recipeStep < 3) {
                        quality = Math.max(0, quality - 15);
                        qualityMistakes++;
                        int expected = recipes[customers.get(selectedCustomer).drink][recipeStep];
                        showMessage("Önce " + ingredientNames[expected] + " eklemelisin! Kalite -15%");
                    } else if (!mixed) {
                        mixed = true;
                        addParticles(195, 520, Color.rgb(255, 248, 203), 24);
                        showMessage("Doğru işlem! İçecek karıştırıldı. Kalite " + quality + "%");
                    }
                } else {
                    if (!mixed) {
                        showMessage("Önce tarifteki sırayı tamamla ve karıştır!");
                    } else {
                        Customer c = customers.get(selectedCustomer);
                        if (quality < MINIMUM_PAID_QUALITY) {
                            combo = 0;
                            c.state = C_ANGRY;
                            c.rejecting = true;
                            c.angerTimer = 0f;
                            clearGlass();
                            showMessage(c.name + " içeceği beğenmedi! Kalite düşük, para ödemiyor!");
                        } else {
                            int baseReward = 12 + level * 2 + Math.min(combo, 5) * 2;
                            int reward = Math.max(1, Math.round(baseReward * quality / 100f));
                            if (c.state == C_ANGRY || c.state == C_BANGING) reward = Math.max(1, reward - 4);
                            coins += reward;
                            served++;
                            combo++;
                            addParticles(195, 490, drinkColors[c.drink], 32);
                            c.state = C_DRINKING;
                            c.drinkTime = 3.8f;
                            c.sipPhase = 0f;
                            clearGlass();
                            if (served % 5 == 0) level++;
                            showMessage(c.name + " beğendi! Kaliteye göre +" + reward + " coin");
                        }
                    }
                }
            }
        }

        @Override public boolean onTouchEvent(MotionEvent event) {
            if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                float x = (event.getX() - offsetX) / scale;
                float y = (event.getY() - offsetY) / scale;
                if (x >= 0 && x <= W && y >= 0 && y <= H) tap(x, y);
                invalidate();
                return true;
            }
            return true;
        }

        private final class Customer {
            final String name;
            final int skin, shirt, drink, slot;
            final float phase;
            float patience;
            float bob;
            float x, entryX, exitX;
            float enterDelay, enterProgress;
            float angerTimer, bangPhase, drinkTime, sipPhase, walkPhase, blink;
            int state = C_ARRIVING;
            int bangHits;
            boolean leaveReason;
            boolean rejecting;

            Customer(String name, int skin, int shirt, int drink,
                     float patience, float phase, int slot) {
                this.name = name;
                this.skin = skin;
                this.shirt = shirt;
                this.drink = drink;
                this.patience = patience;
                this.phase = phase;
                this.slot = slot;
            }
        }

        private final class FloatParticle {
            float x, y, vx, vy, life;
            final float maxLife, size;
            final int color;
            FloatParticle(float x, float y, float vx, float vy, float life, float size, int color) {
                this.x = x; this.y = y; this.vx = vx; this.vy = vy;
                this.life = life; this.maxLife = life; this.size = size; this.color = color;
            }
        }
    }
}
