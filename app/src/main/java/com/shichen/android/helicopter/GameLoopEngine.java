package com.shichen.android.helicopter;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * Created by hsctn on 2016-06-23.
 */


public class GameLoopEngine extends Thread {
    private int FPS = 40;                      // how many frame we want per second
    private double framecap = 1.0 / FPS;       // this is the frequency of refresh the screen

    private SurfaceHolder surfaceHolder;       // surfaceholder and gamePanel object
    private GamePanel gamePanel;               //    use surfaceholder to get canvas, use gamePanel to
                                               //    control game

    private boolean ifRunning;       //used to control the  status of the game by setting the value of it true or false

    public static Canvas canvas;   // we use Canvas to draw something


    public GameLoopEngine(SurfaceHolder surfaceHolder, GamePanel gamePanel) { // constructor: we need a
                                                     // surfaceHolder and gamePanel to construct this object


        super();
        this.surfaceHolder = surfaceHolder;
        this.gamePanel = gamePanel;
    }


    @Override
    public void run() {              // because Mainthread extends thread, so should override run() method
        ifRunning = true;

        double thisTime = System.nanoTime() / 1000000000.0f;  // this is for first loop
        double lastTime;                                      // thisTime and lastTime are uesd to calculate passedTime
        double passedTime;                                    // this is the running time for each game Engine loop
        double unprocessedTime = 0;                           // this is used to determine when to update


                          // the following three variables are used for debugging
                          // they are used for printing frequency of while loop and update
        double fpsTimeCumulator = 0;
        int fpsCountCumulator = 0;
        long whileloopcount = 0;


        while (ifRunning) {   // when ifRunning is true, the loop will keep ifRunning
            lastTime = thisTime;
            thisTime = System.nanoTime() / 1000000000.0f;
            passedTime = thisTime - lastTime;
            unprocessedTime += passedTime;
            fpsTimeCumulator += passedTime;
            whileloopcount++;

            //Log.i("Fish :", "the score of fish is "+ Fish.score);

            if (unprocessedTime > framecap) {       // when unprocessedTime > framecap, we need to refresh
                unprocessedTime -= framecap;        // we subtract the framecap for next refesh
                fpsCountCumulator++;                // every time we refresh, we add 1 to fpsCountCumulator
                                                    //               which is a variable used for debugging


                                                    // to draw with canvas, we need three steps:
                                                    //    1. first, lockCanvas, that means instantiate canvas
                                                    //    2. second, draw
                                                    //    3. unlock the canvas and post

                                                    // attention: the surfaceHolder below is actually passed from
                                                    //      GamePanel, that means we draw something in the surfaceView
                                                    //      (which is GamePanel)
                canvas = null;
                Log.e("GameEngine Log", "Another new game loop");
                try {
                    canvas = this.surfaceHolder.lockCanvas();
                    synchronized (surfaceHolder) {

                        this.gamePanel.update();           // we call the methods in gamePanel
                        this.gamePanel.draw(canvas);
                    }
                } catch (Exception e) {
                } finally {
                    if (canvas != null) {
                        try {
                            surfaceHolder.unlockCanvasAndPost(canvas);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }

                if (fpsTimeCumulator > 1) {            // print out the frequency of the engine
                    Log.e("Frequency of the Engine"," fps = " + fpsCountCumulator);
                    fpsCountCumulator = 0;
                    fpsTimeCumulator -= 1.0f;
                }
            }
        }
    }

    public void setIfRunning(boolean ifrunning) {   // this is used to set the status of the game to running or stopped
        this.ifRunning = ifrunning;
    }
}