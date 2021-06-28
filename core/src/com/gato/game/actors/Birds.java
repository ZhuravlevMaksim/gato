package com.gato.game.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Birds {

    private static List<Bird> activeBirds = new ArrayList<>();
    private static Pool<Bird> birdPool;
    public static Texture birdTexture;
    public static int birdsCount;

    public Birds() {
        birdPool = Pools.get(Bird.class);
        birdTexture = new Texture(Gdx.files.internal("bird-blue.png"));
    }

    public List<Bird> getBirds() {
        activeBirds = activeBirds.stream().filter(bird -> bird.active).collect(Collectors.toList());
        if (activeBirds.size() == 0) {
            birdsCount = MathUtils.random(2, 17);
            for (int i = 0; i < birdsCount; i++) {
                activeBirds.add(birdPool.obtain());
            }
        }

        return activeBirds;
    }

    public static class Bird implements Pool.Poolable {

        public Vector2 position;
        private boolean active;
        private boolean flyUp = true;
        public Sprite birdSprite;

        public Bird() {
            active = true;
            this.position = randCors();
            float random = MathUtils.random(1.5f, 2.5f);
            birdSprite = new Sprite(birdTexture);
            birdSprite.setSize(random, random);
            birdSprite.flip(true, false);
        }

        private Vector2 randCors() {
            return new Vector2(MathUtils.random(27, 27 + birdsCount * 2), MathUtils.random(16, 30));
        }

        @Override
        public void reset() {
            position.set(randCors());
        }

        public void draw(Batch batch) {
            update(Gdx.graphics.getDeltaTime());
            birdSprite.draw(batch);
        }

        int flyPowerUp = 27;
        int flyPowerDown = 18;
        int flySpeedX = MathUtils.random(11, 12);
        int flySpeedY = MathUtils.random(1, 2);

        public void update(float delta) {
            position.x -= delta * flySpeedX;
            if (position.y > flyPowerUp) {
                flyUp = false;
            } else if (position.y < flyPowerDown) {
                flyUp = true;
            }
            position.y = flyUp ? position.y + delta * flySpeedY : position.y - delta * 2 * flySpeedY;
            birdSprite.setPosition(position.x, position.y);

            if (isOutOfScreen()) {
                birdPool.free(this);
                active = false;
            }
        }

        private boolean isOutOfScreen() {
            return position.x < -26;
        }

    }


}
