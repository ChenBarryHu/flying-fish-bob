package com.shichen.android.helicopter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.shichen.android.helicopter.GameCharacter.Explosion;
import com.shichen.android.helicopter.GameCharacter.Fish;
import com.shichen.android.helicopter.GameCharacter.MonsterCommander;
import com.shichen.android.helicopter.GameCharacter.PuffCommander;

/**
 * Created by hsctn on 2016-06-23.
 */
// SurfaceHolder.Callback interface has three abstract void methed:
//  surfaceChanged, surfaceCreated, surfaceDestroyed
public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {

    // ********************* variable part******************************//
    // these are constant for background
    public static final int WIDTH = 1280;   // these are the actual width and
    public static final int HEIGHT = 640;   // height of the background picture
    public static final int MOVESPEED = -5; // this is the distance background move left
                                            // every time we update



    // these are the objects we are gonna manipulate to run the game
    private GameLoopEngine thread;          // we will need a thread to run our engine
    private Background bg;                  // this is a background object,provide bitmap, position, update method and draw  method for app background
    private Fish fish;                      // we will create our player character which is a cute little fish!!!
    private PuffCommander puffCommander;
    private MonsterCommander monsterCommander;
    private Explosion explosion;




    // these are the sentinals we use to control the flow of the game
    private boolean newGameWellPrepared = true;
    private boolean resetModeStart = false;
    private boolean fishNeedDisappear = false;
    public  static boolean explosionStart =false;
    public  static boolean explosionFinish = false;
    private boolean justOpened = true;



    // bestScore record the score of the game, it will also be used to change the easyness of the game
    private long bestScore = 0;



    // this variable is used to give a interval when the little fish die
    private long startResetTime =0;




    //*********************************************************************************//


    public GamePanel(Context context) {
        super(context);
        getHolder().addCallback(this);      // add the callback to the surfaceholder
                                            // the code above means that the surfaceview hold its holder and
                                            // we need the callbacks(surfaceChanged, surfaceCreated, surfaceDestroyed)
        setFocusable(true);      // make gamePanel focusable so it can handle events
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {   // This is called immediately after the surface is first created.

        explosionStart =false;
        explosionFinish = false;
        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.gb));// initialize the background object, give it the image resource which is a bitmap
        fish = new Fish(BitmapFactory.decodeResource(getResources(), R.drawable.swimfish), 64, 64, 6);
        puffCommander = new PuffCommander(fish);
        monsterCommander = new MonsterCommander(getContext(), fish);
        explosion = new Explosion(BitmapFactory.decodeResource(getResources(),R.drawable.explosion));
        thread = new GameLoopEngine(getHolder(), this);
        thread.setIfRunning(true);
        thread.start();

    }


    // This is called immediately after any structural
    // changes (format or size) have been made to the surface.
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    // This is called immediately before a surface is being destroyed.
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        int counter = 0;
        while (counter < 1000) {
            counter++;
            try {
                thread.setIfRunning(false);
                thread.join(); // wait until that thread finish
                thread = null;   //  set it to null, so that the garbage collector can collect it
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override                                       // this is the central control part of the game
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!fish.getIfcurrentlyplaying()&&newGameWellPrepared) {
                resetModeStart = false;
                fish.setIfcurrentlyplaying(true);
                fish.setIfScreenPressed(true);
                monsterCommander.initializeBats();
                newGameWellPrepared = false;
            } else if(fish.getIfcurrentlyplaying()){
                fish.setIfScreenPressed(true);
            }
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            fish.setIfScreenPressed(false);
            return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void draw(Canvas canvas) {
        final float scaleFactorX = (float) getWidth() / (WIDTH * 1.f);     // getWidth(), getHeight() gets
        final float scaleFactorY = (float) getHeight() / (HEIGHT * 1.f); // the screenwidth and screenheight
        super.draw(canvas);
        if (canvas != null) {
            final int savedState = canvas.save();      // I don't know what does it means
            canvas.scale(scaleFactorX, scaleFactorY);  // scale the  canvas
            bg.draw(canvas);                           // draw the background and fish!!!!
            if(!fishNeedDisappear) {
                fish.draw(canvas);
            }
            puffCommander.draw(canvas);
            monsterCommander.draw(canvas);
            Log.e("explosion", "explosionStart "+explosionStart+"explosionFinish"+explosionFinish);
            if(explosionStart&& (!explosionFinish))
            {
                explosion.draw(canvas);
            }
            drawText(canvas);
            canvas.restoreToCount(savedState);         // I don't know what does it means
        }

    }


    // this function is called in Mainthread ruuning
    // loop to update the background and fish
    public void update() {

        if (fish.getIfcurrentlyplaying()) {
            bg.update();
            fish.update();
            puffCommander.update();
            monsterCommander.update();


            //Log.e("From game panel:","The pos_y of fish is "+ fish.pos_y);
        }else{
            if(!resetModeStart)
            {
                resetModeStart = true;
                newGameWellPrepared = false;
                explosionStart = true;
                explosionFinish = false;
                startResetTime = System.nanoTime();
                fishNeedDisappear = true;
                explosion.setX(fish.getPos_x()-60);
                explosion.setY(fish.getPos_y()-70);

            }
            if(!newGameWellPrepared) {
                    explosion.update();

                if (justOpened) {
                    explosionStart = false;
                    explosionFinish = true;
                    newGame();
                    return;
                }
                long resetElapsed = (System.nanoTime() - startResetTime) / 1000000;
                if (resetElapsed > 2500 && explosionFinish) {
                    explosionStart = false;
                    newGame();
                }
            }
        }
    }






    public void drawText(Canvas canvas)
    {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Score: " + fish.getScore(), 10, HEIGHT - 60, paint);
        canvas.drawText("BEST: " + bestScore, WIDTH - 215, HEIGHT - 60, paint);

        if(!fish.getIfcurrentlyplaying()&&newGameWellPrepared)
        {
            Paint paint1 = new Paint();
            paint1.setTextSize(40);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("PRESS TO START", WIDTH/2-50, HEIGHT/2-130, paint1);

            paint1.setTextSize(20);
            canvas.drawText("PRESS AND HOLD TO GO UP", WIDTH/2-50, HEIGHT/2 -110, paint1);
            canvas.drawText("RELEASE TO GO DOWN", WIDTH/2-50, HEIGHT/2 -90, paint1);
        }
    }


    public void newGame()
    {
        // clear the puff, monster and bat from last round
        monsterCommander.botBats.clear();
        monsterCommander.topBats.clear();
        monsterCommander.monsters.clear();
        puffCommander.puff.clear();


        // reset the status of monstercommander, fish, and puffCommander
        monsterCommander.setIfMonsterOccurInThisRound(false);
        fish.setFirstUpdate(true);
        puffCommander.setIfFirstPuffInThisRound(true);
        explosion.resetExplosion();
        if(!justOpened) {
            if (fish.getScore() > bestScore) {
                bestScore = fish.getScore();
            }
        }
        fish.resetScore();
        fish.setVelocity_y(0);
        fish.setPos_y(HEIGHT/2);
        fishNeedDisappear = false;
        newGameWellPrepared = true;
        if(justOpened) {
            justOpened = false;
        }
    }
}
