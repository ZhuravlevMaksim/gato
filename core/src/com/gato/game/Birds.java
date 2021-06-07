package com.gato.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
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

    public Birds() {
        birdPool = Pools.get(Bird.class);
        birdTexture = new Texture(Gdx.files.internal("bird-blue.png"));
    }

    int birdsCount = 8;

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

        public Bird() {
            this.position = randCors();
            active = true;
        }

        private Vector2 randCors() {
            return new Vector2(MathUtils.random(-54, -22), MathUtils.random(-18, -6));
        }

        @Override
        public void reset() {
            position.set(randCors());
        }

        public void draw(Batch batch) {
            update(Gdx.graphics.getDeltaTime());
            batch.draw(birdTexture, position.x, position.y, 2, 2);
        }

        public void update(float delta) {

            position.add(1 * delta * MathUtils.random(10, 12), 1 * delta * MathUtils.random(4, 8));

            if (isOutOfScreen()) {
                birdPool.free(this);
                active = false;
            }
        }

        private boolean isOutOfScreen() {
            return position.y > 33 || position.x > 26;
        }

    }


}
