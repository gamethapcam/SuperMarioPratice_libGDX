package com.ychstudio.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ychstudio.SuperMario;
import com.ychstudio.actors.Mario;
import com.ychstudio.actors.enemies.Enemy;
import com.ychstudio.actors.items.Item;
import com.ychstudio.actors.items.Mushroom;
import com.ychstudio.actors.items.SpawningItem;
import com.ychstudio.actors.maptiles.MapTileObject;
import com.ychstudio.gamesys.GameManager;
import com.ychstudio.hud.Hud;
import com.ychstudio.utils.WorldContactListener;
import com.ychstudio.utils.WorldCreator;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by yichen on 10/11/15.
 *
 * PlayScreen
 */
public class PlayScreen implements Screen {

    private SuperMario game;

    public World world;

    private float accumulator;

    private OrthographicCamera camera;
    private Viewport viewport;

    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer mapRenderer;

    private float mapWidth;
    private float mapHeight;

    private TextureAtlas textureAtlas;

    private Box2DDebugRenderer box2DDebugRenderer;
    private boolean renderB2DDebug;

    private Array<MapTileObject> mapTileObjects;
    private Array<Enemy> enemies;
    private Array<Item> items;

    private LinkedBlockingDeque<SpawningItem> itemSpawnQuque;

    private Mario mario;

    private Hud hud;

    private AssetManager assetManager;

    public PlayScreen(SuperMario game) {
        this.game = game;
        assetManager = GameManager.instance.getAssetManager();
        loadAudio();
    }

    private void loadAudio() {
        assetManager.load("audio/music/mario_music.ogg", Music.class);
        assetManager.load("audio/sfx/breakblock.wav", Sound.class);
        assetManager.load("audio/sfx/bump.wav", Sound.class);
        assetManager.load("audio/sfx/coin.wav", Sound.class);
        assetManager.load("audio/sfx/jump_small.wav", Sound.class);
        assetManager.load("audio/sfx/jump_super.wav", Sound.class);
        assetManager.load("audio/sfx/mariodie.wav", Sound.class);
        assetManager.load("audio/sfx/powerdown.wav", Sound.class);
        assetManager.load("audio/sfx/powerup.wav", Sound.class);
        assetManager.load("audio/sfx/powerup_spawn.wav", Sound.class);
        assetManager.load("audio/sfx/stomp.wav", Sound.class);
        assetManager.finishLoading();
    }

