package com.Client.map;

import com.badlogic.gdx.math.Vector2;

public class Smoother {
    private final Vector2 renderPos = new Vector2();
    private final Vector2 targetPos = new Vector2();
    private float snapDistance = 30f;
    private float lerpSpeed = 12f; // units per second factor

    public Smoother(float x, float y) { renderPos.set(x,y); targetPos.set(x,y); }

    public void setTarget(float x, float y) { targetPos.set(x,y); }

    public Vector2 getRenderPos() { return renderPos; }

    public void update(float dt) {
        if (renderPos.dst2(targetPos) > snapDistance * snapDistance) {
            renderPos.set(targetPos);
        } else {
            float alpha = Math.min(1f, lerpSpeed * dt);
            renderPos.lerp(targetPos, alpha);
        }
    }
}
