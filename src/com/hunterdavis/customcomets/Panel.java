package com.hunterdavis.customcomets;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.hunterdavis.customcomets.Panel.Bullet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

class Panel extends SurfaceView implements SurfaceHolder.Callback {
	// member variables
	private CanvasThread canvasthread;
	public Boolean surfaceCreated;
	public Context mContext;
	int difficulty = 0;
	public Boolean gameOver = false;
	public Boolean gamePaused = false;
	public Boolean introScreenOver = false;
	Point player1Pos = new Point(25, 25);
	Point player1Wants = new Point(0, 0);
	public Bitmap cometBitmap = null;
	public Bitmap cometBitmapLarge = null;
	public Bitmap cometBitmapSmall = null;
	public Bitmap player1Bitmap = null;
	public Uri selectedImageUri = null;
	public Uri selectedShipUri = null;
	int mwidth = 0;
	int mheight = 0;
	int player1Score = 0;
	int cometSize = 0;
	int player1Lives = 0;
	Random myrandom = new Random();
	List<Comet> cometList;
	List<Bullet> bulletList;

	// tweaking for game mechanics
	int player1Step = 2;
	int bulletStep = 10;
	int player1Size = 5;
	int playerColor = Color.rgb(204, 8, 57);
	int scoreColor = Color.rgb(0, 0, 234);
	int playerCenterColor = Color.BLACK;
	int bulletColor = Color.RED;
	int scoreybuffer = 20;
	int scorexbuffer = 100;
	int player1LivesStarting = 3;
	int maxBulletSize = 25;
	int initialNumberOfComets = 3;
	int numCracks = 3;
	Boolean shootReverse = false;
	private static final float EPS = (float) 0.000001;

	public class Comet {
		public int x;
		public int y;
		public int size;
		public int healthPoints;
		public Boolean left;
		public Boolean down;

		Comet(int xa, int ya, int sizea, int healtha, Boolean lefta,
				Boolean downa) {
			x = xa;
			y = ya;
			size = sizea;
			healthPoints = healtha;
			left = lefta;
			down = downa;
		}

		Comet(int xa, int ya) {
			x = xa;
			y = ya;
			size = myrandom.nextInt(3);
			healthPoints = (int) Math.pow(2, size);
			left = myrandom.nextBoolean();
			down = myrandom.nextBoolean();

		}

		Comet(Comet newComet) {
			x = newComet.x;
			y = newComet.y;
			size = newComet.size;
			healthPoints = newComet.healthPoints;
			left = newComet.left;
			down = newComet.down;
		}
	}

	public class Bullet {
		public int x;
		public int y;
		public int xdest;
		public int ydest;

		Bullet(int xa, int ya, int xdesta, int ydesta) {
			x = xa;
			y = ya;
			xdest = xdesta;
			ydest = ydesta;
		}

		Bullet(Bullet tempBull) {
			x = tempBull.x;
			y = tempBull.y;
			xdest = tempBull.xdest;
			ydest = tempBull.ydest;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		synchronized (getHolder()) {

			int action = event.getAction();
			if (action == MotionEvent.ACTION_DOWN) {

				player1Wants.x = (int) event.getX();
				player1Wants.y = (int) event.getY();

				if (gamePaused == true) {
					gamePaused = false;
				}

				addBullet();

				return true;
			} else if (action == MotionEvent.ACTION_MOVE) {

				player1Wants.x = (int) event.getX();
				player1Wants.y = (int) event.getY();
				addBullet();

				return true;
			} else if (action == MotionEvent.ACTION_UP) {

				return true;
			}
			return true;
		}
	}

	public void setDifficulty(int difficult) {
		difficulty = difficult;
		initialNumberOfComets = (difficulty + 2) * 2;
		maxBulletSize = 25 + (5 * difficult);
		reset();
	}

	public void setUri(Uri uri) {
		selectedImageUri = uri;
		cometBitmap = null;
	}

	public void setShipUri(Uri uri) {
		selectedShipUri = uri;
		cometBitmap = null;
	}

