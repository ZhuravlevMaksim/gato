package com.gato.game.screen;

import box2dLight.DirectionalLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.gato.game.Birds;
import com.gato.game.Meow;
import com.rafaskoberg.gdx.typinglabel.TypingLabel;
import de.eskalon.commons.screen.ManagedScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameScreen extends ManagedScreen {

    static final int RAYS_PER_BALL = 1128;
    static final int BALLSNUM = 12;

    OrthographicCamera camera;

    SpriteBatch batch;
    Texture bg;
    Music music;

    World world;
    ArrayList<Body> balls = new ArrayList<>(BALLSNUM);
    DirectionalLight light;

    Matrix4 normalProjection = new Matrix4();

    RayHandler rayHandler;

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

        addInputProcessor(new InputProcessor() {

            boolean night = false;
            boolean day = true;

            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case Input.Keys.F1:
                        light.setColor(
                                MathUtils.random(),
                                MathUtils.random(),
                                MathUtils.random(),
                                1f);
                        return true;

                    default:
                        return false;

                }
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
                ambient += amountY / 100;
                if (ambient > 1) ambient = 1;
                if (ambient < 0) ambient = 0;

                rayHandler.setAmbientLight(ambient, ambient, ambient, 0.5f);

                if (ambient < 0.1 && !night) {
                    music.stop();
                    night = true;
                    day = false;
                    setMusic(0, "bensound-ofeliasdream.mp3");

                    for (Body ball : balls) {
                        ball.createFixture(fixture(6f));
                    }

                }

                if (ambient > 0.3 && !day) {
                    music.stop();
                    night = false;
                    day = true;
                    setMusic(0, "bensound-cute.mp3");

                    for (Body ball : balls) {
                        ball.createFixture(fixture(MathUtils.random(1f, 4f)));
                    }
                }

                return false;
            }
        });

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

        createPhysicsWorld();

        normalProjection.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        RayHandler.setGammaCorrection(true);
        RayHandler.useDiffuseLight(true);

        rayHandler = new RayHandler(world);
        rayHandler.setAmbientLight(ambient, ambient, ambient, 0.5f);
        rayHandler.setBlurNum(3);

        stage = new Stage(new ScreenViewport());
        stage.addActor(gato);
        stage.addActor(meow);
        setText();

        initDirectionalLight();

        world = new World(new Vector2(0, -10), true);
        setMusic(0, "bensound-ukulele.mp3");

    }

    void initDirectionalLight() {
        clearLights();
        light = new DirectionalLight(rayHandler, 4 * RAYS_PER_BALL, null, 240f);
    }

    void clearLights() {
        if (light != null) {
            light.remove();
        }
    }

    private void createPhysicsWorld() {
        world = new World(new Vector2(0, -10), true);
        CircleShape ballShape = new CircleShape();

        FixtureDef fixture = fixture(MathUtils.random(1f, 4f));

        BodyDef boxBodyDef = new BodyDef();
        boxBodyDef.type = BodyDef.BodyType.DynamicBody;

        for (int i = 0; i < BALLSNUM; i++) {
            boxBodyDef.position.x = MathUtils.random(-25, 60);
            boxBodyDef.position.y = 32;
            Body boxBody = world.createBody(boxBodyDef);
            boxBody.createFixture(fixture);
            balls.add(boxBody);
        }
        ballShape.dispose();
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
        batch.disableBlending();
        if (sourceX > 1000000) sourceX = 0;
        sourceX += 1;

        List<Birds.Bird> birds = birdsNest.getBirds();

        batch.begin();
        batch.draw(bg, -viewportWidth / 2, 0, 5, 5, 48, 32, 1, 1, 0, sourceX, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, false);
        batch.enableBlending();

        birds.forEach(bird -> bird.draw(batch));

        batch.end();

        batch.setProjectionMatrix(normalProjection);
        meow.setPosition(gato.x + gato.width - 40, gato.y + gato.height / 2f);

        batch.begin();
        stage.act();
        stage.draw();
        batch.end();

        for (Body ball : balls) {
            Vector2 position = ball.getPosition();
            if (position.x < -30) {
                position.x = 60;
            }
            ball.setTransform(position.x -= Gdx.graphics.getDeltaTime() * 5, 32, 0);
        }
        rayHandler.setCombinedMatrix(camera);
        rayHandler.update();
        rayHandler.render();

        world.step(1/60f, 6, 2);
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void dispose() {
        batch.dispose();
        music.dispose();
        rayHandler.dispose();
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
