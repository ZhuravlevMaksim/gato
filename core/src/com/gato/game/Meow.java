package com.gato.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class Meow extends Image {
    ParticleEffect effect;

    public Meow() {
        super(new Texture("meow.png"));

        TextureAtlas textureAtlas = new TextureAtlas();
        textureAtlas.addRegion("meow",new TextureRegion(new Texture("meow.png")));
        effect = new ParticleEffect();
        effect.load(Gdx.files.internal("meow.p"), textureAtlas);
        effect.start();
        effect.setPosition(this.getWidth() / 2 + this.getX(), this.getHeight() / 2 + this.getY());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        effect.draw(batch);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        effect.setPosition(this.getWidth() / 2 + this.getX(), this.getHeight() / 2 + this.getY());
        effect.update(delta);

    }
}
