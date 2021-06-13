package com.gato.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class Gato extends Image {

    private static final int FRAME_COLS = 5, FRAME_ROWS = 5;
    Animation<TextureRegion> walkAnimation;
    float stateTime;

    int width = 280;
    int height = 160;
    float x = 0;
    float y = 18;
    TextureRegion keyFrame;

    public Gato() {
        super(new Texture("gato-sprite.png"));

        TextureRegionDrawable drawable = (TextureRegionDrawable) this.getDrawable();
        Texture texture = drawable.getRegion().getTexture();
        TextureRegion[][] tmp = TextureRegion.split(texture,
                texture.getWidth() / FRAME_COLS,
                texture.getHeight() / FRAME_ROWS);

        TextureRegion[] walkFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS - 3];
        int index = 0;
        for (int i = 0; i < FRAME_ROWS; i++) {
            for (int j = 0; j < FRAME_COLS; j++) {
                if (index < 22) {
                    walkFrames[index++] = tmp[i][j];
                }
            }
        }

        walkAnimation = new Animation<>(0.03f, walkFrames);
        stateTime = 0f;
    }


    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(keyFrame, x, y, width, height);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        x += delta * 90;
        if (x > Gdx.graphics.getWidth()) {
            x = -width;
        }
        keyFrame = walkAnimation.getKeyFrame(stateTime += Gdx.graphics.getDeltaTime(), true);
    }

}
