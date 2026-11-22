package com.dincer552.testpak2;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;

public final class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(8, 13, 22));
        getWindow().setNavigationBarColor(Color.rgb(8, 13, 22));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(new ArenaGame());
    }

    final class ArenaGame extends View {
        private static final int BOT_COUNT = 29;
        private static final int FOOD_COUNT = 210;
        private static final float MAX_RADIUS = 74f;
        private static final float MIN_RADIUS = 19f;
        private static final float BULLET_SPEED = 930f;
        private static final float BASE_FIRE_INTERVAL = 0.83f;
        private static final long RESPAWN_DELAY_MS = 2600L;

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Random random = new Random();
        private final ArrayList<Orb> bots = new ArrayList<>();
        private final ArrayList<Food> foods = new ArrayList<>();
        private final ArrayList<Remnant> remnants = new ArrayList<>();
        private final ArrayList<Projectile> projectiles = new ArrayList<>();
        private final ArrayList<PowerUp> powerUps = new ArrayList<>();
        private final int[] colors = {
                Color.rgb(255, 105, 91), Color.rgb(255, 165, 71),
                Color.rgb(255, 215, 83), Color.rgb(161, 225, 83),
                Color.rgb(63, 221, 151), Color.rgb(57, 208, 219),
                Color.rgb(73, 158, 255), Color.rgb(126, 131, 255),
                Color.rgb(186, 116, 255), Color.rgb(245, 105, 203),
                Color.rgb(255, 128, 164), Color.rgb(164, 213, 255)
        };
        private final String[] botNames = {
                "PULSE", "NOVA", "BOLT", "PIXEL", "COMET", "VENOM",
                "SPARK", "ORBIT", "FROST", "BLAZE", "VIPER", "ECHO",
                "TURBO", "SHADOW", "NEXUS", "ZIPPY", "COSMO", "FLUX",
                "RAPTOR", "MINT", "PHANTOM", "ROCKET", "CYBER", "STORM",
                "LASER", "TITAN", "MAMBA", "GLITCH", "METEOR"
        };

        private Orb player;
        private float worldWidth = 2600f;
        private float worldHeight = 3000f;
        private float cameraX, cameraY;
        private long lastFrame;
        private long nextPowerSpawn;
        private long statusUntil;
        private String statusMessage = "Dokun: yön ver  •  Toplar durmadan hareket eder";
        private boolean started;
        private boolean initialized;
        private float downX, downY;
        private boolean dragging;
        private int playerKills;

        ArenaGame() {
            super(MainActivity.this);
            text.setTypeface(android.graphics.Typeface.create(
                    "sans-serif-medium", android.graphics.Typeface.NORMAL));
            setBackgroundColor(Color.rgb(8, 13, 22));
        }

        private float rand(float min, float max) {
            return min + random.nextFloat() * (max - min);
        }

        private static float clamp(float value, float min, float max) {
            return Math.max(min, Math.min(max, value));
        }

        private static float distance(float x1, float y1, float x2, float y2) {
            float dx = x2 - x1;
            float dy = y2 - y1;
            return (float) Math.sqrt(dx * dx + dy * dy);
        }

        private static float wrapAngle(float angle) {
            while (angle > Math.PI) angle -= (float) (Math.PI * 2.0);
            while (angle < -Math.PI) angle += (float) (Math.PI * 2.0);
            return angle;
        }

        private void initializeArena() {
            bots.clear();
            foods.clear();
            remnants.clear();
            projectiles.clear();
            powerUps.clear();
            playerKills = 0;

            player = new Orb("SEN", true, worldWidth * 0.5f,
                    worldHeight * 0.5f, 27f, 0);
            player.heading = -0.22f;

            for (int i = 0; i < BOT_COUNT; i++) {
                float x = rand(90f, worldWidth - 90f);
                float y = rand(100f, worldHeight - 100f);
                if (distance(x, y, player.x, player.y) < 440f) {
                    x = clamp(x + (x < player.x ? -520f : 520f), 80f, worldWidth - 80f);
                    y = clamp(y + rand(-360f, 360f), 80f, worldHeight - 80f);
                }
                bots.add(new Orb(botNames[i], false, x, y, rand(21f, 34f), colors[i % colors.length]));
            }

            for (int i = 0; i < FOOD_COUNT; i++) foods.add(createFood());
            for (int i = 0; i < 7; i++) powerUps.add(createPowerUp());
            cameraX = clamp(player.x - getWidth() * 0.5f, 0f,
                    Math.max(0f, worldWidth - getWidth()));
            cameraY = clamp(player.y - getHeight() * 0.5f, 0f,
                    Math.max(0f, worldHeight - getHeight()));
            long now = System.currentTimeMillis();
            nextPowerSpawn = now + 6500L;
            lastFrame = now;
            initialized = true;
        }

        private Food createFood() {
            return new Food(rand(28f, worldWidth - 28f),
                    rand(35f, worldHeight - 35f), rand(2.2f, 4.3f),
                    new int[] { Color.rgb(112, 133, 148), Color.rgb(130, 126, 153),
                            Color.rgb(130, 151, 125), Color.rgb(151, 127, 139) }[random.nextInt(4)]);
        }

        private PowerUp createPowerUp() {
            return new PowerUp(rand(90f, worldWidth - 90f),
                    rand(100f, worldHeight - 100f), random.nextInt(4));
        }

        private void startGame() {
            initializeArena();
            started = true;
            statusMessage = "Yön vermek için dokun veya sürükle";
            statusUntil = System.currentTimeMillis() + 3200L;
            invalidate();
        }

        private ArrayList<Orb> allOrbs() {
            ArrayList<Orb> result = new ArrayList<>(BOT_COUNT + 1);
            if (player != null && player.alive) result.add(player);
            for (Orb bot : bots) if (bot.alive) result.add(bot);
            return result;
        }

        private void aimAt(float screenX, float screenY) {
            if (player == null || !player.alive) return;
            float targetX = screenX + cameraX;
            float targetY = screenY + cameraY;
            float dx = targetX - player.x;
            float dy = targetY - player.y;
            if (dx * dx + dy * dy > 100f) {
                player.heading = (float) Math.atan2(dy, dx);
            }
        }

        private void showMessage(String message) {
            statusMessage = message;
            statusUntil = System.currentTimeMillis() + 1800L;
        }

        private void updateGame(float dt, long now) {
            if (player == null) return;

            if (!player.alive && now >= player.respawnAt) {
                float retainedScore = player.score;
                player = new Orb("SEN", true,
                        rand(180f, worldWidth - 180f), rand(180f, worldHeight - 180f),
                        25f, 0);
                player.score = retainedScore;
                player.heading = rand(-3.14f, 3.14f);
                showMessage("Yeniden oyundasın! Rakiplerden parça topla.");
            }

            for (Orb bot : bots) {
                if (!bot.alive) {
                    if (now >= bot.respawnAt) {
                        Orb reborn = new Orb(bot.name, false,
                                rand(100f, worldWidth - 100f),
                                rand(100f, worldHeight - 100f), rand(20f, 29f), bot.color);
                        reborn.score = bot.score;
                        int index = bots.indexOf(bot);
                        if (index >= 0) bots.set(index, reborn);
                    }
                }
            }

            if (player.alive) updatePlayer(player, dt);
            for (Orb bot : bots) if (bot.alive) updateBot(bot, dt, now);

            for (Orb orb : allOrbs()) {
                orb.fireTimer -= dt;
                if (orb.fireTimer <= 0f) fireForward(orb, now);
                applyIdlePenalty(orb, dt);
            }

            updateProjectiles(dt, now);
            updatePickups(now);
            if (now >= nextPowerSpawn && powerUps.size() < 12) {
                powerUps.add(createPowerUp());
                nextPowerSpawn = now + (long) rand(7500f, 11000f);
            }

            if (foods.size() < FOOD_COUNT) {
                int amount = Math.min(FOOD_COUNT - foods.size(), 4);
                for (int i = 0; i < amount; i++) foods.add(createFood());
            }
            updateCamera(dt);
        }

        private void updatePlayer(Orb orb, float dt) {
            moveAndBounce(orb, dt);
        }

        private void updateBot(Orb bot, float dt, long now) {
            bot.aiTimer -= dt;
            if (bot.aiTimer <= 0f) {
                bot.aiTimer = rand(0.22f, 0.48f);
                Orb target = nearestOpponent(bot, 930f);
                if (target != null) {
                    float d = distance(bot.x, bot.y, target.x, target.y);
                    float desired;
                    if (target.radius > bot.radius * 1.28f && d < 660f) {
                        desired = (float) Math.atan2(bot.y - target.y, bot.x - target.x);
                    } else {
                        desired = (float) Math.atan2(target.y - bot.y, target.x - bot.x);
                    }
                    bot.desiredHeading = desired;
                } else {
                    bot.desiredHeading += rand(-1.2f, 1.2f);
                }

                Remnant loot = nearestRemnant(bot.x, bot.y, 540f);
                if (loot != null) {
                    float lootAngle = (float) Math.atan2(loot.y - bot.y, loot.x - bot.x);
                    bot.desiredHeading = bot.desiredHeading * 0.62f + lootAngle * 0.38f;
                } else {
                    Food food = nearestFood(bot.x, bot.y, 440f);
                    if (food != null && bot.score < 25f) {
                        float foodAngle = (float) Math.atan2(food.y - bot.y, food.x - bot.x);
                        bot.desiredHeading = bot.desiredHeading * 0.70f + foodAngle * 0.30f;
                    }
                }
            }

            float difference = wrapAngle(bot.desiredHeading - bot.heading);
            float turn = clamp(difference, -1.85f * dt, 1.85f * dt);
            bot.heading = wrapAngle(bot.heading + turn);
            moveAndBounce(bot, dt);
        }

        private Orb nearestOpponent(Orb source, float maxDistance) {
            Orb chosen = null;
            float best = maxDistance;
            if (player != source && player != null && player.alive) {
                float d = distance(source.x, source.y, player.x, player.y);
                if (d < best) { best = d; chosen = player; }
            }
            for (Orb bot : bots) {
                if (!bot.alive || bot == source) continue;
                float d = distance(source.x, source.y, bot.x, bot.y);
                if (d < best) { best = d; chosen = bot; }
            }
            return chosen;
        }

        private Remnant nearestRemnant(float x, float y, float maxDistance) {
            Remnant chosen = null;
            float best = maxDistance;
            for (Remnant piece : remnants) {
                float d = distance(x, y, piece.x, piece.y);
                if (d < best) { best = d; chosen = piece; }
            }
            return chosen;
        }

        private Food nearestFood(float x, float y, float maxDistance) {
            Food chosen = null;
            float best = maxDistance;
            for (Food food : foods) {
                float d = distance(x, y, food.x, food.y);
                if (d < best) { best = d; chosen = food; }
            }
            return chosen;
        }

        private void moveAndBounce(Orb orb, float dt) {
            float speed = clamp(183f - orb.radius * 0.76f, 112f, 168f);
            orb.x += (float) Math.cos(orb.heading) * speed * dt;
            orb.y += (float) Math.sin(orb.heading) * speed * dt;

            float minX = orb.radius + 7f;
            float maxX = worldWidth - orb.radius - 7f;
            float minY = orb.radius + 7f;
            float maxY = worldHeight - orb.radius - 7f;

            if (orb.x < minX) {
                orb.x = minX;
                orb.heading = (float) Math.PI - orb.heading;
            } else if (orb.x > maxX) {
                orb.x = maxX;
                orb.heading = (float) Math.PI - orb.heading;
            }
            if (orb.y < minY) {
                orb.y = minY;
                orb.heading = -orb.heading;
            } else if (orb.y > maxY) {
                orb.y = maxY;
                orb.heading = -orb.heading;
            }
            orb.heading = wrapAngle(orb.heading);
        }

        private void fireForward(Orb shooter, long now) {
            if (!shooter.alive) return;
            float size = shooter.radius;
            float range = 265f + size * 9.8f + (now < shooter.rangeUntil ? 260f : 0f);
            float damage = 4.0f + size * 0.36f;
            if (now < shooter.damageUntil) damage *= 1.72f;
            float interval = BASE_FIRE_INTERVAL;
            if (now < shooter.rateUntil) interval *= 0.48f;
            interval *= clamp(31f / size, 0.64f, 1.35f);

            float startX = shooter.x + (float) Math.cos(shooter.heading) * (size + 5f);
            float startY = shooter.y + (float) Math.sin(shooter.heading) * (size + 5f);
            projectiles.add(new Projectile(shooter, startX, startY, shooter.heading,
                    range, damage, shooter.color));
            shooter.fireTimer = interval;
        }

        private void updateProjectiles(float dt, long now) {
            Iterator<Projectile> iterator = projectiles.iterator();
            while (iterator.hasNext()) {
                Projectile shot = iterator.next();
                float step = BULLET_SPEED * dt;
                float nextX = shot.x + (float) Math.cos(shot.heading) * step;
                float nextY = shot.y + (float) Math.sin(shot.heading) * step;
                shot.travelled += step;
                shot.x = nextX;
                shot.y = nextY;

                Orb hit = null;
                for (Orb target : allOrbs()) {
                    if (target == shot.owner) continue;
                    if (distance(shot.x, shot.y, target.x, target.y) < target.radius + 7f) {
                        hit = target;
                        break;
                    }
                }
                if (hit != null) {
                    hit.hp -= shot.damage;
                    if (hit.hp <= 0f) destroyOrb(hit, now);
                    iterator.remove();
                } else if (shot.travelled >= shot.range
                        || shot.x < 0f || shot.x > worldWidth
                        || shot.y < 0f || shot.y > worldHeight) {
                    iterator.remove();
                }
            }
        }

        private void destroyOrb(Orb victim, long now) {
            if (!victim.alive) return;
            victim.alive = false;
            victim.respawnAt = now + RESPAWN_DELAY_MS;
            int fragmentCount = 8 + (int) (victim.radius / 8f);
            float totalValue = Math.max(12f, victim.radius * 1.55f + victim.score * 0.24f);
            float fragmentValue = totalValue / fragmentCount;
            for (int i = 0; i < fragmentCount; i++) {
                float angle = (float) (Math.PI * 2.0 * i / fragmentCount) + rand(-0.22f, 0.22f);
                float radius = rand(5f, 9f) + victim.radius * 0.055f;
                float distance = rand(8f, victim.radius * 1.55f);
                remnants.add(new Remnant(
                        victim.x + (float) Math.cos(angle) * distance,
                        victim.y + (float) Math.sin(angle) * distance,
                        radius, fragmentValue, victim.color));
            }
            if (victim == player) {
                playerKills = Math.max(0, playerKills);
                victim.score *= 0.5f;
                showMessage("Parçalandın! Yeniden doğuyorsun...");
            } else {
                victim.score *= 0.5f;
            }
        }

        private void applyIdlePenalty(Orb orb, float dt) {
            orb.idleTimer += dt;
            if (orb.idleTimer >= 7.5f) {
                orb.decayTimer += dt;
                if (orb.decayTimer >= 4.0f) {
                    orb.decayTimer = 0f;
                    orb.score = Math.max(0f, orb.score - 1f);
                }
            }
        }

        private void updatePickups(long now) {
            ArrayList<Orb> orbs = allOrbs();
            for (Orb orb : orbs) {
                for (int i = foods.size() - 1; i >= 0; i--) {
                    Food food = foods.get(i);
                    if (distance(orb.x, orb.y, food.x, food.y) <= orb.radius + food.radius) {
                        foods.remove(i);
                        orb.score += 1f;
                        orb.radius = Math.min(MAX_RADIUS, orb.radius + 0.025f);
                        orb.hp = Math.min(orb.maxHp, orb.hp + 0.35f);
                        if (orb == player) showMessage("+1 küçük yem puanı");
                    }
                }

                for (int i = remnants.size() - 1; i >= 0; i--) {
                    Remnant piece = remnants.get(i);
                    if (distance(orb.x, orb.y, piece.x, piece.y) <= orb.radius + piece.radius) {
                        remnants.remove(i);
                        orb.score += piece.value;
                        orb.radius = Math.min(MAX_RADIUS,
                                orb.radius + Math.min(1.3f, piece.value * 0.075f));
                        orb.maxHp += piece.value * 0.55f;
                        orb.hp = Math.min(orb.maxHp, orb.hp + piece.value * 0.65f);
                        orb.idleTimer = 0f;
                        orb.decayTimer = 0f;
                        if (orb == player) {
                            playerKills++;
                            showMessage("Rakip kalıntısı toplandı! +" + Math.round(piece.value) + " puan");
                        }
                    }
                }

                for (int i = powerUps.size() - 1; i >= 0; i--) {
                    PowerUp item = powerUps.get(i);
                    if (distance(orb.x, orb.y, item.x, item.y) <= orb.radius + 16f) {
                        applyPowerUp(orb, item, now);
                        powerUps.remove(i);
                    }
                }
            }
        }

        private void applyPowerUp(Orb orb, PowerUp power, long now) {
            if (power.type == 0) {
                orb.rangeUntil = now + 15000L;
            } else if (power.type == 1) {
                orb.damageUntil = now + 15000L;
            } else if (power.type == 2) {
                orb.rateUntil = now + 15000L;
            } else {
                orb.hp = Math.min(orb.maxHp, orb.hp + Math.max(32f, orb.maxHp * 0.48f));
            }
            if (orb == player) {
                String[] labels = {"UZUN MENZİL", "GÜÇLÜ ATIŞ", "HIZLI ATEŞ", "CAN YENİLENDİ"};
                showMessage("ÖZEL GÜÇ: " + labels[power.type]);
            }
        }

        private void updateCamera(float dt) {
            if (player == null || !player.alive) return;
            float targetX = clamp(player.x - getWidth() * 0.5f, 0f,
                    Math.max(0f, worldWidth - getWidth()));
            float targetY = clamp(player.y - getHeight() * 0.5f, 0f,
                    Math.max(0f, worldHeight - getHeight()));
            float alpha = Math.min(1f, dt * 6.8f);
            cameraX += (targetX - cameraX) * alpha;
            cameraY += (targetY - cameraY) * alpha;
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (!initialized) initializeArena();
            long now = System.currentTimeMillis();
            float dt = clamp((now - lastFrame) / 1000f, 0.008f, 0.045f);
            lastFrame = now;
            if (started) updateGame(dt, now);

            canvas.drawColor(Color.rgb(8, 13, 22));
            canvas.save();
            canvas.translate(-cameraX, -cameraY);
            drawArena(canvas);
            drawFoods(canvas);
            drawRemnants(canvas);
            drawPowerUps(canvas, now);
            drawProjectiles(canvas, now);
            if (player != null && player.alive) drawOrb(canvas, player, now);
            for (Orb bot : bots) if (bot.alive) drawOrb(canvas, bot, now);
            canvas.restore();

            drawHud(canvas, now);
            if (!started) drawStartScreen(canvas);
            if (started) postInvalidateDelayed(16L);
        }

        private void drawArena(Canvas canvas) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(9, 16, 27));
            canvas.drawRect(0f, 0f, worldWidth, worldHeight, paint);
            paint.setStrokeWidth(1f);
            paint.setColor(Color.argb(34, 101, 146, 170));
            float grid = 115f;
            int x0 = (int) (cameraX / grid);
            int x1 = (int) ((cameraX + getWidth()) / grid) + 1;
            for (int i = x0; i <= x1; i++) {
                float x = i * grid;
                canvas.drawLine(x, cameraY, x, cameraY + getHeight(), paint);
            }
            int y0 = (int) (cameraY / grid);
            int y1 = (int) ((cameraY + getHeight()) / grid) + 1;
            for (int i = y0; i <= y1; i++) {
                float y = i * grid;
                canvas.drawLine(cameraX, y, cameraX + getWidth(), y, paint);
            }
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(8f);
            paint.setColor(Color.rgb(61, 87, 111));
            canvas.drawRect(4f, 4f, worldWidth - 4f, worldHeight - 4f, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        private void drawFoods(Canvas canvas) {
            for (Food food : foods) {
                if (!visible(food.x, food.y, 12f)) continue;
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.argb(70, Color.red(food.color),
                        Color.green(food.color), Color.blue(food.color)));
                canvas.drawCircle(food.x, food.y, food.radius + 2f, paint);
                paint.setColor(Color.argb(135, Color.red(food.color),
                        Color.green(food.color), Color.blue(food.color)));
                canvas.drawCircle(food.x, food.y, food.radius, paint);
            }
        }

        private void drawRemnants(Canvas canvas) {
            for (Remnant piece : remnants) {
                if (!visible(piece.x, piece.y, 20f)) continue;
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.argb(65, Color.red(piece.color),
                        Color.green(piece.color), Color.blue(piece.color)));
                canvas.drawCircle(piece.x, piece.y, piece.radius + 4f, paint);
                paint.setColor(piece.color);
                canvas.drawCircle(piece.x, piece.y, piece.radius, paint);
                paint.setColor(Color.argb(175, 255, 255, 255));
                canvas.drawCircle(piece.x - piece.radius * 0.25f,
                        piece.y - piece.radius * 0.25f, Math.max(1.5f, piece.radius * 0.22f), paint);
            }
        }

        private void drawPowerUps(Canvas canvas, long now) {
            String[] icons = {"R", "D", "F", "+"};
            int[] powerColors = {
                    Color.rgb(75, 198, 255), Color.rgb(255, 104, 95),
                    Color.rgb(255, 202, 72), Color.rgb(88, 238, 157)
            };
            for (PowerUp item : powerUps) {
                if (!visible(item.x, item.y, 40f)) continue;
                float pulse = (float) Math.sin(now * 0.006f + item.phase) * 2.5f;
                int color = powerColors[item.type];
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.argb(55, Color.red(color), Color.green(color), Color.blue(color)));
                canvas.drawCircle(item.x, item.y, 22f + pulse, paint);
                paint.setColor(color);
                canvas.drawCircle(item.x, item.y, 14f + pulse * 0.35f, paint);
                text.setTextAlign(Paint.Align.CENTER);
                text.setTextSize(14f);
                text.setColor(Color.rgb(10, 18, 27));
                canvas.drawText(icons[item.type], item.x, item.y + 5f, text);
            }
        }

        private boolean visible(float x, float y, float margin) {
            return x >= cameraX - margin && x <= cameraX + getWidth() + margin
                    && y >= cameraY - margin && y <= cameraY + getHeight() + margin;
        }

        private void drawOrb(Canvas canvas, Orb orb, long now) {
            if (!visible(orb.x, orb.y, orb.radius * 2f)) return;
            float pulse = (float) Math.sin(now * 0.004f + orb.phase) * 1.6f;
            float r = orb.radius + pulse;
            float rotation = now * 0.0017f + orb.phase;

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(45, Color.red(orb.color),
                    Color.green(orb.color), Color.blue(orb.color)));
            canvas.drawCircle(orb.x, orb.y, r * 1.55f, paint);

            Path spikes = new Path();
            int count = 24;
            for (int i = 0; i < count; i++) {
                double angle = rotation + Math.PI * 2.0 * i / count;
                float radius = r * (i % 2 == 0 ? 1.29f : 1.02f);
                float x = orb.x + (float) Math.cos(angle) * radius;
                float y = orb.y + (float) Math.sin(angle) * radius;
                if (i == 0) spikes.moveTo(x, y); else spikes.lineTo(x, y);
            }
            spikes.close();
            paint.setColor(orb.color);
            canvas.drawPath(spikes, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2f);
            paint.setColor(Color.argb(220, 245, 250, 255));
            canvas.drawCircle(orb.x, orb.y, r * 0.92f, paint);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(orb.x, orb.y, r * 0.82f, paint);

            paint.setColor(Color.argb(110, 255, 255, 255));
            canvas.drawCircle(orb.x - r * 0.25f, orb.y - r * 0.28f, r * 0.18f, paint);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(orb.x + r * 0.23f, orb.y - r * 0.12f,
                    Math.max(2.4f, r * 0.09f), paint);
            paint.setColor(Color.rgb(12, 21, 31));
            canvas.drawCircle(orb.x + r * 0.25f, orb.y - r * 0.12f,
                    Math.max(1.2f, r * 0.045f), paint);

            float barW = r * 1.8f;
            float barY = orb.y + r * 1.35f;
            paint.setColor(Color.argb(180, 4, 8, 14));
            canvas.drawRoundRect(orb.x - barW / 2f, barY,
                    orb.x + barW / 2f, barY + 5f, 3f, 3f, paint);
            paint.setColor(orb.hp / orb.maxHp < 0.3f
                    ? Color.rgb(255, 92, 87) : Color.rgb(92, 238, 166));
            canvas.drawRoundRect(orb.x - barW / 2f, barY,
                    orb.x - barW / 2f + barW * clamp(orb.hp / orb.maxHp, 0f, 1f),
                    barY + 5f, 3f, 3f, paint);

            text.setTextAlign(Paint.Align.CENTER);
            text.setTextSize(clamp(r * 0.43f, 9f, 14f));
            text.setColor(Color.WHITE);
            text.setShadowLayer(3f, 0f, 1f, Color.BLACK);
            canvas.drawText(orb.name, orb.x, orb.y - r * 1.48f, text);
            text.clearShadowLayer();

            if (now < orb.rangeUntil || now < orb.damageUntil || now < orb.rateUntil) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2.5f);
                paint.setColor(Color.argb(220, 255, 255, 255));
                canvas.drawCircle(orb.x, orb.y, r * 1.48f, paint);
                paint.setStyle(Paint.Style.FILL);
            }
            if (orb.isPlayer) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2f);
                paint.setColor(Color.WHITE);
                canvas.drawCircle(orb.x, orb.y, r * 1.62f, paint);
                paint.setStyle(Paint.Style.FILL);
            }
        }

        private void drawProjectiles(Canvas canvas, long now) {
            for (Projectile shot : projectiles) {
                float dx = shot.x - shot.startX;
                float dy = shot.y - shot.startY;
                float length = (float) Math.sqrt(dx * dx + dy * dy);
                if (length < 2f || !visible(shot.x, shot.y, 30f)) continue;
                float nx = -dy / length;
                float ny = dx / length;
                Path trail = new Path();
                int segments = 8;
                for (int i = 0; i <= segments; i++) {
                    float t = i / (float) segments;
                    float wave = (float) Math.sin(t * 18f + now * 0.025f) * 4.5f;
                    float x = shot.startX + dx * t + nx * wave;
                    float y = shot.startY + dy * t + ny * wave;
                    if (i == 0) trail.moveTo(x, y); else trail.lineTo(x, y);
                }
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setStrokeWidth(7f);
                paint.setColor(Color.argb(65, Color.red(shot.color),
                        Color.green(shot.color), Color.blue(shot.color)));
                canvas.drawPath(trail, paint);
                paint.setStrokeWidth(2.8f);
                paint.setColor(Color.argb(245, Color.red(shot.color),
                        Color.green(shot.color), Color.blue(shot.color)));
                canvas.drawPath(trail, paint);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.WHITE);
                canvas.drawCircle(shot.x, shot.y, 3.5f, paint);
            }
        }

        private void drawHud(Canvas canvas, long now) {
            float w = getWidth();
            float h = getHeight();

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(226, 5, 10, 18));
            canvas.drawRoundRect(10f, 10f, w - 10f, 82f, 15f, 15f, paint);

            text.setTextAlign(Paint.Align.LEFT);
            text.setTextSize(14f);
            text.setColor(Color.WHITE);
            canvas.drawText("FASTER FAST MINI", 22f, 31f, text);
            text.setTextSize(11.5f);
            text.setColor(Color.rgb(190, 210, 225));
            int score = player == null ? 0 : Math.round(player.score);
            int size = player == null ? 0 : Math.round(player.radius);
            float hp = player == null || !player.alive ? 0f : player.hp / player.maxHp;
            canvas.drawText("PUAN " + score + "   BOYUT " + size,
                    22f, 51f, text);
            canvas.drawText("CAN " + (int) Math.max(0f, hp * 100f) + "%   PARÇALAMA " + playerKills,
                    22f, 68f, text);

            // Compact live top-five leaderboard in the upper-right corner.
            ArrayList<Orb> ranked = new ArrayList<>();
            if (player != null) ranked.add(player);
            ranked.addAll(bots);
            Collections.sort(ranked, new Comparator<Orb>() {
                @Override public int compare(Orb a, Orb b) {
                    return Float.compare(b.score, a.score);
                }
            });
            float panelW = Math.min(142f, w * 0.37f);
            float panelX = w - panelW - 17f;
            float panelY = 93f;
            paint.setColor(Color.argb(210, 5, 10, 18));
            canvas.drawRoundRect(panelX, panelY, w - 10f, panelY + 126f, 12f, 12f, paint);
            text.setTextAlign(Paint.Align.LEFT);
            text.setTextSize(10.5f);
            text.setColor(Color.rgb(136, 239, 193));
            canvas.drawText("SIRALAMA", panelX + 10f, panelY + 17f, text);
            for (int i = 0; i < Math.min(5, ranked.size()); i++) {
                Orb orb = ranked.get(i);
                float yy = panelY + 37f + i * 17f;
                text.setTextSize(10f);
                text.setColor(orb.isPlayer ? Color.rgb(117, 255, 191) : Color.WHITE);
                String label = (i + 1) + ". " + orb.name;
                if (!orb.alive) label += " *";
                canvas.drawText(label, panelX + 10f, yy, text);
                text.setTextAlign(Paint.Align.RIGHT);
                text.setColor(Color.rgb(218, 230, 240));
                canvas.drawText(String.valueOf(Math.round(orb.score)), w - 20f, yy, text);
                text.setTextAlign(Paint.Align.LEFT);
            }

            if (now < statusUntil && started) {
                text.setTextAlign(Paint.Align.CENTER);
                text.setTextSize(11.5f);
                text.setColor(Color.WHITE);
                paint.setColor(Color.argb(155, 5, 10, 18));
                canvas.drawRoundRect(22f, h - 48f, w - 22f, h - 16f, 10f, 10f, paint);
                canvas.drawText(statusMessage, w / 2f, h - 27f, text);
            } else if (started) {
                text.setTextAlign(Paint.Align.CENTER);
                text.setTextSize(10.5f);
                text.setColor(Color.argb(170, 216, 230, 240));
                canvas.drawText("Dokun / sürükle: yön değiştir  •  Otomatik ateş",
                        w / 2f, h - 20f, text);
            }
        }

        private void drawStartScreen(Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            paint.setColor(Color.argb(185, 3, 8, 15));
            canvas.drawRect(0f, 0f, w, h, paint);

            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(Color.WHITE);
            text.setTextSize(Math.min(28f, Math.max(22f, w * 0.064f)));
            canvas.drawText("FASTER FAST MINI", w / 2f, h * 0.37f, text);
            text.setTextSize(15f);
            text.setColor(Color.rgb(117, 244, 188));
            canvas.drawText("KESİNTİSİZ ARENA SAVAŞI", w / 2f, h * 0.43f, text);

            text.setTextSize(12.5f);
            text.setColor(Color.rgb(219, 231, 241));
            canvas.drawText("29 rakip durmadan hareket eder ve ateş eder",
                    w / 2f, h * 0.50f, text);
            canvas.drawText("Rakipleri parçala, kalıntılarını ilk sen kap",
                    w / 2f, h * 0.545f, text);
            canvas.drawText("Güç nesnelerini topla, sıralamada yüksel",
                    w / 2f, h * 0.59f, text);

            paint.setColor(Color.rgb(65, 218, 159));
            canvas.drawRoundRect(w * 0.24f, h * 0.67f,
                    w * 0.76f, h * 0.74f, 20f, 20f, paint);
            text.setTextSize(16f);
            text.setColor(Color.rgb(7, 23, 19));
            canvas.drawText("ARENA'YA GİR", w / 2f, h * 0.715f, text);
        }

        @Override public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                downX = event.getX();
                downY = event.getY();
                dragging = false;
                if (!started) startGame();
                else aimAt(event.getX(), event.getY());
                return true;
            }
            if (!started) return true;
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                float dx = event.getX() - downX;
                float dy = event.getY() - downY;
                if (Math.sqrt(dx * dx + dy * dy) > 8f) dragging = true;
                if (dragging) aimAt(event.getX(), event.getY());
                invalidate();
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                aimAt(event.getX(), event.getY());
                invalidate();
                return true;
            }
            return true;
        }

        private final class Orb {
            final String name;
            final boolean isPlayer;
            final int color;
            final float phase;
            float x, y, radius, hp, maxHp, score;
            float heading, desiredHeading, fireTimer, aiTimer;
            float idleTimer, decayTimer;
            long respawnAt;
            long rangeUntil, damageUntil, rateUntil;
            boolean alive = true;

            Orb(String name, boolean isPlayer, float x, float y, float radius, int colorIndex) {
                this.name = name;
                this.isPlayer = isPlayer;
                this.x = x;
                this.y = y;
                this.radius = radius;
                this.color = isPlayer ? Color.rgb(67, 244, 174) : colorIndex;
                this.maxHp = 72f + radius * 1.55f;
                this.hp = maxHp;
                this.phase = rand(0f, (float) Math.PI * 2f);
                this.heading = rand(-3.14f, 3.14f);
                this.desiredHeading = heading;
                this.fireTimer = rand(0.12f, 0.8f);
                this.aiTimer = rand(0.15f, 0.55f);
            }
        }

        private final class Food {
            final float x, y, radius;
            final int color;
            Food(float x, float y, float radius, int color) {
                this.x = x; this.y = y; this.radius = radius; this.color = color;
            }
        }

        private final class Remnant {
            final float x, y, radius, value;
            final int color;
            Remnant(float x, float y, float radius, float value, int color) {
                this.x = x; this.y = y; this.radius = radius;
                this.value = value; this.color = color;
            }
        }

        private final class PowerUp {
            final float x, y;
            final int type;
            final float phase;
            PowerUp(float x, float y, int type) {
                this.x = x; this.y = y; this.type = type;
                this.phase = rand(0f, 6.28f);
            }
        }

        private final class Projectile {
            final Orb owner;
            final float startX, startY, heading, range, damage;
            final int color;
            float x, y, travelled;
            Projectile(Orb owner, float startX, float startY, float heading,
                       float range, float damage, int color) {
                this.owner = owner;
                this.startX = startX;
                this.startY = startY;
                this.x = startX;
                this.y = startY;
                this.heading = heading;
                this.range = range;
                this.damage = damage;
                this.color = color;
            }
        }
    }
}