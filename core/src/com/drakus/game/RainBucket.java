package com.drakus.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class RainBucket extends ApplicationAdapter {
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Texture dropImage;
	private Texture bucketImage;
	private Texture gameOverImage;
	private BitmapFont font;
	//private Texture gameWonImage;
	private Sound dropSound;
	private Music rainMusic;
	private Rectangle bucket;
	private Vector3 touchPos;
	private Array<Rectangle> rainDrops;
	private long lastDropTime;

	//private boolean isPaused;
	private boolean isResumed;
	private boolean isOver;
	private int bottom;
	private int m_height;
	private int m_width;
	private int score;
	private int deltaScore;
	private String str;
	private FreeTypeFontGenerator generator;
	private FreeTypeFontGenerator.FreeTypeFontParameter parameter;

	@Override
	public void create () {

		m_width = 800;
		m_height = 480;

		camera = new OrthographicCamera();
		camera.setToOrtho(false, m_width, m_height);

		batch = new SpriteBatch();
		dropImage = new Texture("droplet.png");
		bucketImage = new Texture("bucket.png");
		gameOverImage = new Texture("game-over.jpg");

		generator = new FreeTypeFontGenerator(Gdx.files.internal("courbd.ttf"));
		parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.size = 23;
		//parameter.borderWidth = 2.0f;
		font = generator.generateFont(parameter);
		//font.setColor(Color.WHITE);
		//font.getData().setScale(1.4f, 1.4f);



		dropSound = Gdx.audio.newSound(Gdx.files.internal("waterdrop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("undertreeinrain.mp3"));

		rainMusic.setLooping(true);
		rainMusic.play();

		bucket = new Rectangle();

		bucket.x = m_width / 2 - 64 / 2;
		bucket.y = 20;
		bucket.width = 64;
		bucket.height = 64;

		bottom = 15;
		isOver = false;
		isResumed = true;
		//isPaused = false;
		score = 0;
		deltaScore = 1;
		str = "Score: " + score;
		//str = "Score: ";

		touchPos = new Vector3();

		rainDrops = new Array<Rectangle>();
		spawnDrops();

	}

	private void spawnDrops () {

		Rectangle rainDrop = new Rectangle();

		rainDrop.x = MathUtils.random(0, m_width - bucket.width);
		rainDrop.y = m_height;

		rainDrop.width = bucket.width;
		rainDrop.height = bucket.height;

		rainDrops.add(rainDrop);

		lastDropTime = TimeUtils.nanoTime();

	}


	@Override
	public void render () {

		if (isOver) {

			onFinish();

		} else {

			Gdx.gl.glClearColor(0, 0, 0.2f, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

			camera.update();

			batch.setProjectionMatrix(camera.combined);
			drawObjects();

			if (isResumed) {
				if (Gdx.input.isTouched()) {

					touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);

					camera.unproject(touchPos);

					bucket.x = touchPos.x - (bucket.width / 2);

				}

				//if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
				//bucket.x -= 200 * Gdx.graphics.getDeltaTime();
				//if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
				//bucket.x += 200 * Gdx.graphics.getDeltaTime();

				if (bucket.x < 0)
					bucket.x = 0;
				if (bucket.x > m_width - bucket.width)
					bucket.x = m_width - bucket.width;

				if (TimeUtils.nanoTime() - lastDropTime > 1000000000)
					spawnDrops();

				Iterator<Rectangle> iter = rainDrops.iterator();

				while (iter.hasNext()) {

					Rectangle raindrop = iter.next();
					raindrop.y -= 200 * Gdx.graphics.getDeltaTime();

					if (raindrop.y + 64 < bottom) {

						gameOver();

					}

					//if (raindrop.y + 64 < 0)
					//iter.remove();

					if (raindrop.overlaps(bucket)) {
						dropSound.play();
						iter.remove();
						score += deltaScore;
						str = "Score: " + score;
					}
				}
			}
		}

		drawScore();

	}


	private void onFinish() {


		//camera.update();

		//batch.setProjectionMatrix(camera.combined);

		//Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		//Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();
		batch.draw(gameOverImage, 0, 0);
		batch.end();

		camera.update();
	}

	private void drawScore() {

		batch.begin();
		//font.draw(batch, "Hello World", 500, 20);
		font.draw(batch, str, 630, 460);
		batch.end();

	}

	@Override
	public void dispose () {
		batch.dispose();
		dropSound.dispose();
		dropImage.dispose();
		bucketImage.dispose();
		rainMusic.dispose();
		font.dispose();
		super.dispose();
	}

	private void gameOver() {
		isOver = true;
		isResumed = false;
	}

	private  void drawObjects() {

		batch.begin();
		batch.draw(bucketImage, bucket.x, bucket.y);
		for (Rectangle raindrop: rainDrops) {
			batch.draw(dropImage, raindrop.x, raindrop.y);
		}
		batch.end();

	}


	@Override
	public void pause() {
		rainMusic.pause();
		//isPaused = true;
		isResumed = false;
	}

	@Override
	public void resume() {
		if (Gdx.input.isTouched()) {
			rainMusic.play();
			//isPaused = false;
			isResumed = true;
		}
	}

}