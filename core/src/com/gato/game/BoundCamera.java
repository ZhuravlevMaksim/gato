package com.gato.game;

import com.badlogic.gdx.graphics.OrthographicCamera;

public class BoundCamera extends OrthographicCamera {

    @Override
    public void update() {
        super.update();
    }

    public void setPosition(float x, float y) {
        this.position.set(x, y, 0);
    }
}
