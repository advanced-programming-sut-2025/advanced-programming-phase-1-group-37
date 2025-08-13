package com.Client.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;

public class ModalShade extends Actor {
    private final ShapeRenderer shapes = new ShapeRenderer();
    @Override public void draw(Batch batch, float parentAlpha) {
        if (!isVisible()) return;
        batch.end();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0, 0, 0, 0.45f * parentAlpha);
        shapes.rect(0, 0, getStage().getWidth(), getStage().getHeight());
        shapes.end();
        batch.begin();
    }
    @Override public void setVisible(boolean visible) {
        super.setVisible(visible);
        setTouchable(visible ? Touchable.enabled : Touchable.disabled);
    }
     public void dispose() { shapes.dispose(); }
}
