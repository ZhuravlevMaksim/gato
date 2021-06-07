package com.gato.game;

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
    public static Sprite birdTexture;

    public Birds() {
        birdPool = Pools.get(Bird.class);
        birdTexture = new Sprite(new Texture(Gdx.files.internal("bird-blue.png")));
        birdTexture.flip(true, false);
    }

    int birdsCount = 6;

    public List<Bird> getBirds() {
        activeBirds = activeBirds.stream().filter(bird -> bird.active).collect(Collectors.toList());
        if (activeBirds.size() == 0) {
            for (int i = 0; i < birdsCount; i++) {
                activeBirds.add(birdPool.obtain());
            }
            if (birdsCount < 40) {
                birdsCount++;
            }
        }

        return activeBirds;
    }

    public static class Bird implements Pool.Poolable {

        public Vector2 position;
        private boolean active;
        private boolean flyUp = true;

        public Bird() {
            this.position = randCors();
            active = true;
            float random = MathUtils.random(1, 3.5f);
            birdTexture.setSize(random, random);
        }

        private Vector2 randCors() {
            return new Vector2(MathUtils.random(26, 36), MathUtils.random(16, 30));
        }

        @Override
        public void reset() {
            position.set(randCors());
        }

        public void draw(Batch batch) {
            update(Gdx.graphics.getDeltaTime());
            birdTexture.draw(batch);
        }

        int flyPowerUp = MathUtils.random(21, 30);
        int flyPowerDown = MathUtils.random(16, 20);
        int flySpeed = MathUtils.random(11, 12);

        public void update(float delta) {
            position.x -= delta * flySpeed;
            if (position.y > flyPowerUp) {
                flyUp = false;
            } else if (position.y < flyPowerDown) {
                flyUp = true;
            }
            position.y = flyUp ? position.y + delta * 2 : position.y - delta * 2;
            birdTexture.setPosition(position.x, position.y);

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
