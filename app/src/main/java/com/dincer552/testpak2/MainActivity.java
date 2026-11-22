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
import java.util.Iterator;
import java.util.Random;

public final class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(9, 14, 23));
        getWindow().setNavigationBarColor(Color.rgb(9, 14, 23));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(new ArenaGame());
    }

    final class ArenaGame extends View {
        private static final int BOT_COUNT = 29;
        private static final int FOOD_TARGET = 190;
        private static final float MAX_ENERGY = 100f;
        private static final float ATTACK_COST = 18f;
        private static final float HIT_COST = 9f;
        private static final float ATTACK_RANGE = 620f;
        private static final float ATTACK_SPEED = 880f;
        private static final float PLAYER_SPEED = 205f;
        private static final float MOVE_DRAIN = 2.25f;

        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Random random = new Random();
        private final ArrayList<Orb> bots = new ArrayList<>();
        private final ArrayList<Food> foods = new ArrayList<>();
        private final ArrayList<Projectile> projectiles = new ArrayList<>();
        private final int[] botColors = new int[] {
                Color.rgb(255, 112, 92), Color.rgb(255, 179, 71),
                Color.rgb(255, 220, 92), Color.rgb(177, 235, 92),
                Color.rgb(75, 225, 155), Color.rgb(64, 211, 220),
                Color.rgb(83, 169, 255), Color.rgb(130, 139, 255),
                Color.rgb(190, 126, 255), Color.rgb(245, 119, 211),
                Color.rgb(255, 143, 174), Color.rgb(185, 220, 255)
        };

        private Orb player;
        private float worldWidth = 2200f;
        private float worldHeight = 2600f;
        private float cameraX, cameraY;
        private float moveTargetX, moveTargetY;
        private boolean hasMoveTarget;
        private boolean started;
        private boolean gameOver;
        private boolean initialized;
        private long lastFrame;
        private long lastTapTime;
        private float lastTapX, lastTapY;
        private float pointerDownX, pointerDownY;
        private boolean dragging;
        private int kills;
        private String statusMessage = "Tek dokunuş: hareket  •  Çift dokunuş: saldırı";
        private long statusUntil;

        ArenaGame() {
            super(MainActivity.this);
            textPaint.setTypeface(android.graphics.Typeface.create(
                    "sans-serif-medium", android.graphics.Typeface.NORMAL));
            setBackgroundColor(Color.rgb(9, 14, 23));
        }

        private float rand(float min, float max) {
            return min + random.nextFloat() * (max - min);
        }

        private void setupArena() {
            bots.clear();
            foods.clear();
            projectiles.clear();
            kills = 0;
            player = new Orb(true, worldWidth * 0.5f, worldHeight * 0.5f,
                    34f, MAX_ENERGY, Color.rgb(72, 244, 177));
            moveTargetX = player.x;
            moveTargetY = player.y;
            hasMoveTarget = false;

            for (int i = 0; i < BOT_COUNT; i++) {
                float x, y;
                int attempts = 0;
                do {
                    x = rand(100f, worldWidth - 100f);
                    y = rand(130f, worldHeight - 130f);
                    attempts++;
                } while (distance(x, y, player.x, player.y) < 420f && attempts < 40);
                bots.add(createBot(x, y));
            }

            for (int i = 0; i < FOOD_TARGET; i++) {
                foods.add(createFood());
            }
            cameraX = clamp(player.x - getWidth() / 2f, 0f,
                    Math.max(0f, worldWidth - getWidth()));
            cameraY = clamp(player.y - getHeight() / 2f, 0f,
                    Math.max(0f, worldHeight - getHeight()));
            initialized = true;
        }

        private Orb createBot(float x, float y) {
            float radius = rand(22f, 39f);
            float energy = rand(62f, 100f);
            int color = botColors[random.nextInt(botColors.length)];
            return new Orb(false, x, y, radius, energy, color);
        }

        private Food createFood() {
            return new Food(rand(35f, worldWidth - 35f),
                    rand(45f, worldHeight - 45f), rand(3.2f, 6.8f),
                    new int[] {
                            Color.rgb(104, 132, 142),
                            Color.rgb(119, 125, 154),
                            Color.rgb(126, 143, 116),
                            Color.rgb(145, 119, 135)
                    }[random.nextInt(4)]);
        }

        private static float clamp(float value, float min, float max) {
            return Math.max(min, Math.min(max, value));
        }

        private static float distance(float x1, float y1, float x2, float y2) {
            float dx = x2 - x1;
            float dy = y2 - y1;
            return (float) Math.sqrt(dx * dx + dy * dy);
        }

        @Override protected void onSizeChanged(int w, int h, int oldW, int oldH) {
            super.onSizeChanged(w, h, oldW, oldH);
            worldWidth = Math.max(2200f, w * 2.0f);
            worldHeight = Math.max(2500f, h * 1.75f);
            setupArena();
        }

        private void startGame() {
            setupArena();
            started = true;
            gameOver = false;
            lastFrame = System.currentTimeMillis();
            statusMessage = "Çift dokun: saldır  •  Sürükle: hareket";
            statusUntil = lastFrame + 3500L;
            invalidate();
        }

        private Orb findOrbAt(float worldX, float worldY) {
            Orb nearest = null;
            float best = Float.MAX_VALUE;
            if (player != null && player.alive) {
                float d = distance(worldX, worldY, player.x, player.y);
                if (d < player.radius + 28f && d < best) {
                    nearest = player;
                    best = d;
                }
            }
            for (Orb bot : bots) {
                if (!bot.alive) continue;
                float d = distance(worldX, worldY, bot.x, bot.y);
                if (d < bot.radius + 25f && d < best) {
                    nearest = bot;
                    best = d;
                }
            }
            return nearest;
        }

        private void setMoveTarget(float screenX, float screenY) {
            moveTargetX = clamp(screenX + cameraX, 30f, worldWidth - 30f);
            moveTargetY = clamp(screenY + cameraY, 45f, worldHeight - 45f);
            hasMoveTarget = distance(player.x, player.y, moveTargetX, moveTargetY) > 24f;
        }

        private void handleTap(float x, float y) {
            long now = System.currentTimeMillis();
            float wx = x + cameraX;
            float wy = y + cameraY;
            Orb target = findOrbAt(wx, wy);
            boolean doubleTap = now - lastTapTime < 340L
                    && distance(x, y, lastTapX, lastTapY) < 62f;

            if (doubleTap && target != null && target != player) {
                fireAttack(player, target, true);
                lastTapTime = 0L;
                return;
            }

            setMoveTarget(x, y);
            lastTapX = x;
            lastTapY = y;
            lastTapTime = now;
        }

        private void fireAttack(Orb attacker, Orb target, boolean playerAttack) {
            if (attacker == null || target == null || !attacker.alive || !target.alive
                    || attacker == target) return;

            float d = distance(attacker.x, attacker.y, target.x, target.y);
            if (d > ATTACK_RANGE) {
                if (playerAttack) showMessage("Hedef menzil dışında");
                return;
            }
            if (attacker.energy < ATTACK_COST) {
                if (playerAttack) showMessage("Saldırı için en az 18 enerji gerekli");
                return;
            }
            if (attacker.attackCooldown > 0f) {
                if (playerAttack) showMessage("Saldırı hazırlanıyor");
                return;
            }

            // Attacker spends twice the energy that the defender loses on a hit.
            attacker.energy = Math.max(0f, attacker.energy - ATTACK_COST);
            attacker.attackCooldown = playerAttack ? 0.52f : rand(0.8f, 1.4f);
            projectiles.add(new Projectile(attacker, target));
            if (playerAttack) showMessage("Saldırı gönderildi  •  -18 enerji");
        }

        private void showMessage(String message) {
            statusMessage = message;
            statusUntil = System.currentTimeMillis() + 1900L;
        }

        private void updateGame(float dt) {
            if (player == null || !player.alive) return;

            player.attackCooldown = Math.max(0f, player.attackCooldown - dt);
            updatePlayer(dt);

            for (int i = 0; i < bots.size(); i++) {
                Orb bot = bots.get(i);
                if (!bot.alive) continue;
                updateBot(bot, dt);
            }

            updateProjectiles(dt);
            consumeFood(player);
            for (Orb bot : bots) {
                if (bot.alive) consumeFood(bot);
            }

            // A defeated bot is replaced so the arena always contains 29 opponents.
            for (int i = 0; i < bots.size(); i++) {
                Orb bot = bots.get(i);
                if (!bot.alive || bot.energy <= 0f) {
                    dropFood(bot.x, bot.y);
                    float sx, sy;
                    int tries = 0;
                    do {
                        sx = rand(70f, worldWidth - 70f);
                        sy = rand(70f, worldHeight - 70f);
                        tries++;
                    } while (distance(sx, sy, player.x, player.y) < 500f && tries < 30);
                    bots.set(i, createBot(sx, sy));
                }
            }

            if (foods.size() < FOOD_TARGET) {
                int add = Math.min(FOOD_TARGET - foods.size(), 4);
                for (int i = 0; i < add; i++) foods.add(createFood());
            }
            updateCamera(dt);
        }

        private void updatePlayer(float dt) {
            boolean moving = false;
            if (hasMoveTarget) {
                float dx = moveTargetX - player.x;
                float dy = moveTargetY - player.y;
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                if (d < 10f) {
                    hasMoveTarget = false;
                } else if (player.energy > 1.5f) {
                    float step = Math.min(d, PLAYER_SPEED * dt);
                    player.x += dx / d * step;
                    player.y += dy / d * step;
                    player.energy = Math.max(0f, player.energy - MOVE_DRAIN * dt);
                    moving = true;
                } else {
                    hasMoveTarget = false;
                    showMessage("Hareket için enerji topla");
                }
            }

            player.x = clamp(player.x, player.radius + 12f, worldWidth - player.radius - 12f);
            player.y = clamp(player.y, player.radius + 12f, worldHeight - player.radius - 12f);
            player.energy = Math.min(MAX_ENERGY,
                    player.energy + (moving ? 0.20f : 1.65f) * dt);
        }

        private void updateBot(Orb bot, float dt) {
            bot.attackCooldown = Math.max(0f, bot.attackCooldown - dt);
            bot.decisionTimer -= dt;
            bot.energy = Math.min(bot.maxEnergy,
                    bot.energy + (bot.isMoving ? 0.35f : 1.0f) * dt);

            if (bot.decisionTimer <= 0f) {
                bot.decisionTimer = rand(0.28f, 0.58f);
                Orb target = chooseBotTarget(bot);
                bot.target = target;

                if (target != null) {
                    float d = distance(bot.x, bot.y, target.x, target.y);
                    if (bot.energy < 28f && target.radius > bot.radius * 1.22f) {
                        float angle = (float) Math.atan2(bot.y - target.y, bot.x - target.x);
                        bot.goalX = clamp(bot.x + (float) Math.cos(angle) * 330f,
                                50f, worldWidth - 50f);
                        bot.goalY = clamp(bot.y + (float) Math.sin(angle) * 330f,
                                50f, worldHeight - 50f);
                        bot.hasGoal = true;
                    } else {
                        if (d > 245f) {
                            bot.goalX = target.x;
                            bot.goalY = target.y;
                            bot.hasGoal = true;
                        } else {
                            bot.hasGoal = false;
                        }
                        if (d <= ATTACK_RANGE && bot.attackCooldown <= 0f
                                && bot.energy >= ATTACK_COST && random.nextFloat() < 0.88f) {
                            fireAttack(bot, target, false);
                        }
                    }
                } else {
                    Food food = nearestFood(bot.x, bot.y);
                    if (food != null) {
                        bot.goalX = food.x;
                        bot.goalY = food.y;
                        bot.hasGoal = true;
                    } else {
                        bot.goalX = rand(80f, worldWidth - 80f);
                        bot.goalY = rand(80f, worldHeight - 80f);
                        bot.hasGoal = true;
                    }
                }
            }

            bot.isMoving = false;
            if (bot.hasGoal) {
                float dx = bot.goalX - bot.x;
                float dy = bot.goalY - bot.y;
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                if (d < 18f) {
                    bot.hasGoal = false;
                } else {
                    float step = Math.min(d, (125f + (40f - bot.radius) * 1.2f) * dt);
                    bot.x += dx / d * step;
                    bot.y += dy / d * step;
                    bot.energy = Math.max(0f, bot.energy - 0.55f * dt);
                    bot.isMoving = true;
                }
            }

            bot.x = clamp(bot.x, bot.radius + 10f, worldWidth - bot.radius - 10f);
            bot.y = clamp(bot.y, bot.radius + 10f, worldHeight - bot.radius - 10f);
        }

        private Orb chooseBotTarget(Orb bot) {
            Orb selected = null;
            float best = 690f;
            if (player != bot && player.alive) {
                float d = distance(bot.x, bot.y, player.x, player.y);
                if (d < best) {
                    selected = player;
                    best = d;
                }
            }
            for (Orb other : bots) {
                if (other == bot || !other.alive) continue;
                float d = distance(bot.x, bot.y, other.x, other.y);
                if (d < best) {
                    selected = other;
                    best = d;
                }
            }
            return selected;
        }

        private Food nearestFood(float x, float y) {
            Food selected = null;
            float best = 650f;
            for (Food food : foods) {
                float d = distance(x, y, food.x, food.y);
                if (d < best) {
                    best = d;
                    selected = food;
                }
            }
            return selected;
        }

        private void updateProjectiles(float dt) {
            Iterator<Projectile> iterator = projectiles.iterator();
            while (iterator.hasNext()) {
                Projectile shot = iterator.next();
                if (shot.target == null || !shot.target.alive) {
                    iterator.remove();
                    continue;
                }
                float dx = shot.target.x - shot.x;
                float dy = shot.target.y - shot.y;
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                shot.previousX = shot.x;
                shot.previousY = shot.y;

                if (d <= shot.target.radius + ATTACK_SPEED * dt) {
                    shot.x = shot.target.x;
                    shot.y = shot.target.y;
                    shot.target.energy = Math.max(0f, shot.target.energy - HIT_COST);
                    if (shot.target == player && shot.target.energy <= 0f) {
                        player.alive = false;
                        started = false;
                        gameOver = true;
                        showMessage("Enerjin tükendi");
                    } else if (shot.target != player && shot.target.energy <= 0f) {
                        shot.target.alive = false;
                        if (shot.owner == player) kills++;
                    }
                    iterator.remove();
                } else if (d > 0f) {
                    shot.x += dx / d * ATTACK_SPEED * dt;
                    shot.y += dy / d * ATTACK_SPEED * dt;
                    shot.life += dt;
                    if (shot.life > 2.2f) iterator.remove();
                }
            }
        }

        private void consumeFood(Orb orb) {
            for (int i = foods.size() - 1; i >= 0; i--) {
                Food food = foods.get(i);
                if (distance(orb.x, orb.y, food.x, food.y) < orb.radius + food.radius + 3f) {
                    orb.energy = Math.min(orb.maxEnergy, orb.energy + 12f + food.radius);
                    foods.remove(i);
                    if (orb == player) showMessage("Yem toplandı  •  enerji arttı");
                }
            }
        }

        private void dropFood(float x, float y) {
            for (int i = 0; i < 5; i++) {
                Food food = createFood();
                food.x = clamp(x + rand(-38f, 38f), 20f, worldWidth - 20f);
                food.y = clamp(y + rand(-38f, 38f), 20f, worldHeight - 20f);
                foods.add(food);
            }
        }

        private void updateCamera(float dt) {
            float targetX = clamp(player.x - getWidth() / 2f, 0f,
                    Math.max(0f, worldWidth - getWidth()));
            float targetY = clamp(player.y - getHeight() / 2f, 0f,
                    Math.max(0f, worldHeight - getHeight()));
            float smoothing = Math.min(1f, dt * 7f);
            cameraX += (targetX - cameraX) * smoothing;
            cameraY += (targetY - cameraY) * smoothing;
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (!initialized) setupArena();

            long now = System.currentTimeMillis();
            float dt = lastFrame == 0L ? 0.016f
                    : clamp((now - lastFrame) / 1000f, 0.008f, 0.045f);
            lastFrame = now;

            if (started) updateGame(dt);

            canvas.drawColor(Color.rgb(9, 14, 23));
            canvas.save();
            canvas.translate(-cameraX, -cameraY);
            drawArena(canvas);
            drawFood(canvas);
            drawProjectiles(canvas, now);
            for (Orb bot : bots) if (bot.alive) drawOrb(canvas, bot, now);
            if (player != null && player.alive) drawOrb(canvas, player, now);
            canvas.restore();

            drawHud(canvas, now);
            if (!started) drawStartOverlay(canvas);

            if (started) postInvalidateDelayed(16L);
        }

        private void drawArena(Canvas canvas) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(11, 18, 29));
            canvas.drawRect(0f, 0f, worldWidth, worldHeight, p);

            p.setColor(Color.argb(38, 112, 154, 177));
            p.setStrokeWidth(1f);
            float grid = 120f;
            int startX = (int) (cameraX / grid);
            int endX = (int) ((cameraX + getWidth()) / grid) + 1;
            for (int i = startX; i <= endX; i++) {
                float x = i * grid;
                canvas.drawLine(x, cameraY, x, cameraY + getHeight(), p);
            }
            int startY = (int) (cameraY / grid);
            int endY = (int) ((cameraY + getHeight()) / grid) + 1;
            for (int i = startY; i <= endY; i++) {
                float y = i * grid;
                canvas.drawLine(cameraX, y, cameraX + getWidth(), y, p);
            }

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(8f);
            p.setColor(Color.rgb(53, 76, 96));
            canvas.drawRect(4f, 4f, worldWidth - 4f, worldHeight - 4f, p);
            p.setStyle(Paint.Style.FILL);
        }

        private void drawFood(Canvas canvas) {
            float left = cameraX - 20f;
            float right = cameraX + getWidth() + 20f;
            float top = cameraY - 20f;
            float bottom = cameraY + getHeight() + 20f;
            for (Food food : foods) {
                if (food.x < left || food.x > right || food.y < top || food.y > bottom) continue;
                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.argb(88, Color.red(food.color),
                        Color.green(food.color), Color.blue(food.color)));
                canvas.drawCircle(food.x, food.y, food.radius + 2f, p);
                p.setColor(Color.argb(125, Color.red(food.color),
                        Color.green(food.color), Color.blue(food.color)));
                canvas.drawCircle(food.x, food.y, food.radius, p);
                p.setColor(Color.argb(90, 225, 235, 240));
                canvas.drawCircle(food.x - food.radius * 0.25f,
                        food.y - food.radius * 0.25f, Math.max(1.1f, food.radius * 0.22f), p);
            }
        }

        private void drawOrb(Canvas canvas, Orb orb, long now) {
            float pulse = (float) Math.sin(now * 0.0034f + orb.phase) * 1.8f;
            float r = orb.radius + pulse;
            float rotation = now * (orb.isPlayer ? 0.0015f : 0.0011f) + orb.phase;
            int base = orb.color;

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(48, Color.red(base), Color.green(base), Color.blue(base)));
            canvas.drawCircle(orb.x, orb.y, r * 1.55f, p);

            Path spikes = new Path();
            int points = orb.isPlayer ? 28 : 22;
            for (int i = 0; i < points; i++) {
                double angle = rotation + (Math.PI * 2.0 * i / points);
                float radius = r * (i % 2 == 0 ? 1.30f : 1.02f);
                float x = orb.x + (float) Math.cos(angle) * radius;
                float y = orb.y + (float) Math.sin(angle) * radius;
                if (i == 0) spikes.moveTo(x, y); else spikes.lineTo(x, y);
            }
            spikes.close();
            p.setColor(base);
            canvas.drawPath(spikes, p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2.1f);
            p.setColor(Color.argb(220, 238, 250, 255));
            canvas.drawCircle(orb.x, orb.y, r * 0.92f, p);
            p.setStyle(Paint.Style.FILL);
            p.setColor(base);
            canvas.drawCircle(orb.x, orb.y, r * 0.83f, p);

            p.setColor(Color.argb(100, 255, 255, 255));
            canvas.drawCircle(orb.x - r * 0.23f, orb.y - r * 0.28f,
                    r * 0.19f, p);
            p.setColor(Color.WHITE);
            canvas.drawCircle(orb.x + r * 0.20f, orb.y - r * 0.12f,
                    Math.max(2.2f, r * 0.09f), p);
            p.setColor(Color.rgb(16, 25, 36));
            canvas.drawCircle(orb.x + r * 0.23f, orb.y - r * 0.12f,
                    Math.max(1.1f, r * 0.045f), p);

            float barWidth = r * 1.75f;
            float barY = orb.y + r * 1.42f;
            p.setColor(Color.argb(170, 5, 10, 16));
            canvas.drawRoundRect(orb.x - barWidth / 2f, barY,
                    orb.x + barWidth / 2f, barY + 4.5f, 3f, 3f, p);
            p.setColor(orb.energy / orb.maxEnergy < 0.3f
                    ? Color.rgb(255, 105, 100) : Color.rgb(116, 242, 180));
            canvas.drawRoundRect(orb.x - barWidth / 2f, barY,
                    orb.x - barWidth / 2f + barWidth
                            * clamp(orb.energy / orb.maxEnergy, 0f, 1f),
                    barY + 4.5f, 3f, 3f, p);

            if (orb.isPlayer) {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(2f);
                p.setColor(Color.argb(210, 255, 255, 255));
                canvas.drawCircle(orb.x, orb.y, r * 1.48f, p);
                p.setStyle(Paint.Style.FILL);
            }
        }

        private void drawProjectiles(Canvas canvas, long now) {
            for (Projectile shot : projectiles) {
                if (shot.owner == null) continue;
                float sx = shot.owner.x;
                float sy = shot.owner.y;
                float dx = shot.x - sx;
                float dy = shot.y - sy;
                float length = (float) Math.sqrt(dx * dx + dy * dy);
                if (length < 2f) continue;
                float nx = -dy / length;
                float ny = dx / length;
                Path beam = new Path();
                int segments = 9;
                for (int i = 0; i <= segments; i++) {
                    float t = i / (float) segments;
                    float wave = (float) Math.sin(t * 21f + now * 0.025f) * 6f;
                    float x = sx + dx * t + nx * wave;
                    float y = sy + dy * t + ny * wave;
                    if (i == 0) beam.moveTo(x, y); else beam.lineTo(x, y);
                }
                int color = shot.owner.color;
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeCap(Paint.Cap.ROUND);
                p.setStrokeJoin(Paint.Join.ROUND);
                p.setStrokeWidth(8f);
                p.setColor(Color.argb(58, Color.red(color), Color.green(color), Color.blue(color)));
                canvas.drawPath(beam, p);
                p.setStrokeWidth(3.2f);
                p.setColor(Color.argb(235, Color.red(color), Color.green(color), Color.blue(color)));
                canvas.drawPath(beam, p);
                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.WHITE);
                canvas.drawCircle(shot.x, shot.y, 4.5f, p);
            }
        }

        private void drawHud(Canvas canvas, long now) {
            float w = getWidth();
            float h = getHeight();
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(225, 7, 13, 22));
            canvas.drawRoundRect(12f, 12f, w - 12f, 100f, 18f, 18f, p);

            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setTextSize(15f);
            textPaint.setColor(Color.WHITE);
            canvas.drawText("FASTER FAST MINI", 27f, 35f, textPaint);

            float energy = player == null ? 0f : player.energy;
            textPaint.setTextSize(12.5f);
            textPaint.setColor(Color.rgb(190, 209, 224));
            canvas.drawText("ENERJİ  " + Math.round(energy) + " / 100",
                    27f, 57f, textPaint);

            float barRight = Math.min(w - 120f, 230f);
            p.setColor(Color.rgb(35, 49, 65));
            canvas.drawRoundRect(27f, 66f, barRight, 75f, 6f, 6f, p);
            p.setColor(energy < 25f ? Color.rgb(255, 105, 100)
                    : Color.rgb(69, 231, 163));
            canvas.drawRoundRect(27f, 66f,
                    27f + (barRight - 27f) * clamp(energy / 100f, 0f, 1f),
                    75f, 6f, 6f, p);

            textPaint.setTextAlign(Paint.Align.RIGHT);
            textPaint.setTextSize(13f);
            textPaint.setColor(Color.rgb(213, 226, 237));
            canvas.drawText("BOT  " + bots.size(), w - 27f, 37f, textPaint);
            canvas.drawText("SKOR  " + kills, w - 27f, 59f, textPaint);

            if (now < statusUntil && started) {
                textPaint.setTextAlign(Paint.Align.CENTER);
                textPaint.setTextSize(12f);
                textPaint.setColor(Color.argb(235, 226, 239, 248));
                canvas.drawText(statusMessage, w / 2f, h - 24f, textPaint);
            } else if (started) {
                textPaint.setTextAlign(Paint.Align.CENTER);
                textPaint.setTextSize(11.5f);
                textPaint.setColor(Color.argb(150, 207, 223, 236));
                canvas.drawText("Sürükle: hareket   •   Çift dokun: saldır",
                        w / 2f, h - 24f, textPaint);
            }
        }

        private void drawStartOverlay(Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(172, 4, 9, 16));
            canvas.drawRect(0f, 0f, w, h, p);

            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(Math.min(29f, Math.max(22f, w * 0.065f)));
            canvas.drawText(gameOver ? "OYUN BİTTİ" : "FASTER FAST MINI",
                    w / 2f, h * 0.40f, textPaint);

            textPaint.setColor(Color.rgb(112, 242, 186));
            textPaint.setTextSize(15f);
            canvas.drawText(gameOver ? ("Skorun: " + kills) : "ARENA SAVAŞI",
                    w / 2f, h * 0.46f, textPaint);

            textPaint.setColor(Color.rgb(219, 231, 241));
            textPaint.setTextSize(13f);
            canvas.drawText("1 oyuncu  +  29 bot", w / 2f, h * 0.52f, textPaint);
            canvas.drawText("Çift dokun: 18 enerji harcayan saldırı",
                    w / 2f, h * 0.56f, textPaint);
            canvas.drawText("Sürükle veya dokun: hareket  •  Yem: enerji",
                    w / 2f, h * 0.60f, textPaint);

            p.setColor(Color.rgb(59, 207, 151));
            canvas.drawRoundRect(w * 0.23f, h * 0.67f,
                    w * 0.77f, h * 0.74f, 22f, 22f, p);
            textPaint.setColor(Color.rgb(7, 24, 20));
            textPaint.setTextSize(16f);
            canvas.drawText(gameOver ? "TEKRAR OYNA" : "OYNA",
                    w / 2f, h * 0.715f, textPaint);
        }

        @Override public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                pointerDownX = event.getX();
                pointerDownY = event.getY();
                dragging = false;
                return true;
            }

            if (!started) {
                if (event.getAction() == MotionEvent.ACTION_UP) startGame();
                return true;
            }

            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                float dx = event.getX() - pointerDownX;
                float dy = event.getY() - pointerDownY;
                if (!dragging && Math.sqrt(dx * dx + dy * dy) > 13f) dragging = true;
                if (dragging) setMoveTarget(event.getX(), event.getY());
                invalidate();
                return true;
            }

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (dragging) {
                    setMoveTarget(event.getX(), event.getY());
                    lastTapTime = 0L;
                } else {
                    handleTap(event.getX(), event.getY());
                }
                invalidate();
                return true;
            }
            return true;
        }

        private final class Orb {
            final boolean isPlayer;
            final float radius;
            final float maxEnergy;
            final int color;
            final float phase;
            float x, y, energy;
            float attackCooldown;
            float decisionTimer;
            float goalX, goalY;
            boolean hasGoal;
            boolean isMoving;
            boolean alive = true;
            Orb target;

            Orb(boolean isPlayer, float x, float y, float radius,
                float energy, int color) {
                this.isPlayer = isPlayer;
                this.x = x;
                this.y = y;
                this.radius = radius;
                this.maxEnergy = isPlayer ? MAX_ENERGY : Math.max(70f, energy);
                this.energy = energy;
                this.color = color;
                this.phase = rand(0f, (float) Math.PI * 2f);
                this.decisionTimer = rand(0.1f, 0.7f);
            }
        }

        private final class Food {
            float x, y;
            final float radius;
            final int color;
            Food(float x, float y, float radius, int color) {
                this.x = x;
                this.y = y;
                this.radius = radius;
                this.color = color;
            }
        }

        private final class Projectile {
            final Orb owner;
            final Orb target;
            float x, y, previousX, previousY;
            float life;
            Projectile(Orb owner, Orb target) {
                this.owner = owner;
                this.target = target;
                this.x = owner.x;
                this.y = owner.y;
                this.previousX = x;
                this.previousY = y;
            }
        }
    }
}