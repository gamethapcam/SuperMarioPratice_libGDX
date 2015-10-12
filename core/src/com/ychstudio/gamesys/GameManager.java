package com.ychstudio.gamesys;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

/**
 * Created by yichen on 10/11/15.
 *
 * GameManager
 */
public class GameManager implements Disposable {

    public static GameManager instance;

    public static final float PPM = 16;

    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;

    public static final float V_WIDTH = 24.0f;
    public static final float V_HEIGHT = 18.0f;

    public static final Vector2 GRAVITY = new Vector2(0.0f, -9.8f * 4);

    public static final float STEP = 1 / 60.0f;

    public static final short GROUND_BIT = 1;
    public static final short MARIO_BIT = 1 << 1;
    public static final short MARIO_HEAD_BIT = 1 << 2;

    private AssetManager assetManager;

    private int score;

    public GameManager() {
        if (instance == null) {
            instance = this;
        }

        if (assetManager == null) {
            assetManager = new AssetManager();
        }

        score = 0;
    }

    public int getScore() {
        return score;
    }

    public void clearScore() {
        score = 0;
    }

    public void addScore(int value) {
        score += value;
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }


    @Override
    public void dispose() {
        assetManager.dispose();
    }
}
