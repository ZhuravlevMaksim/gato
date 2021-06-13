package com.gato.game;

import box2dLight.*;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.rafaskoberg.gdx.typinglabel.TypingLabel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class GatoGame extends InputAdapter implements ApplicationListener {


    static final int RAYS_PER_BALL = 1128;
    static final int BALLSNUM = 8;
    static final float LIGHT_DISTANCE = 16f;
    static final float RADIUS = 1f;

    OrthographicCamera camera;

    SpriteBatch batch;
    Texture bg;
    Music music;
    BitmapFont font;

    World world;
    ArrayList<Body> balls = new ArrayList<>(BALLSNUM);
    Body groundBody;

    Matrix4 normalProjection = new Matrix4();

    RayHandler rayHandler;
    ArrayList<Light> lights = new ArrayList<>(BALLSNUM);

    Body hitBody = null;
    MouseJoint mouseJoint = null;

    static final float viewportWidth = 48;
    static final float viewportHeight = 32;

    TypingLabel label;

    Gato gato;
    HashMap<Integer, Vector2> movePosition = new HashMap<>();
    Stage stage;
    Birds birdsNest;
    Meow meow;

    @Override
    public void create() {
        camera = new OrthographicCamera(viewportWidth, viewportHeight);
        camera.position.set(0, viewportHeight / 2f, 0);
        camera.update();
        batch = new SpriteBatch();
        bg = new Texture(Gdx.files.internal("bg.png"));
        bg.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        bg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font = new BitmapFont();
        gato = new Gato();
        birdsNest = new Birds();
        meow = new Meow();

        createPhysicsWorld();
        Gdx.input.setInputProcessor(this);

        normalProjection.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        RayHandler.setGammaCorrection(true);
        RayHandler.useDiffuseLight(true);

        rayHandler = new RayHandler(world);
        rayHandler.setAmbientLight(0f, 0f, 0f, 0.5f);
        rayHandler.setBlurNum(3);

        stage = new Stage(new ScreenViewport());
        stage.addActor(gato);
        stage.addActor(meow);
        setText();

        initDirectionalLight();

        world = new World(new Vector2(0, -10), true);
        setMusic(world, 0);
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

        for (int i = 0; i < balls.size(); i++) {
            Vector2 move = movePosition.putIfAbsent(i, new Vector2(MathUtils.random(-35, 23), MathUtils.random(0, viewportHeight)));
            Body ball = balls.get(i);
            Vector2 position = ball.getPosition();
            if (move == null) continue;
            float x = 0;
            float y = 0;
            if (position.x < move.x) {
                x = position.x + 0.02f;
            }
            if (position.x > move.x) {
                x = position.x - 0.02f;
            }
            if (position.y < move.y) {
                y = position.y + 0.02f;
            }
            if (position.y > move.y) {
                y = position.y - 0.02f;
            }
            ball.setTransform(x, y, 0);
            if (lightsType == 3){
                ball.setTransform(x, 32, 0);
            }
            if (Math.abs(position.x - move.x) <= 0.1f) {
                movePosition.remove(i);
            }
        }
        rayHandler.setCombinedMatrix(camera);
        if (stepped) rayHandler.update();
        rayHandler.render();
    }

    Vector3 testPoint = new Vector3();
    QueryCallback callback = new QueryCallback() {
        @Override
        public boolean reportFixture(Fixture fixture) {
            if (fixture.getBody() == groundBody)
                return true;

            if (fixture.testPoint(testPoint.x, testPoint.y)) {
                hitBody = fixture.getBody();
                return false;
            } else
                return true;
        }
    };

    @Override
    public boolean touchDown(int x, int y, int pointer, int newParam) {
        // translate the mouse coordinates to world coordinates
        testPoint.set(x, y, 0);
        camera.unproject(testPoint);
//        gato.updatePosition(testPoint.x);

        // ask the world which bodies are within the given
        // bounding box around the mouse pointer
        hitBody = null;
        world.QueryAABB(callback, testPoint.x - 0.3f, testPoint.y - 0.3f,
                testPoint.x + 0.3f, testPoint.y + 0.3f);

        // if we hit something we create a new mouse joint
        // and attach it to the hit body.
        if (hitBody != null) {
            MouseJointDef def = new MouseJointDef();
            def.bodyA = groundBody;
            def.bodyB = hitBody;
            def.collideConnected = true;
            def.target.set(testPoint.x, testPoint.y);
            def.maxForce = 1000.0f * hitBody.getMass();

            mouseJoint = (MouseJoint) world.createJoint(def);
            hitBody.setAwake(true);
        }

        return false;
    }

    Vector2 target = new Vector2();

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        camera.unproject(testPoint.set(x, y, 0));
        target.set(testPoint.x, testPoint.y);
//        gato.updatePosition(testPoint.x);
        if (mouseJoint != null) {
            mouseJoint.setTarget(target);
        }
        return false;
    }

    /**
     * Type of lights to use:
     * 0 - PointLight
     * 1 - ConeLight
     * 2 - ChainLight
     * 3 - DirectionalLight
     */
    int lightsType = 3;

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {

            case Input.Keys.F1:
                if (lightsType != 0) {
                    initPointLights();
                    lightsType = 0;
                }
                return true;

            case Input.Keys.F2:
                if (lightsType != 1) {
                    initConeLights();
                    lightsType = 1;
                }
                return true;

            case Input.Keys.F3:
                if (lightsType != 2) {
                    initChainLights();
                    lightsType = 2;
                }
                return true;

            case Input.Keys.F4:
                if (lightsType != 3) {
                    initDirectionalLight();
                    lightsType = 3;
                }
                return true;

            case Input.Keys.F5:
                for (Light light : lights)
                    light.setColor(
                            MathUtils.random(),
                            MathUtils.random(),
                            MathUtils.random(),
                            1f);
                return true;

            case Input.Keys.F6:
                for (Light light : lights)
                    light.setDistance(MathUtils.random(LIGHT_DISTANCE * 0.5f, LIGHT_DISTANCE * 2f));
                return true;

            case Input.Keys.F9:
                rayHandler.diffuseBlendFunc.reset();
                return true;

            case Input.Keys.F10:
                rayHandler.diffuseBlendFunc.set(GL20.GL_DST_COLOR, GL20.GL_SRC_COLOR);
                return true;

            case Input.Keys.F11:
                rayHandler.diffuseBlendFunc.set(GL20.GL_SRC_COLOR, GL20.GL_DST_COLOR);
                return true;

            default:
                return false;

        }
    }

    void initConeLights() {
        clearLights();
        for (int i = 0; i < BALLSNUM; i++) {
            ConeLight light = new ConeLight(rayHandler, RAYS_PER_BALL, null, LIGHT_DISTANCE, 0, 0, 0f, MathUtils.random(15f, 40f));
            light.attachToBody(balls.get(i), RADIUS / 2f, RADIUS / 2f, MathUtils.random(0f, 360f));
            light.setColor(
                    MathUtils.random(),
                    MathUtils.random(),
                    MathUtils.random(),
                    1f);
            lights.add(light);
        }
    }

    void initChainLights() {
        clearLights();
        for (int i = 0; i < BALLSNUM; i++) {
            ChainLight light = new ChainLight(rayHandler, RAYS_PER_BALL, null, LIGHT_DISTANCE, 1, new float[]{-5, 0, 0, 3, 5, 0});
            light.attachToBody(
                    balls.get(i),
                    MathUtils.random(0f, 360f));
            light.setColor(
                    MathUtils.random(),
                    MathUtils.random(),
                    MathUtils.random(),
                    1f);
            lights.add(light);
        }
    }

    float sunDirection = -90f;

    void initDirectionalLight() {
        clearLights();

        groundBody.setActive(false);
        sunDirection = 240f;

        DirectionalLight light = new DirectionalLight(rayHandler, 4 * RAYS_PER_BALL, null, sunDirection);
        lights.add(light);
    }

    @Override
    public boolean mouseMoved(int x, int y) {
        testPoint.set(x, y, 0);
        camera.unproject(testPoint);
        return false;
    }

    @Override
    public void resize(int width, int height) {

    }

    void initPointLights() {
        clearLights();
        for (int i = 0; i < BALLSNUM; i++) {
            PointLight light = new PointLight(rayHandler, RAYS_PER_BALL, null, LIGHT_DISTANCE, 0f, 0f);
            light.attachToBody(balls.get(i), RADIUS / 2f, RADIUS / 2f);
            light.setColor(
                    MathUtils.random(),
                    MathUtils.random(),
                    MathUtils.random(),
                    1f);
            lights.add(light);
        }
    }

    void clearLights() {
        if (lights.size() > 0) {
            for (Light light : lights) {
                light.remove();
            }
            lights.clear();
        }
        groundBody.setActive(true);
    }

    private void createPhysicsWorld() {

        world = new World(new Vector2(0, -10), true);

        float halfWidth = viewportWidth / 2f;
        ChainShape chainShape = new ChainShape();
        chainShape.createLoop(new Vector2[]{
                new Vector2(-halfWidth, 0f),
                new Vector2(halfWidth, 0f),
                new Vector2(halfWidth, viewportHeight),
                new Vector2(-halfWidth, viewportHeight)});
        BodyDef chainBodyDef = new BodyDef();
        chainBodyDef.type = BodyDef.BodyType.StaticBody;
        groundBody = world.createBody(chainBodyDef);
        groundBody.createFixture(chainShape, 0);
        chainShape.dispose();
        createBoxes();
    }

    private void createBoxes() {
        CircleShape ballShape = new CircleShape();
        ballShape.setRadius(1f);

        FixtureDef def = new FixtureDef();
        def.restitution = 0.9f;
        def.friction = 0.01f;
        def.shape = ballShape;
        def.density = 1f;
        BodyDef boxBodyDef = new BodyDef();
        boxBodyDef.type = BodyDef.BodyType.DynamicBody;

        for (int i = 0; i < BALLSNUM; i++) {
            // Create the BodyDef, set a random position above the
            // ground and create a new body
            boxBodyDef.position.x = -20 + (float) (Math.random() * 40);
            boxBodyDef.position.y = 10 + (float) (Math.random() * 15);
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
        font.dispose();
        rayHandler.dispose();
    }

    private void setMusic(World world, Integer initialDelay) {

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
        camera.rotate(amountY * 3f, 0, 0, 1);
        return false;
    }
}
