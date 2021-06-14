package com.gato.game;

import box2dLight.DirectionalLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
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
import com.rafaskoberg.gdx.typinglabel.TypingLabel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class GatoGame extends InputAdapter implements ApplicationListener {


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
    public void create() {
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
        Gdx.input.setInputProcessor(this);

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
        setMusic(0);
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
    public void render() {
        camera.update();

        boolean stepped = fixedStep(Gdx.graphics.getDeltaTime());
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
        if (stepped) rayHandler.update();
        rayHandler.render();
    }

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
    public void resize(int width, int height) {

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
        ballShape.setRadius(MathUtils.random(1f, 4f));

        FixtureDef def = new FixtureDef();
        def.restitution = 0.3f;
        def.friction = 0.01f;
        def.shape = ballShape;
        def.density = 0.2f;

        BodyDef boxBodyDef = new BodyDef();
        boxBodyDef.type = BodyDef.BodyType.DynamicBody;

        for (int i = 0; i < BALLSNUM; i++) {
            boxBodyDef.position.x = MathUtils.random(-25, 60);
            boxBodyDef.position.y = 32;
            Body boxBody = world.createBody(boxBodyDef);
            boxBody.createFixture(def);
            balls.add(boxBody);
        }
        ballShape.dispose();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    float physicsTimeLeft;
    private final static int MAX_FPS = 30;
    private final static int MIN_FPS = 15;
    public final static float TIME_STEP = 1f / MAX_FPS;
    private final static float MAX_STEPS = 1f + MAX_FPS / MIN_FPS;
    private final static float MAX_TIME_PER_FRAME = TIME_STEP * MAX_STEPS;
    private final static int VELOCITY_ITERS = 6;
    private final static int POSITION_ITERS = 2;

    private boolean fixedStep(float delta) {
        physicsTimeLeft += delta;
        if (physicsTimeLeft > MAX_TIME_PER_FRAME)
            physicsTimeLeft = MAX_TIME_PER_FRAME;

        boolean stepped = false;
        while (physicsTimeLeft >= TIME_STEP) {
            world.step(TIME_STEP, VELOCITY_ITERS, POSITION_ITERS);
            physicsTimeLeft -= TIME_STEP;
            stepped = true;
        }
        return stepped;
    }

    @Override
    public void dispose() {
        batch.dispose();
        music.dispose();
        rayHandler.dispose();
    }

    private void setMusic(Integer initialDelay) {

        music = Gdx.audio.newMusic(Gdx.files.internal("bensound-onceagain.mp3"));
        music.setLooping(true);

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

    @Override
    public boolean scrolled(float amountX, float amountY) {
        ambient += amountY / 100;
        rayHandler.setAmbientLight(ambient, ambient, ambient, 0.5f);
        return false;
    }
}
