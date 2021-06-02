package com.gato.game;

import box2dLight.Light;
import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class GatoGame extends InputAdapter implements ApplicationListener {


    static final int RAYS_PER_BALL = 1128;
    static final int BALLSNUM = 10;
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

    Gato gato;
    HashMap<Integer, Vector2> movePosition = new HashMap<>();

    @Override
    public void create() {
        MathUtils.random.setSeed(Long.MIN_VALUE);
        camera = new OrthographicCamera(viewportWidth, viewportHeight);
        camera.position.set(0, viewportHeight / 2f, 0);
        camera.update();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.RED);
        bg = new Texture(Gdx.files.internal("bg.png"));
        gato = new Gato(-viewportWidth / 2f);

        createPhysicsWorld();
        Gdx.input.setInputProcessor(this);

        normalProjection.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        RayHandler.setGammaCorrection(true);
        RayHandler.useDiffuseLight(true);

        rayHandler = new RayHandler(world);
        rayHandler.setAmbientLight(0f, 0f, 0f, 0.5f);
        rayHandler.setBlurNum(3);

        initPointLights();

        setMusic();
    }

    @Override
    public void render() {
        camera.update();

        boolean stepped = fixedStep(Gdx.graphics.getDeltaTime());
        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.disableBlending();

        batch.begin();
        {
            batch.draw(bg, -viewportWidth / 2f, 0, viewportWidth, viewportHeight);
            batch.enableBlending();
            batch.draw(gato.texture(), gato.x, gato.y, gato.width, gato.height);
        }
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
        gato.updatePosition(testPoint.x);

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
        gato.updatePosition(testPoint.x);
        if (mouseJoint != null) {
            mouseJoint.setTarget(target);
        }
        return false;
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

        world = new World(new Vector2(0, 0), true);

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
        gato.dispose();
    }

    private void setMusic() {
        world = new World(new Vector2(0, -10), true);
        music = Gdx.audio.newMusic(Gdx.files.internal("1.mp3"));
        music.setLooping(true);

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
        }, 3, 1, TimeUnit.SECONDS);
    }

}
