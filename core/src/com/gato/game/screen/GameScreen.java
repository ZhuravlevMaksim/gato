package com.gato.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gato.game.BoundCamera;
import de.eskalon.commons.screen.ManagedScreen;

public class GameScreen extends ManagedScreen implements InputProcessor {

    private final Stage stage = new Stage(new ExtendViewport(1280, 720));
    private final BoundCamera camera = new BoundCamera();
    private final Viewport viewport = new FitViewport(640, 240, camera);

    private final SpriteBatch batch = new SpriteBatch();

    float width = 6400;
    float height = 2100;

    float x = viewport.getWorldWidth() / 2f;
    float y = viewport.getWorldHeight() / 2f;

    Texture bg;
    Music music;

    @Override
    protected void create() {
        addInputProcessor(this);
        setBackground();
        setCamera(x, y);
        setMusic("bensound-ukulele.mp3");
    }

    @Override
    public void render(float delta) {
        camera.update();
        camera.setPosition(x, y);
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.disableBlending();
        batch.draw(bg, 0, 0, width, height);
        batch.enableBlending();
        batch.end();

        update(Gdx.graphics.getDeltaTime());
    }

    private void update(float delta) {
        float v = delta * 400 * camera.zoom;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) x = x - v;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) x = x + v;
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) y = y + v;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) y = y - v;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        music.dispose();
    }

    private void setMusic(String song) {
        music = Gdx.audio.newMusic(Gdx.files.internal(song));
        music.setLooping(true);
        music.setVolume(0.1f);
        music.play();
    }

    private void setBackground() {
        bg = new Texture(Gdx.files.internal("bg.png"));
    }

    private void setCamera(float x, float y) {
        camera.setPosition(x, y);
        camera.update();
    }

    @Override
    public void hide() {

    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        camera.zoom += 0.2 * amountY;
        return true;
    }
}
