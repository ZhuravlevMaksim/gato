package com.gato.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.gato.game.Meow;
import com.gato.game.actors.Birds;
import com.gato.game.actors.Gato;
import com.rafaskoberg.gdx.typinglabel.TypingLabel;
import de.eskalon.commons.screen.ManagedScreen;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameScreen extends ManagedScreen {

    OrthographicCamera camera;

    SpriteBatch batch;
    Texture bg;
    Music music;

    static final float viewportWidth = 48;
    static final float viewportHeight = 32;

    TypingLabel label;

    Gato gato;
    Stage stage;
    Birds birdsNest;
    Meow meow;

    float ambient = .5f;

    @Override
    protected void create() {

        camera = new OrthographicCamera(viewportWidth, viewportHeight);
        camera.position.set(0, viewportHeight / 2f, 0);
        camera.update();
        batch = new SpriteBatch();
        bg = new Texture(Gdx.files.internal("bg.png"));
        bg.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        bg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        gato = new Gato();
        birdsNest = new Birds();
        meow = new Meow();

        stage = new Stage(new ScreenViewport());
        stage.addActor(gato);
        stage.addActor(meow);
        setText();

        setMusic(0, "bensound-ukulele.mp3");
    }

    @Override
    public void hide() {

    }

    private void setText() {
        Table table = new Table();
        table.setFillParent(true);
        label = new TypingLabel("{EASE}{SPEED=SLOWER}Hello{WAIT=2}, traveler{SPEED}.\n" +
                "Your journey begins here {SPEED=0.2}{WAIT=2} with ... {SPEED=0.1}{SHAKE}gato{ENDSHAKE}.",
                new Skin(Gdx.files.internal("uiskin.json")));

        table.add(label).top().center().expand();

        stage.addActor(table);
    }

    int sourceX = 0;

    @Override
    public void render(float delta) {
        camera.update();

        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);

        if (sourceX > 1000000) sourceX = 0;
        sourceX += 1;

        batch.begin();

        batch.disableBlending();
        batch.draw(bg, -viewportWidth / 2, 0, 5, 5, 48, 32, 1, 1, 0, sourceX, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, false);
        batch.enableBlending();

        birdsNest.getBirds().forEach(bird -> bird.draw(batch));

        meow.setPosition(gato.x + gato.width - 40, gato.y + gato.height / 2f);


        stage.act();
        stage.draw();

        batch.end();

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void dispose() {
        batch.dispose();
        music.dispose();
    }

    private void setMusic(Integer initialDelay, String song) {

        music = Gdx.audio.newMusic(Gdx.files.internal(song));
        music.setLooping(true);
        music.setVolume(0.1f);

        if (initialDelay > 0) {
            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
            service.scheduleWithFixedDelay(new Runnable() {
                float volume = 0;

                @Override
                public void run() {
                    music.setVolume(volume += 0.05);
                    music.play();
                    if (volume >= 1) {
                        service.shutdown();
                    }
                }
            }, initialDelay, 1, TimeUnit.SECONDS);
        } else {
            music.play();
        }

    }

    private FixtureDef fixture(float radius) {
        CircleShape ballShape = new CircleShape();
        ballShape.setRadius(radius);

        FixtureDef def = new FixtureDef();
        def.restitution = 0.3f;
        def.friction = 0.01f;
        def.shape = ballShape;
        def.density = 0.2f;

        return def;
    }
}
