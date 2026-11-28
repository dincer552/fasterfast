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
import java.util.Locale;

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
                Color.rgb(157,224,255),Color.rgb(255,221,83),Color.rgb(241,91,132),Color.rgb(112,79,62),
                Color.rgb(255,244,220),Color.rgb(67,179,120),Color.rgb(255,157,55),Color.rgb(247,190,91),
                Color.rgb(236,239,225),Color.rgb(255,171,66),Color.rgb(111,205,220),Color.rgb(92,112,190),
                Color.rgb(232,190,139),Color.rgb(107,190,130),Color.rgb(183,119,65),Color.rgb(255,231,213),
                Color.rgb(244,174,128),Color.rgb(160,91,61),Color.rgb(116,190,111),Color.rgb(91,57,53)
        };
        private final String[] ingredientNames = {
                "BUZ","LİMON","ÇİLEK","KAHVE","SÜT","NANE","PORTAKAL","ANANAS","HİNDİSTAN","MANGO",
                "SODA","YABAN MERSİNİ","VANİLYA","LİME","KARAMEL","KREMA","ŞEFTALİ","ÇAY","SALATALIK","ÇİKOLATA"
        };
        private final int[] ingredientBuyPrices = {28,34,46,52,38,25,42,49,55,58,30,62,44,33,48,51,40,36,29,57};
        private final String[] drinkNames = {
                "Limonata","Çilek Fizz","Buzlu Kahve","Tropik Rüya","Mango Sunset","Orman Meyvesi",
                "Karamel Latte","Şeftali Ice Tea","Yeşil Detox","Çikolata Shake","Ananas Mojito","Kırmızı Velvet"
        };
        private final String[] drinkShort = {"LİMONATA","ÇİLEK FIZZ","BUZLU KAHVE","TROPİK","MANGO","ORMAN","KARAMEL","ŞEFTALİ","DETOX","ÇİKOLATA","MOJITO","VELVET"};
        private final int[] drinkColors = {
                Color.rgb(255,216,91),Color.rgb(247,104,143),Color.rgb(154,105,77),Color.rgb(255,159,69),
                Color.rgb(255,177,58),Color.rgb(118,101,180),Color.rgb(185,125,74),Color.rgb(244,171,133),
                Color.rgb(107,190,130),Color.rgb(102,60,55),Color.rgb(101,202,164),Color.rgb(186,70,100)
        };
        private final int[] drinkPrices = {38,52,58,66,72,75,68,54,62,78,70,95};
        private final int[][] recipes = {
                {1,0,5},{2,0,10,5},{3,4,0,12},{6,7,8,0,10},{9,6,0,5},{11,2,13,0},
                {3,4,14,12,15},{16,17,0,5},{18,1,5,0,13},{19,4,15,12,2,0},{7,13,5,0,10},{2,11,4,15,14,0}
        };
        private final int[] ingredientStock = new int[20];
        private int selectedCustomer = 0;
        private int coins = 4000;
        private int served = 0;
        private int level = 1;
        private int combo = 0;
        private int[] glassIngredients = new int[0];
        private boolean mixed = false;
        private int recipeStep = 0;
        private int quality = 100;
        private int qualityMistakes = 0;
        private float barRating = 5.0f;
        private boolean barClosed = false;
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
            for (int i = 0; i < ingredientStock.length; i++) ingredientStock[i] = 40;
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
            return customer.state == C_WAITING || (customer.state == C_ANGRY && !customer.rejecting)
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
            drawSelectedRecipe(canvas);
            drawIngredients(canvas);
            drawActions(canvas);
            drawParticles(canvas);
            drawToast(canvas, now);

            if(barClosed)drawClosedOverlay(canvas);
            canvas.restore();
            postInvalidateDelayed(16L);
        }

        private void update(float dt, long now) {
            for (int i = 0; i < customers.size(); i++) {
                Customer c = customers.get(i);
                if(barClosed&&c.state!=C_LEAVING){c.state=C_LEAVING;c.exitX=c.x<W*0.5f?-86f:W+86f;}
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
                            changeRating(-0.12f);
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
                            if(!barClosed)customers.set(i,makeCustomer(i));
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

            p.setColor(Color.rgb(255,226,157));round(c,252,87,365,118,15);drawCoin(c,266,102,8);
            t.setTextAlign(Paint.Align.LEFT);t.setTextSize(13);t.setColor(Color.rgb(111,78,43));c.drawText("$"+coins,279,107,t);
            p.setColor(barRating<2f?Color.rgb(255,220,214):Color.rgb(230,245,225));round(c,170,87,243,118,14);
            t.setTextAlign(Paint.Align.CENTER);t.setTextSize(10);t.setColor(barRating<2f?Color.rgb(194,65,55):Color.rgb(76,133,91));
            c.drawText("★ "+String.format(Locale.US,"%.2f",barRating),206,106,t);
            t.setTextAlign(Paint.Align.LEFT);t.setTextSize(8);t.setColor(Color.rgb(160,139,102));c.drawText("LVL "+level,29,132,t);
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
            p.setColor(Color.argb(45,104,83,55));round(c,18,321,372,391,14);p.setColor(Color.rgb(255,252,237));round(c,15,317,375,387,14);
            t.setTextAlign(Paint.Align.LEFT);t.setTypeface(android.graphics.Typeface.create("sans-serif-rounded",android.graphics.Typeface.BOLD));t.setTextSize(9);t.setColor(Color.rgb(120,105,78));
            c.drawText("MÜŞTERİ TALEPLERİ • SİPARİŞ KARTINI SEÇ",25,329,t);int visible=0;
            for(int i=0;i<customers.size();i++){Customer customer=customers.get(i);if(!customerCanOrder(customer))continue;
                float left=23+visible*116;boolean active=i==selectedCustomer;p.setColor(active?Color.rgb(255,231,185):Color.rgb(245,239,220));round(c,left,335,left+108,379,8);
                if(active){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setColor(Color.rgb(218,145,73));round(c,left,335,left+108,379,8);p.setStyle(Paint.Style.FILL);}
                p.setColor(drinkColors[customer.drink]);c.drawCircle(left+12,347,5,p);t.setTextAlign(Paint.Align.LEFT);t.setTextSize(8);t.setColor(Color.rgb(89,83,65));c.drawText(customer.name,left+22,350,t);
                t.setTextSize(8.5f);t.setColor(Color.rgb(84,91,69));c.drawText(drinkShort[customer.drink]+" $"+drinkPrices[customer.drink],left+7,363,t);
                t.setTextSize(7);t.setColor(customer.state==C_BANGING?Color.rgb(205,65,54):Color.rgb(132,117,91));c.drawText(customer.state==C_BANGING?"MASAYA VURUYOR":(customer.state==C_ANGRY?"SABIRSIZ":"SİPARİŞ BEKLİYOR"),left+7,374,t);
                visible++;if(visible==3)break;}
            if(visible==0){t.setTextAlign(Paint.Align.CENTER);t.setTextSize(12);t.setColor(Color.rgb(106,143,105));c.drawText(barClosed?"KAFE KAPANDI":"Yeni siparişler bekleniyor...",195,359,t);}
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
            Path glass = new Path();int cupStyle=selectedCustomer>=0&&selectedCustomer<customers.size()?customers.get(selectedCustomer).drink%5:0;
            if(cupStyle==0){glass.moveTo(gx-35,gy-45);glass.lineTo(gx+35,gy-45);glass.lineTo(gx+25,gy+70);glass.quadTo(gx,gy+83,gx-25,gy+70);}
            else if(cupStyle==1){glass.moveTo(gx-31,gy-45);glass.quadTo(gx,gy-52,gx+31,gy-45);glass.lineTo(gx+31,gy+65);glass.quadTo(gx,gy+82,gx-31,gy+65);}
            else if(cupStyle==2){glass.moveTo(gx-38,gy-42);glass.lineTo(gx+38,gy-42);glass.lineTo(gx+22,gy+72);glass.lineTo(gx-22,gy+72);}
            else if(cupStyle==3){glass.moveTo(gx-34,gy-45);glass.lineTo(gx+34,gy-45);glass.lineTo(gx+27,gy+68);glass.quadTo(gx,gy+88,gx-27,gy+68);}
            else{glass.moveTo(gx-29,gy-45);glass.lineTo(gx+29,gy-45);glass.lineTo(gx+36,gy+60);glass.quadTo(gx,gy+87,gx-36,gy+60);}glass.close();
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
                    : glassIngredients.length + "/" + (selectedCustomerCanOrder()?recipes[customers.get(selectedCustomer).drink].length:0) + " MALZEME";
            c.drawText(status, gx, gy + 111, t);

            // Six ingredient slots for recipes with up to six components.
            for (int i = 0; i < 6; i++) {
                float x = 99 + i * 32;
                p.setColor(Color.argb(100, 255, 248, 226));
                round(c, x, 593, x + 28, 620, 7);
                if (i < glassIngredients.length) {
                    p.setColor(ingredientColors[glassIngredients[i]]);
                    c.drawCircle(x + 14, 606, 8, p);
                } else {
                    t.setTextAlign(Paint.Align.CENTER);
                    t.setTextSize(9);
                    t.setColor(Color.argb(160, 255, 250, 231));
                    c.drawText(String.valueOf(i + 1), x + 14, 610, t);
                }
            }
        }

        private void drawSelectedRecipe(Canvas c) {
            p.setColor(Color.argb(65,83,65,44));round(c,12,625,378,649,8);
            p.setColor(Color.rgb(255,248,225));round(c,12,623,378,647,8);
            if(!selectedCustomerCanOrder()){
                t.setTextAlign(Paint.Align.CENTER);t.setTextSize(9);t.setColor(Color.rgb(116,105,80));
                c.drawText("TARİFİ GÖRMEK İÇİN MÜŞTERİ SİPARİŞİNİ SEÇ",195,639,t);return;
            }
            int[] recipe=recipes[customers.get(selectedCustomer).drink];
            float chipW=48f;
            for(int i=0;i<recipe.length+1;i++){
                float x=15+i*51f;
                boolean done=i<recipeStep||(i==recipe.length&&mixed);
                boolean current=i==recipeStep&&!mixed;
                p.setColor(done?Color.rgb(218,241,211):(current?Color.rgb(255,222,164):Color.rgb(239,232,211)));
                round(c,x,627,x+chipW,643,5);
                t.setTextAlign(Paint.Align.CENTER);t.setTextSize(6.8f);
                t.setColor(done?Color.rgb(66,137,79):(current?Color.rgb(137,88,34):Color.rgb(104,98,78)));
                String label=i<recipe.length?ingredientNames[recipe[i]]:"KARIŞTIR";
                if(label.length()>9)label=label.substring(0,9);
                c.drawText(label,x+chipW/2,638,t);
            }
        }

        private void drawIngredients(Canvas c) {
            t.setTextAlign(Paint.Align.LEFT);t.setTextSize(10);t.setTypeface(android.graphics.Typeface.create("sans-serif-rounded",android.graphics.Typeface.BOLD));t.setColor(Color.rgb(103,106,76));
            c.drawText("MALZEME RAFI • + İŞARETİNDEN 10 ADET YENİ STOK AL",15,651,t);float tileW=88f,tileH=25f;
            for(int i=0;i<ingredientNames.length;i++){int col=i%4,row=i/4;float x=12+col*92f,y=660+row*27f;
                boolean next=selectedCustomerCanOrder()&&!mixed&&recipeStep<recipes[customers.get(selectedCustomer).drink].length&&recipes[customers.get(selectedCustomer).drink][recipeStep]==i;
                p.setColor(next?Color.rgb(224,245,211):Color.rgb(255,251,236));round(c,x,y,x+tileW,y+tileH,7);
                if(next){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.3f);p.setColor(Color.rgb(93,170,105));round(c,x,y,x+tileW,y+tileH,7);p.setStyle(Paint.Style.FILL);}
                p.setColor(ingredientColors[i]);c.drawCircle(x+10,y+12.5f,6.5f,p);t.setTextAlign(Paint.Align.LEFT);t.setTextSize(7.1f);t.setColor(Color.rgb(91,87,68));c.drawText(ingredientNames[i],x+19,y+11,t);
                t.setTextSize(7.5f);t.setColor(ingredientStock[i]<=5?Color.rgb(200,72,57):Color.rgb(99,126,91));c.drawText("x"+ingredientStock[i],x+19,y+21,t);
                p.setColor(Color.rgb(255,224,164));round(c,x+68,y+3,x+85,y+22,5);t.setTextAlign(Paint.Align.CENTER);t.setTextSize(10);t.setColor(Color.rgb(124,83,40));c.drawText("+",x+76.5f,y+17,t);}
        }

        private void drawClosedOverlay(Canvas c){
            p.setColor(Color.argb(205,64,54,46));round(c,28,270,362,565,24);p.setColor(Color.rgb(255,246,225));round(c,38,280,352,555,20);
            t.setTextAlign(Paint.Align.CENTER);t.setTypeface(android.graphics.Typeface.create("sans-serif-rounded",android.graphics.Typeface.BOLD));t.setTextSize(23);t.setColor(Color.rgb(184,68,56));c.drawText("KAFE KAPANDI",195,337,t);
            t.setTextSize(13);t.setColor(Color.rgb(103,91,71));c.drawText("Müşteri memnuniyeti 2 yıldızın altına düştü.",195,371,t);t.setTextSize(17);t.setColor(Color.rgb(178,126,56));c.drawText("SON PUAN: ★ "+String.format(Locale.US,"%.2f",barRating),195,414,t);
            t.setTextSize(11);t.setColor(Color.rgb(111,104,84));c.drawText("Kasada kalan para: $"+coins,195,449,t);c.drawText("Bu vardiya sona erdi.",195,480,t);
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

        private void clearGlass(){glassIngredients=new int[0];mixed=false;recipeStep=0;quality=100;qualityMistakes=0;}
        private void changeRating(float delta){
            if(barClosed)return;barRating=clamp(barRating+delta,0f,5f);
            if(barRating<2f){barClosed=true;for(Customer customer:customers){customer.state=C_LEAVING;customer.exitX=customer.x<W*0.5f?-86f:W+86f;}clearGlass();showMessage("İŞLETME KAPANDI! Bar puanı 2.00 altına düştü.");}
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
            if(barClosed){showMessage("Kafe kapalı! Puan 2.00 altına düştü.");return;}
            if(y>=317&&y<=391){int card=(int)((x-23)/116f);if(card>=0&&card<3&&x>=23+card*116){int found=-1,count=0;
                for(int i=0;i<customers.size();i++)if(customerCanOrder(customers.get(i))){if(count==card){found=i;break;}count++;}
                if(found>=0){selectedCustomer=found;clearGlass();showMessage(customers.get(found).name+" • "+drinkNames[customers.get(found).drink]);}}return;}
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

            if(y>=660&&y<=795){int col=(int)((x-12)/92f),row=(int)((y-660)/27f);if(col>=0&&col<4&&row>=0&&row<5){int ingredient=row*4+col;if(ingredient<ingredientNames.length){
                float bx=12+col*92f,by=660+row*27f;if(x>bx+67){int cost=ingredientBuyPrices[ingredient];if(coins<cost)showMessage("Yetersiz bakiye! Paket fiyatı $"+cost);
                    else{coins-=cost;ingredientStock[ingredient]+=10;showMessage(ingredientNames[ingredient]+" stoğu +10 • -$"+cost);addParticles(bx+76,by+12,ingredientColors[ingredient],8);}return;}
                if(!selectedCustomerCanOrder()){showMessage("Önce sipariş kartından müşteri seç!");return;}if(mixed){showMessage("Yeni içecek için bardağı temizle!");return;}
                int[] recipe=recipes[customers.get(selectedCustomer).drink];if(recipeStep>=recipe.length){showMessage("Malzemeler tamam. Şimdi KARIŞTIR!");return;}
                if(ingredientStock[ingredient]<=0){showMessage("Stok bitti! + işaretinden 10 adet satın al.");return;}int expected=recipe[recipeStep];
                if(ingredient!=expected){recordWrongStep("Yanlış ürün! Sıradaki: "+ingredientNames[expected]+".");return;}ingredientStock[ingredient]--;
                int[] next=new int[glassIngredients.length+1];System.arraycopy(glassIngredients,0,next,0,glassIngredients.length);next[next.length-1]=ingredient;glassIngredients=next;recipeStep++;
                addParticles(195+random.nextInt(35)-17,535+random.nextInt(30),ingredientColors[ingredient],7);showMessage(ingredientNames[ingredient]+" eklendi • stok: "+ingredientStock[ingredient]);return;}}}
            if (y >= 808 && y <= 844) {
                if (!selectedCustomerCanOrder()) {
                    showMessage("Bu müşteri şu anda sipariş alamıyor.");
                    return;
                }
                if (x < 195) {
                    if (recipeStep < recipes[customers.get(selectedCustomer).drink].length) {
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
                        if(quality<MINIMUM_PAID_QUALITY){combo=0;c.state=C_ANGRY;c.rejecting=true;c.angerTimer=0f;changeRating(-0.15f);clearGlass();showMessage(c.name+" içeceği beğenmedi! Para ödemiyor. ★ "+String.format(Locale.US,"%.2f",barRating));}
                        else{int baseReward=drinkPrices[c.drink]+Math.min(combo,5)*3;int reward=Math.max(1,Math.round(baseReward*quality/100f));boolean wasAngry=c.state==C_ANGRY||c.state==C_BANGING;
                            if(wasAngry)reward=Math.max(1,reward-4);coins+=reward;served++;combo++;
                            if(quality>=95&&c.patience>0.45f)changeRating(0.025f);else if(quality>=80)changeRating(0.012f);else changeRating(-0.025f);
                            addParticles(195,490,drinkColors[c.drink],32);c.state=C_DRINKING;c.drinkTime=3.8f;c.sipPhase=0f;clearGlass();if(served%5==0)level++;
                            showMessage(c.name+" beğendi! +$"+reward+" • ★ "+String.format(Locale.US,"%.2f",barRating));}
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