	public void setShootReverse(Boolean shot) {
		shootReverse = shot;
	}

	public void reset() {
		// reset everything
		gameOver = false;
		gamePaused = true;
		introScreenOver = false;
		player1Score = 0;
		player1Lives = player1LivesStarting;

		player1Pos.x = mwidth / 2;
		player1Pos.y = mheight / 2;

		// clear lists
		cometList = new ArrayList();
		bulletList = new ArrayList();

		cometBitmap = null;
		cometBitmapLarge = null;
		cometBitmapSmall = null;
		player1Bitmap = null;
	}

	float fdistance(float x1, float y1, float x2, float y2) {
		return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	}

	public Panel(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		//
		surfaceCreated = false;

		reset();

		getHolder().addCallback(this);
		setFocusable(true);
	}

	public void createThread(SurfaceHolder holder) {
		canvasthread = new CanvasThread(getHolder(), this, mContext,
				new Handler());
		canvasthread.setRunning(true);
		canvasthread.start();
	}

	public void terminateThread() {
		canvasthread.setRunning(false);
		try {
			canvasthread.join();
		} catch (InterruptedException e) {

		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		reset();

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		//
		if (surfaceCreated == false) {
			createThread(holder);
			// Bitmap kangoo = BitmapFactory.decodeResource(getResources(),
			// R.drawable.kangoo);
			surfaceCreated = true;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		surfaceCreated = false;

	}

	public void addBullet() {

		if (bulletList.size() > maxBulletSize) {
			return;
		}

		// find player position and direction orientation
		int x = player1Wants.x;
		int y = player1Wants.y;

		int deltax = player1Wants.x - player1Pos.x;
		int deltay = player1Wants.y - player1Pos.y;

		if (deltax == 0) {
			deltax = 1;
		}
		if (deltay == 0) {
			deltay = 1;
		}

		Boolean bothInBounds = true;
		Boolean xOutOfBounds = false;
		Boolean yOutOfBounds = false;
		while (bothInBounds == true) {
			if (shootReverse == true) {
				x -= deltax;
				y -= deltay;
			} else {
				x += deltax;
				y += deltay;

			}
			xOutOfBounds = testXBounds(x);
			yOutOfBounds = testYBounds(y);
			if ((xOutOfBounds == true) && (yOutOfBounds == true)) {
				bothInBounds = false;
			}
		}

		// add a bullet to bullet list with movement direction vector
		Bullet ourBullet = new Bullet(player1Pos.x, player1Pos.y, x, y);
		bulletList.add(ourBullet);

	}

	Boolean testXBounds(int x) {
		if (x < 0) {
			return true;
		}
		if (x > mwidth) {
			return true;
		}
		return false;
	}

	Boolean testYBounds(int y) {
		if (y < 0) {
			return true;
		}
		if (y > mheight) {
			return true;
		}
		return false;
	}

	public void movePlayer1Tick() {
		if (introScreenOver == false) {
			player1Pos.x = mwidth / 2;
			player1Pos.y = mheight / 2;
			introScreenOver = true;
		}

		if (player1Wants.x > (player1Pos.x + (player1Size / 2))) {
			if ((player1Wants.x - player1Pos.x) > player1Step) {
				player1Pos.x += player1Step;
			}
		} else if (player1Wants.x < (player1Pos.x)) {
			if (player1Pos.x > 0) {
				if ((player1Pos.x - player1Wants.x) >= player1Step) {
					player1Pos.x -= player1Step;
				} else {
					player1Pos.x -= (player1Pos.x - player1Wants.x);
				}
			}
		}

		// now for a mirror Y tick
		if (player1Wants.y > (player1Pos.y + (player1Size / 2))) {
			if ((player1Wants.y - player1Pos.y) > player1Step) {
				player1Pos.y += player1Step;
			}
		} else if (player1Wants.y < (player1Pos.y)) {
			if (player1Pos.y > 0) {
				if ((player1Pos.y - player1Wants.y) >= player1Step) {
					player1Pos.y -= player1Step;
				} else {
					player1Pos.y -= (player1Pos.y - player1Wants.y);
				}
			}
		}

	}

	public void moveBulletsTick() {
		int numBullets = bulletList.size();

		if (numBullets < 1) {
			return;
		}
		// if we reverse iterate over list we can remove items without worry
		// because we'll be removing from end down
		for (int i = numBullets - 1; i > 0; i--) {
			// increment and re-set each bullet after movement
			if (bulletList.size() > i) {

				Bullet ourBullet = incrementBulletOnLine((Bullet) bulletList
						.get(i));
				bulletList.set(i, ourBullet);
				// if we've already done a canvas draw
				if (mwidth > 0) {
					if ((ourBullet.x >= mwidth) || (ourBullet.x < 0)
							|| (ourBullet.y >= mheight) || (ourBullet.y < 0)) {
						bulletList.remove(i);
					}
					if ((ourBullet.x == ourBullet.xdest)
							&& (ourBullet.y == ourBullet.ydest)) {
						bulletList.remove(i);
					}
				}
			}

		}

	}

	public void moveCometsTick() {
		for (int i = 0; i < cometList.size(); i++) {
			Comet comet = cometList.get(i);

			// we won't move the ball the first tick so we have the correct
			// screen
			// width and hieght
			if ((mwidth == mheight) && (mwidth == 0)) {
				return;
			}

			int speed = 1;

			// bounce off left wall or move left
			if (comet.left == true) {
				if (comet.x < speed) {
					comet.left = false;
				} else {
					comet.x -= speed;
				}
			}

			// bounce off right wall or move right
			if (comet.left == false) {
				if (comet.x > (mwidth - speed)) {
					comet.left = true;
				} else {
					comet.x += speed;
				}
			}

			// move up or down the field
			// bounce off paddles or walls
			if (comet.down == true) {
				// if we hit player 2's paddle at bottom of screen (bottom = top
				// in
				// graphics)

				// if we hit the bottom of the screen
				if (comet.y < (speed)) {
					comet.down = false;

				} else {
					comet.y -= speed;
				}
			}

			if (comet.down == false) {
				if (comet.y > (mheight - speed)) {
					comet.down = true;
				} else {
					comet.y += speed;
				}
			}
		}
	}

	public void testForCollisionAndProcess() {

		// loop through all comets
		Rect playerRect = new Rect();
		playerRect.left = player1Pos.x - player1Size / 2;
		playerRect.right = player1Pos.x + player1Size / 2;
		playerRect.top = player1Pos.y + player1Size / 2;
		playerRect.bottom = player1Pos.y - player1Size / 2;

		for (int i = 0; i < cometList.size(); i++) {

			Comet comet = cometList.get(i);

			int localCometSize = cometSize;
			if (comet.size == 0) {
				localCometSize = cometSize / 2;
			} else if (comet.size == 2) {
				localCometSize = cometSize * 2;
			}

			Rect cometRect = new Rect();
			cometRect.left = comet.x - localCometSize / 2;
			cometRect.right = comet.x + localCometSize / 2;
			cometRect.top = comet.y + localCometSize / 2;
			cometRect.bottom = comet.y - localCometSize / 2;

			// test if player hit a comet
			boolean collision = doTheyOverlap(cometRect, playerRect);
			// CollisionTest(playerRect,cometRect);
			if (collision != false) {
				decrementLives();
				player1Score -= 1000;
				return;
			}

			Boolean changedComet = false;
			for (int j = bulletList.size() - 1; j > 0; j--) {
				Bullet localBullet = bulletList.get(j);

				if ((localBullet.x <= cometRect.right)
						&& (localBullet.x >= cometRect.left)) {

					if ((localBullet.y >= cometRect.bottom)
							&& (localBullet.y <= cometRect.top)) {
						// we hit this comet with a bullet
						comet.healthPoints--;
						player1Score += 50;
						changedComet = true;
						bulletList.remove(j);
					}

				}

			}
			if (changedComet) {
				cometList.set(i, comet);
			}

		}
	}

	public boolean betweenOrOn(int a, int b, int c) {
		if (a >= b) {
			if (a <= c) {
				return true;
			}
		}
		return false;
	}

	public boolean doTheyOverlap(Rect one, Rect two) {

		// left side of one is in two
		if (betweenOrOn(one.left, two.left, two.right)) {
			// top side of one is in two
			if (betweenOrOn(one.top, two.bottom, two.top)) {
				return true;
			}

			// bottom side of one is in two
			if (betweenOrOn(one.bottom, two.bottom, two.top)) {
				return true;
			}

			// one is bigger and contains two
			if (betweenOrOn(two.bottom, one.bottom, one.top)
					&& betweenOrOn(two.top, one.bottom, one.top)) {
				return true;
			}

		}
		// right side of one is in two
		// left side of one is in two
		if (betweenOrOn(one.right, two.left, two.right)) {
			// top side of one is in two
			if (betweenOrOn(one.top, two.bottom, two.top)) {
				return true;
			}

			// bottom side of one is in two
			if (betweenOrOn(one.bottom, two.bottom, two.top)) {
				return true;
			}
			// one is bigger and contains two
			if (betweenOrOn(two.bottom, one.bottom, one.top)
					&& betweenOrOn(two.top, one.bottom, one.top)) {
				return true;
			}
		}

		// one is bigger and contains two
		if (betweenOrOn(two.left, one.left, one.right)
				&& betweenOrOn(two.right, one.left, one.right)) {
			// top side of one is in two
			if (betweenOrOn(one.top, two.bottom, two.top)) {
				return true;
			}

			// bottom side of one is in two
			if (betweenOrOn(one.bottom, two.bottom, two.top)) {
				return true;
			}
			// one is bigger and contains two
			if (betweenOrOn(two.bottom, one.bottom, one.top)
					&& betweenOrOn(two.top, one.bottom, one.top)) {
				return true;
			}
		}

		return false;
	}

	public void decrementLives() {
		gamePaused = true;
		player1Lives--;
		if (player1Lives < 1) {
			gameOver = true;
			gamePaused = false;
		}

		// move the player to the middle
		player1Pos.x = mwidth / 2;
		player1Pos.y = mheight / 2;

		// move all comets to the edge
		// loop through all comets
		for (int i = 0; i < cometList.size(); i++) {

			Comet comet = cometList.get(i);
			if (comet.left == true) {
				if (comet.down == true) {
					comet.x = 0;
				} else {
					comet.x = mwidth;
				}
			} else {
				if (comet.down == true) {
					comet.y = 0;
				} else {
					comet.y = mheight;
				}
			}

			cometList.set(i, comet);

		}

	}

	public void updateGameState() {

		if (gameOver == true) {
			return;
		}

		if (gamePaused == true) {
			return;
		}

		// make sure there's a graphics init round
		if (mwidth == 0) {
			return;
		}

		// update the score a point for being alive
		player1Score++;

		// make sure there are enough comets onscreen
		int cometDiff = initialNumberOfComets - cometList.size();
		if (cometDiff > 0) {
			for (int i = 0; i < cometDiff; i++) {
				generateComet();
			}
		}

		// move player 1 a tick
		movePlayer1Tick();

		// move all asteroids a tick
		moveCometsTick();

		// move bulletts a tick
		for (int i = 0; i < bulletStep; i++) {
			moveBulletsTick();
		}

		// test for bullet or player collision
		testForCollisionAndProcess();

	}

	public void generateComet() {

		int randomwElement = myrandom.nextInt(mwidth);
		int randomhElement = myrandom.nextInt(mheight);
		int whichElement = myrandom.nextInt(3);
		int x = 0;
		int y = 0;

		switch (whichElement) {
		case 0:
			// left edge
			y = randomhElement;
			break;
		case 1:
			// right edge
			y = randomhElement;
			x = mwidth;
			break;
		case 2:
			// top edge
			x = randomwElement;
			break;
		case 3:
			// bottom edge
			x = randomwElement;
			y = mheight;
			break;
		default:
			break;
		}

		//
		Comet myComet = new Comet(x, y);
		cometList.add(myComet);

	}

	@Override
	public void onDraw(Canvas canvas) {

		mwidth = canvas.getWidth();
		mheight = canvas.getHeight();

		Paint paint = new Paint();

		// our player sizes should be a function both of difficulty and of
		// screen size
		int visualDivisor = mwidth;
		if (mheight < mwidth) {
			visualDivisor = mheight;
		}
		player1Size = visualDivisor / 8;
		cometSize = visualDivisor / 12;
		int cometSizeSmall = cometSize / 2;
		int cometSizeLarge = cometSize * 2;

		// draw player 1
		if (introScreenOver == true) {
			if (player1Bitmap == null) {
				// cometSize = mwidth / 5;
				// if we can't load somebody else's bitmap
				if (selectedShipUri == null) {
					Bitmap _scratch = BitmapFactory.decodeResource(
							getResources(), R.drawable.trollface);

					if (_scratch == null) {
						Toast.makeText(getContext(), "WTF", Toast.LENGTH_SHORT)
								.show();
					}

					// now scale the bitmap using the scale value
					player1Bitmap = Bitmap.createScaledBitmap(_scratch,
							player1Size, player1Size, false);
				} else {
					// THIS IS WHERE YOU LOAD FILE URIS AT
					InputStream photoStream = null;

					Context context = getContext();
					try {
						photoStream = context.getContentResolver()
								.openInputStream(selectedShipUri);
					} catch (FileNotFoundException e) {
						//
						e.printStackTrace();
					}
					int scaleSize = decodeFile(photoStream, player1Size,
							player1Size);

					try {
						photoStream = context.getContentResolver()
								.openInputStream(selectedShipUri);
					} catch (FileNotFoundException e) {
						//
						e.printStackTrace();
					}
					BitmapFactory.Options o = new BitmapFactory.Options();
					o.inSampleSize = scaleSize;

					Bitmap photoBitmap = BitmapFactory.decodeStream(
							photoStream, null, o);
					player1Bitmap = Bitmap.createScaledBitmap(photoBitmap,
							cometSize, cometSize, true);
					photoBitmap.recycle();

				}

			}
			canvas.drawBitmap(player1Bitmap, player1Pos.x - (player1Size / 2),
					player1Pos.y - (player1Size / 2), paint);

		}// introscreenover = true
			// draw bullets
		paint.setColor(bulletColor);
		int numBullets = bulletList.size();
		for (int i = numBullets - 1; i > 0; i--) {
			if (bulletList.size() > i) {
				Bullet tempBullet = bulletList.get(i);
				canvas.drawCircle(tempBullet.x, tempBullet.y, 1, paint);
			}
		}

		// draw comets
		if (cometBitmap == null) {
			// cometSize = mwidth / 5;
			// if we can't load somebody else's bitmap
			if (selectedImageUri == null) {
				Bitmap _scratch = BitmapFactory.decodeResource(getResources(),
						R.drawable.megusta);

				if (_scratch == null) {
					Toast.makeText(getContext(), "WTF", Toast.LENGTH_SHORT)
							.show();
				}

				// now scale the bitmap using the scale value
				cometBitmap = Bitmap.createScaledBitmap(_scratch, cometSize,
						cometSize, false);
				cometBitmapLarge = Bitmap.createScaledBitmap(_scratch,
						cometSizeLarge, cometSizeLarge, false);
				cometBitmapSmall = Bitmap.createScaledBitmap(_scratch,
						cometSizeSmall, cometSizeSmall, false);
			} else {
				//
				// THIS IS WHERE YOU LOAD FILE URIS AT
				InputStream photoStream = null;

				Context context = getContext();
				try {
					photoStream = context.getContentResolver().openInputStream(
							selectedImageUri);
				} catch (FileNotFoundException e) {
					//
					e.printStackTrace();
				}
				int scaleSize = decodeFile(photoStream, cometSize, cometSize);

				try {
					photoStream = context.getContentResolver().openInputStream(
							selectedImageUri);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				BitmapFactory.Options o = new BitmapFactory.Options();
				o.inSampleSize = scaleSize;

				Bitmap photoBitmap = BitmapFactory.decodeStream(photoStream,
						null, o);
				cometBitmap = Bitmap.createScaledBitmap(photoBitmap, cometSize,
						cometSize, true);
				cometBitmapLarge = Bitmap.createScaledBitmap(photoBitmap,
						cometSizeLarge, cometSizeLarge, false);
				cometBitmapSmall = Bitmap.createScaledBitmap(photoBitmap,
						cometSizeSmall, cometSizeSmall, false);
				photoBitmap.recycle();

			}

		}

		// draw the comet bitmaps all over
		// for each comet
		for (int i = cometList.size(); i > 0; i--) {
			int cometListSize = cometList.size();
			if (cometListSize > i) {

				Comet myComet = cometList.get(i);

				switch (myComet.size) {
				case 0:
					if (myComet.healthPoints < 1) {
						drawExplosion(canvas, myComet.x - (cometSizeSmall / 2),
								myComet.x + (cometSizeSmall / 2), myComet.y
										+ (cometSizeSmall / 2), myComet.y
										- (cometSizeSmall / 2));
						cometList.remove(i);
					} else {
						canvas.drawBitmap(cometBitmapSmall, myComet.x
								- (cometSizeSmall / 2), myComet.y
								- (cometSizeSmall / 2), paint);
					}
					break;
				case 1:
					if (myComet.healthPoints < 1) {
						drawExplosion(canvas, myComet.x - (cometSize / 2),
								myComet.x + (cometSize / 2), myComet.y
										+ (cometSize / 2), myComet.y
										- (cometSize / 2));
						cometList.remove(i);

					} else if (myComet.healthPoints < 2) {
						canvas.drawBitmap(cometBitmap, myComet.x
								- (cometSize / 2), myComet.y - (cometSize / 2),
								paint);
						drawCracks(canvas, myComet.x - (cometSize / 2),
								myComet.x + (cometSize / 2), myComet.y
										+ (cometSize / 2), myComet.y
										- (cometSize / 2));
					} else {
						canvas.drawBitmap(cometBitmap, myComet.x
								- (cometSize / 2), myComet.y - (cometSize / 2),
								paint);
					}
					break;
				case 2:
					if (myComet.healthPoints < 1) {
						drawExplosion(canvas, myComet.x - (cometSizeLarge / 2),
								myComet.x + (cometSizeLarge / 2), myComet.y
										+ (cometSizeLarge / 2), myComet.y
										- (cometSizeLarge / 2));
						cometList.remove(i);
						cometList.add(new Comet(myComet.x, myComet.y, 1, 2,
								false, false));
						cometList.add(new Comet(myComet.x, myComet.y, 1, 2,
								true, true));
						cometList.add(new Comet(myComet.x, myComet.y, 0, 1,
								true, false));
						cometList.add(new Comet(myComet.x, myComet.y, 0, 1,
								false, true));
					} else if (myComet.healthPoints < 4) {
						canvas.drawBitmap(cometBitmapLarge, myComet.x
								- (cometSizeLarge / 2), myComet.y
								- (cometSizeLarge / 2), paint);
						drawCracks(canvas, myComet.x - (cometSizeLarge / 2),
								myComet.x + (cometSizeLarge / 2), myComet.y
										+ (cometSizeLarge / 2), myComet.y
										- (cometSizeLarge / 2));
					} else {

						canvas.drawBitmap(cometBitmapLarge, myComet.x
								- (cometSizeLarge / 2), myComet.y
								- (cometSizeLarge / 2), paint);
					}

					break;
				default:
					Toast.makeText(getContext(), "Error in Comet Rendering",
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}
		// - draw comet in its position
		// - draw comet with its level of asplosion
		// - after drawing a comet that's fully asploded
		// - - remove it from the list
		// - - - generate a new comet
		// generateComet();

		// draw score 1
		paint.setColor(scoreColor);
		// draw score 2
		canvas.drawText(
				String.valueOf(player1Lives) + " lives, "
						+ String.valueOf(player1Score) + " points", mwidth
						- scorexbuffer, 9, paint);

		// draw game over if game over
		if (gameOver == true) {

			paint.setTextSize(20);
			canvas.drawText("Game Over", (mwidth / 2) - 50, mheight / 4, paint);
		}

		if (gamePaused == true) {
			paint.setTextSize(25);
			canvas.drawText("Touch To Continue", (mwidth / 2) - 110,
					mheight / 4, paint);
		}

	}

	private void drawCracks(Canvas canvas, int left, int right, int top,
			int bottom) {
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(1);
		int xa, xb, ya, yb;
		int width = right - left;
		int height = top - bottom;
		for (int i = 0; i < numCracks; i++) {
			xa = myrandom.nextInt(width) + left;
			xb = myrandom.nextInt(width) + left;
			ya = myrandom.nextInt(height) + bottom;
			yb = myrandom.nextInt(height) + bottom;
			canvas.drawLine(xa, ya, xb, yb, paint);
		}
	}

	private void drawExplosion(Canvas canvas, int left, int right, int top,
			int bottom) {
		int x = (left + right) / 2;
		int y = (top + bottom) / 2;
		int radius = (right - left) / 2;

		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(1);
		canvas.drawCircle(x, y, radius, paint);

		canvas.drawLine(x - radius / 2, y + radius / 2, x + radius / 2, y
				+ radius / 2, paint);

		int eyestop = y - radius / 4;
		int eyesbottom = y - radius / 2;
		int lefteyeleft = x - radius / 2;
		int lefteyeright = x - radius / 4;
		int righteyeleft = x + radius / 4;
		int righteyeright = x + radius / 2;

		// left eye x
		canvas.drawLine(lefteyeleft, eyestop, lefteyeright, eyesbottom, paint);
		canvas.drawLine(lefteyeright, eyestop, lefteyeleft, eyesbottom, paint);

		// right eye x
		canvas.drawLine(righteyeleft, eyestop, righteyeright, eyesbottom, paint);
		canvas.drawLine(righteyeright, eyestop, righteyeleft, eyesbottom, paint);

	}

	// decodes image and scales it to reduce memory consumption
	private int decodeFile(InputStream photostream, int h, int w) {
		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(photostream, null, o);

		// Find the correct scale value. It should be the power of 2.
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp / 2 < w || height_tmp / 2 < h)
				break;
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}

		return scale;
	}

	// we manually calculate all pixels in the short lines
	// so that we can test the backing bitmap for
	// the presense of non-background pixels

	public Bullet incrementBulletOnLine(Bullet mybullet) {
		int x0 = mybullet.x;
		int y0 = mybullet.y;
		int x1 = mybullet.xdest;
		int y1 = mybullet.ydest;
		int dx = Math.abs(x1 - x0);
		int dy = Math.abs(y1 - y0);
		int sx = -1;
		if (x0 < x1) {
			sx = 1;
		}
		int sy = -1;
		if (y0 < y1) {
			sy = 1;
		}
		;
		int err = dx - dy;
		Bullet returnBullet = new Bullet(mybullet);

		Boolean running = true;
		while (running) {
			// iterate till finished
			if ((x0 == x1) && (y0 == y1)) {
				running = false;
			} else {
				int e2 = 2 * err;
				if (e2 > -dy) {
					err = err - dy;
					x0 = x0 + sx;
					running = false;
				}
				if (e2 < dx) {
					err = err + dx;
					y0 = y0 + sy;
					running = false;
				}
			}

		}
		returnBullet.x = x0;
		returnBullet.y = y0;
		return returnBullet;

	}// end line draw

} // end class