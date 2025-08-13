package com.Client.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.common.Network;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapRenderer {
    public static class Entity {
        public String id; public Smoother smoother;
        public Entity(String id, float x, float y){ this.id = id; this.smoother = new Smoother(x,y); }
    }

    private final Map<String, Entity> players = new ConcurrentHashMap<>();
    private final Map<String, Entity> npcs    = new ConcurrentHashMap<>();

    public void applySnapshot(java.util.List<Network.PlayerPosition> pp,
                              java.util.List<Network.NPCPosition> np){
        if (pp != null) for (Network.PlayerPosition p : pp) {
            players.computeIfAbsent(p.username, k -> new Entity(k, p.x, p.y)).smoother.setTarget(p.x, p.y);
        }
        if (np != null) for (Network.NPCPosition n : np) {
            npcs.computeIfAbsent(n.npcId, k -> new Entity(k, n.x, n.y)).smoother.setTarget(n.x, n.y);
        }
    }

    public void tick(float dt){
        players.values().forEach(e -> e.smoother.update(dt));
        npcs.values().forEach(e -> e.smoother.update(dt));
    }

    public void drawWorld(OrthographicCamera cam, ShapeRenderer shapes, int hourOfDay){
        // Sky gradient + ground
        shapes.setProjectionMatrix(cam.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < 12; i++){
            float t = i/12f;
            Color c = (hourOfDay >= 6 && hourOfDay <= 18)
                ? new Color(0.2f, 0.4f + 0.3f*t, 0.8f, 1)
                : new Color(0.05f, 0.05f + 0.1f*t, 0.15f + 0.15f*t, 1);
            shapes.setColor(c);
            shapes.rect(cam.position.x - cam.viewportWidth, 60 * i, cam.viewportWidth*2, 60);
        }
        shapes.setColor(Color.FOREST);
        shapes.rect(cam.position.x - 2000, -2000, 4000, 2000);

        // subtle grid
        shapes.setColor(0f, 0f, 0f, 0.06f);
        float left = cam.position.x - cam.viewportWidth/2f;
        float right = cam.position.x + cam.viewportWidth/2f;
        for (int gx = (int)(left/40f)*40; gx < right; gx += 40) shapes.rectLine(gx, -2000, gx, 2000, 1f);
        shapes.end();
    }

    public void drawEntities(OrthographicCamera cam, ShapeRenderer shapes, SpriteBatch batch, BitmapFont font,
                             String myUsername){
        shapes.setProjectionMatrix(cam.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        players.values().forEach(e -> {
            boolean me = e.id.equals(myUsername);
            float x = e.smoother.getRenderPos().x;
            float y = e.smoother.getRenderPos().y;
            shapes.setColor(Color.BLACK); shapes.circle(x, y, 12);
            shapes.setColor(me ? Color.GOLD : Color.SKY); shapes.circle(x, y, 10);
        });
        npcs.values().forEach(e -> {
            float x = e.smoother.getRenderPos().x;
            float y = e.smoother.getRenderPos().y;
            shapes.setColor(Color.BLACK); shapes.circle(x, y, 12);
            shapes.setColor(Color.LIME); shapes.circle(x, y, 10);
        });
        shapes.end();

        batch.setProjectionMatrix(cam.combined);
        batch.begin();
        players.values().forEach(e -> {
            float x = e.smoother.getRenderPos().x;
            float y = e.smoother.getRenderPos().y;
            font.draw(batch, e.id, x - 24, y + 28);
        });
        npcs.values().forEach(e -> {
            float x = e.smoother.getRenderPos().x;
            float y = e.smoother.getRenderPos().y;
            font.draw(batch, e.id, x - 24, y + 28);
        });
        batch.end();
    }
}
