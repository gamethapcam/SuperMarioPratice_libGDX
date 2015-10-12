package com.ychstudio.actors.maptiles;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.ychstudio.actors.Collider;
import com.ychstudio.actors.Mario;
import com.ychstudio.gamesys.GameManager;
import com.ychstudio.screens.PlayScreen;

/**
 * Created by yichen on 10/11/15.
 *
 * Brick
 */
public class Brick extends MapTileObject {

    public Brick(PlayScreen playScreen, float x, float y, TextureRegion textureRegion) {
        super(playScreen, x, y, textureRegion);
    }

    @Override
    protected void defBody() {

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(getX(), getY());
        bodyDef.type = BodyDef.BodyType.StaticBody;

        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(16 / GameManager.PPM / 2, 16 / GameManager.PPM / 2);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.filter.categoryBits = GameManager.GROUND_BIT;
        fixtureDef.shape = shape;

        body.createFixture(fixtureDef).setUserData(this);

        shape.dispose();
    }

    @Override
    public void onTrigger(Collider other) {

        if (other.getFilter().categoryBits == GameManager.MARIO_HEAD_BIT) {

            if (((Mario)other.getUserData()).isGrownUp()) {
                // TODO: break brick

                GameManager.instance.getAssetManager().get("audio/sfx/breakblock.wav", Sound.class).play();
            }
            else {
                GameManager.instance.getAssetManager().get("audio/sfx/bump.wav", Sound.class).play();
            }

        }
    }
}
