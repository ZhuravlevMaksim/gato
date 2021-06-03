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
// Create our body definition
        BodyDef groundBodyDef = new BodyDef();
// Set its world position
        groundBodyDef.position.set(new Vector2(0, 10));

// Create a body from the definition and add it to the world
        Body groundBody = world.createBody(groundBodyDef);

// Create a polygon shape
        PolygonShape groundBox = new PolygonShape();
// Set the polygon shape as a box which is twice the size of our view port and 20 high
// (setAsBox takes half-width and half-height as arguments)
        groundBox.setAsBox(camera.viewportWidth, 10.0f);
// Create a fixture from our polygon shape and add it to our ground body
        groundBody.createFixture(groundBox, 0.0f);
// Clean up after ourselves
        groundBox.dispose();
    }

    private static void body(World world) {
        // First we create a body definition
        BodyDef bodyDef = new BodyDef();
// We set our body to dynamic, for something like ground which doesn't move we would set it to StaticBody
        bodyDef.type = BodyDef.BodyType.DynamicBody;
// Set our body's starting position in the world
        bodyDef.position.set(100, 300);

// Create our body in the world using our body definition
        Body body = world.createBody(bodyDef);

// Create a circle shape and set its radius to 6
        CircleShape circle = new CircleShape();
        circle.setRadius(6f);

// Create a fixture definition to apply our shape to
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.6f; // Make it bounce a little bit

// Create our fixture and attach it to the body
        Fixture fixture = body.createFixture(fixtureDef);

        Vector2 vel = body.getLinearVelocity();
        Vector2 pos = body.getPosition();

// apply left impulse, but only if max velocity is not reached yet
        if (Gdx.input.isKeyPressed(Input.Keys.A) && vel.x > -MAX_VELOCITY) {
            body.applyLinearImpulse(-0.80f, 0, pos.x, pos.y, true);
        }

// apply right impulse, but only if max velocity is not reached yet
        if (Gdx.input.isKeyPressed(Input.Keys.D) && vel.x < MAX_VELOCITY) {
            body.applyLinearImpulse(0.80f, 0, pos.x, pos.y, true);
        }

// Remember to dispose of any shapes after you're done with them!
// BodyDef and FixtureDef don't need disposing, but shapes do.
        circle.dispose();
    }
}
