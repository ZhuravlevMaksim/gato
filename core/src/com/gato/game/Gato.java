package com.gato.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Gato {

    private static final int FRAME_COLS = 5, FRAME_ROWS = 5;
    Animation<TextureRegion> walkAnimation;
    Texture walkSheet;
    float stateTime;

    int width = 23;
    int height = 15;
    float x;
    float y;

    public Gato(float x) {
        this.x = x - width / 2f;
        this.y = 0;

        walkSheet = new Texture(Gdx.files.internal("gato-sprite.png"));

        TextureRegion[][] tmp = TextureRegion.split(walkSheet,
                walkSheet.getWidth() / FRAME_COLS,
                walkSheet.getHeight() / FRAME_ROWS);

        TextureRegion[] walkFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS - 3];
        int index = 0;
        for (int i = 0; i < FRAME_ROWS; i++) {
            for (int j = 0; j < FRAME_COLS; j++) {
                if (index < 22){
                    walkFrames[index++] = tmp[i][j];
                }
            }
        }

        walkAnimation = new Animation<>(0.05f, walkFrames);
        stateTime = 0f;
    }

    public TextureRegion texture() {
        return walkAnimation.getKeyFrame(stateTime += Gdx.graphics.getDeltaTime(), true);
    }

    public void updatePosition(float x) {
        this.x = x - width / 2f;
    }

    public void dispose() {
        walkSheet.dispose();
    }
}
