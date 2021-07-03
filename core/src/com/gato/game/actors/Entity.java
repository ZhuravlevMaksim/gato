package com.gato.game.actors;

import com.badlogic.gdx.math.Vector2;
import com.dongbat.jbump.Item;
import com.dongbat.jbump.World;

public class Entity {

    Vector2 position;
    float width, height;

    public Entity(Vector2 position, float width, float height) {
        this.position = position;
        this.width = width;
        this.height = height;
    }

    public void add(World<Entity> world){
        world.add(new Item<>(this), position.x, position.y, width, height);
    }
}
