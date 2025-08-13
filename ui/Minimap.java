package com.Client.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.Color;
import com.Client.map.MapRenderer;

public class Minimap extends Actor {
    private final ShapeRenderer shapeRenderer;
    private final MapRenderer map;

    public Minimap(MapRenderer map, ShapeRenderer sr){ this.map = map; this.shapeRenderer = sr; }

    @Override public void draw(Batch batch, float parentAlpha) {
        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0,0,0,0.6f);
        shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
        shapeRenderer.end();
        batch.begin(); // icons drawn from main renderer; keep panel only to be light
    }
}
