package com.gato.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class PBody {

    private static final float MAX_VELOCITY = 100;
    public static void create(World world, Camera camera) {
        ground(world, camera);
        body(world);
    }

    private static void ground(World world, Camera camera) {
        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.position.set(new Vector2(0, 10));
        Body groundBody = world.createBody(groundBodyDef);
        PolygonShape groundBox = new PolygonShape();
        groundBox.setAsBox(camera.viewportWidth, 10.0f);
        groundBody.createFixture(groundBox, 0.0f);
        groundBox.dispose();
    }

    private static void body(World world) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(100, 300);
        Body body = world.createBody(bodyDef);
        CircleShape circle = new CircleShape();
        circle.setRadius(6f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.6f;
        Fixture fixture = body.createFixture(fixtureDef);
        Vector2 vel = body.getLinearVelocity();
        Vector2 pos = body.getPosition();
        if (Gdx.input.isKeyPressed(Input.Keys.A) && vel.x > -MAX_VELOCITY) {
            body.applyLinearImpulse(-0.80f, 0, pos.x, pos.y, true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) && vel.x < MAX_VELOCITY) {
            body.applyLinearImpulse(0.80f, 0, pos.x, pos.y, true);
        }
        circle.dispose();
    }
}