    @Override
    public void show() {

        camera = new OrthographicCamera();

        viewport = new FitViewport(GameManager.V_WIDTH, GameManager.V_HEIGHT);
        viewport.setCamera(camera);

        camera.position.set(GameManager.V_WIDTH / 2, GameManager.V_HEIGHT / 2, 0);

        textureAtlas = new TextureAtlas("imgs/actors.atlas");

        // create Box2D world
        world = new World(GameManager.GRAVITY, true);
        world.setContactListener(new WorldContactListener());

        // load tmx tiled map
        TmxMapLoader tmxMapLoader = new TmxMapLoader();
        tiledMap = tmxMapLoader.load("maps/Level_1-1.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1 / GameManager.PPM);

        mapWidth = ((TiledMapTileLayer) tiledMap.getLayers().get(0)).getWidth();
        mapHeight = ((TiledMapTileLayer) tiledMap.getLayers().get(0)).getHeight();

        // create world from TmxTiledMap
        WorldCreator worldCreator = new WorldCreator(this, tiledMap);
        mapTileObjects = worldCreator.getMapTileObject();
        enemies = worldCreator.getEnemies();
        mario = new Mario(this, (worldCreator.getStartPosition().x + 8) / GameManager.PPM, (worldCreator.getStartPosition().y + 8) / GameManager.PPM);

        // for spawning
        items = new Array<Item>();
        itemSpawnQuque = new LinkedBlockingDeque<SpawningItem>();

        hud = new Hud(game.batch);
        hud.setLevel("1-1");

        accumulator = 0;

        assetManager.get("audio/music/mario_music.ogg", Music.class).setLooping(true);
        assetManager.get("audio/music/mario_music.ogg", Music.class).play();

        box2DDebugRenderer = new Box2DDebugRenderer();
        renderB2DDebug = false;

    }

    public TextureAtlas getTextureAtlas() {
        return textureAtlas;
    }

    public TiledMap getTiledMap() {
        return tiledMap;
    }

    public float getMapWidth() {
        return mapWidth;
    }

    public float getMapHeight() {
        return mapHeight;
    }

    public void addSpawnItem(float x, float y, Class<? extends Item> type) {
        itemSpawnQuque.add(new SpawningItem(x, y, type));
    }

    private void handleSpawningItem() {
        if (itemSpawnQuque.size() > 0) {
            SpawningItem spawningItem = itemSpawnQuque.poll();

            if (spawningItem.type == Mushroom.class) {
                items.add(new Mushroom(this, spawningItem.x, spawningItem.y));
            }

        }
    }

    public void handleInput() {
        // Press M to pause or continue background music
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            if (assetManager.get("audio/music/mario_music.ogg", Music.class).isPlaying()) {
                assetManager.get("audio/music/mario_music.ogg", Music.class).pause();
            }
            else {
                assetManager.get("audio/music/mario_music.ogg", Music.class).play();
            }
        }

        // Press B to toggle Box2DDebuggerRenderer
        if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
            renderB2DDebug = !renderB2DDebug;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT_BRACKET)) {
            float timeScale = GameManager.timeScale;
            GameManager.setTimeScale(timeScale - 0.2f);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT_BRACKET)) {
            float timeScale = GameManager.timeScale;
            GameManager.setTimeScale(timeScale + 0.2f);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.EQUALS)) {
            GameManager.setTimeScale(1.0f);
        }
    }

    public void handleMusic() {
        if (mario.isDead()) {
            assetManager.get("audio/music/mario_music.ogg", Music.class).stop();
        }
    }

    public void update(float delta) {
        delta *= GameManager.timeScale;
        float step = GameManager.STEP * GameManager.timeScale;

        handleInput();
        handleSpawningItem();
        handleMusic();

        // Box2D world step
        accumulator += delta;
        if (accumulator > step) {
            world.step(step, 6, 2);
            accumulator -= step;
        }

        // update map tile objects
        for (MapTileObject mapTileObject : mapTileObjects) {
            mapTileObject.update(delta);
        }

        // update enemies
        for (Enemy enemy : enemies) {
            enemy.update(delta);
        }

        // update items
        for (Item item : items) {
            item.update(delta);
        }

        // update Mario
        mario.update(delta);


        // camera control
        float targetX = camera.position.x;
        if (!mario.isDead())
            targetX = mario.getPosition().x;

        if (targetX < GameManager.V_WIDTH / 2) {
            targetX = GameManager.V_WIDTH / 2;
        }
        else if (targetX > mapWidth - GameManager.V_WIDTH / 2) {
            targetX = mapWidth - GameManager.V_WIDTH / 2;
        }

        camera.position.x = MathUtils.lerp(camera.position.x, targetX, 0.1f);
        camera.update();

        // update map renderer
        mapRenderer.setView(camera);

        // update HUD
        hud.update(delta);

//        cleanUpDestroyedObjects(); // do not need to clean yet as performance concerns
    }

    private void cleanUpDestroyedObjects() {
        for (int i = 0; i < mapTileObjects.size; i++) {
            if (mapTileObjects.get(i).isDestroyed()) {
                mapTileObjects.removeIndex(i);
            }
        }
    }

    public Vector2 getMarioPosition() {
        return mario.getPosition();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        update(delta);

        // draw map
        mapRenderer.render(new int[] {0, 1});

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        // draw map tile objects
        for (MapTileObject mapTileObject : mapTileObjects) {
            mapTileObject.draw(game.batch);
        }

        // draw enemies
        for (Enemy enemy : enemies) {
            enemy.draw(game.batch);
        }

        // draw items
        for (Item item : items) {
            item.draw(game.batch);
        }

        // draw Mario
        mario.draw(game.batch);

        game.batch.end();

        hud.draw();

        if (renderB2DDebug) {
            box2DDebugRenderer.render(world, camera.combined);
        }

    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        tiledMap.dispose();
        world.dispose();
        textureAtlas.dispose();
        box2DDebugRenderer.dispose();
        hud.dispose();
    }
}
