package com.hunterdavis.customcomets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.os.Looper;
import android.view.SurfaceHolder;

public class CanvasThread extends Thread {
	private SurfaceHolder _surfaceHolder;
	private Panel _panel;
	private boolean _run = false;
	private Handler mHandler;
    private Context mContext;
    

	// for consistent rendering
	private long sleepTime;
	// amount of time to sleep for (in milliseconds)
	private long delay = 70;


	public CanvasThread(SurfaceHolder surfaceHolder, Panel panel, Context context, Handler handler) {
		_surfaceHolder = surfaceHolder;
		_panel = panel;
		this.mHandler = handler;
		this.mContext = context;

	}

	public void setRunning(boolean run) {
		_run = run;
	}

	public boolean getRunning() {
		return _run;
	}

	@Override
	public void run() {

		// UPDATE
		while (_run) {
			// time before update
			long beforeTime = System.nanoTime();
			// This is where we update the game engine
			_panel.updateGameState();

			// DRAW
			Canvas c = null;
			try {
				// lock canvas so nothing else can use it
				c = _surfaceHolder.lockCanvas(null);
				synchronized (_surfaceHolder) {
					Paint paint = new Paint();
					paint.setColor(Color.BLACK);
					// clear the screen with the black painter.
					c.drawRect(0, 0, c.getWidth(), c.getHeight(),paint);
					/*
					// show us a nice divider
					paint.setColor(Color.BLUE);
					c.drawLine(0, (c.getHeight()/2+c.getHeight()/5), c.getWidth(), (c.getHeight()/2+c.getHeight()/5), paint);
					c.drawLine(0, (c.getHeight()/2-c.getHeight()/5), c.getWidth(), (c.getHeight()/2-c.getHeight()/5), paint);
					
					// draw a circle border
					Paint borderPaint = new Paint();
			        borderPaint.setColor(Color.BLUE);
			        borderPaint.setAntiAlias(true);
			        borderPaint.setStyle(Style.STROKE);
			        borderPaint.setStrokeWidth(1);
					c.drawCircle(c.getWidth()/2, c.getHeight()/2, c.getHeight()/10, borderPaint);
					
					
					
					paint.setColor(Color.RED);
					paint.setPathEffect( new DashPathEffect(new float[] { 15, 5, 8, 5 }, 
							0) ); 
					c.drawLine(0, c.getHeight()/2, ((c.getWidth()/2)-c.getHeight()/10), c.getHeight()/2, paint);
					c.drawLine(((c.getWidth()/2)+c.getHeight()/10), c.getHeight()/2, c.getWidth(), c.getHeight()/2, paint);
					//paint.setColor(Color.BLUE);
					//c.drawLine(0, c.getHeight()/20, c.getWidth(), c.getHeight()/20, paint);
					//c.drawLine(0, c.getHeight() - c.getHeight()/20, c.getWidth(), c.getHeight() - c.getHeight()/20, paint);
					
					
					paint = new Paint();
					paint.setColor(Color.RED);
					c.drawCircle(c.getWidth()/2, c.getHeight()/2, 2, paint);
					*/
					// This is where we draw the game engine.
					_panel.onDraw(c);
				}
			} finally {
				// do this in a finally so that if an exception is thrown
				// during the above, we don't leave the Surface in an
				// inconsistent state
				if (c != null) {
					_surfaceHolder.unlockCanvasAndPost(c);
				}
			}

			// SLEEP
			// Sleep time. Time required to sleep to keep game consistent
			// This starts with the specified delay time (in milliseconds) then
			// subtracts from that the
			// actual time it took to update and render the game. This allows
			// our game to render smoothly.
			this.sleepTime = delay
					- ((System.nanoTime() - beforeTime) / 1000000L);

			try {
				// actual sleep code
				if (sleepTime > 0) {
					CanvasThread.sleep(sleepTime);
				}
			} catch (InterruptedException ex) {
				
			}
		}

	}

}