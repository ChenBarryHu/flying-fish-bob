package com.shichen.android.flyingfish.GameCharacter;

/**
 * Created by hsctn on 2016-06-25.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import com.shichen.android.flyingfish.GamePanel;
import com.shichen.android.flyingfish.R;
import java.util.ArrayList;
import java.util.Random;


/**
 * Created by hsctn on 2016-06-25.
 */
public class MonsterCommander {

    // these are the monsters
    static public ArrayList<Bird> monsters;
    static public int[] backup_bird_pos_x;
    static public int[] backup_bird_pos_y;
    static public int[] backup_bird_speed;


    // this variable is used to determine when to create monsters
    public long monsterStartAppearTime;


    private long elaspedTime;
    private Fish fish;
    private boolean ifMonsterOccurInThisRound = false;
    private Context context;
    private Random random;
    private int birdSelector;  // true represent big yellow bird, false represent colorful bird
    private final int widthForBigYellow = 179;
    private final int heightForBigYellow = 158;
    private final int widthForColorfulBird = 64;
    private final int heightForColorfulBird = 64;
    private final int widthForBigEyeBlue = 146;
    private final int heightForBigEyeBlue = 100;
    private int numOfPosForBY = 2;
    private int numOfPosForCB = 4;
    private int numOfPosForBEB = 8;
    private Bitmap[] resForBigYellow = new Bitmap[numOfPosForBY];
    private Bitmap resForColorfulBird;
    private Bitmap[] resForBigEyeBlue = new Bitmap[numOfPosForBEB];

    public MonsterCommander(Context context, Fish fish) {
        this.fish = fish;
        this.monsters = new ArrayList<Bird>();
        this.random = new Random();
        this.context = context;
        this.resForBigYellow[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.bigyellowup);
        this.resForBigYellow[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.bigyellowdown);
        this.resForColorfulBird = BitmapFactory.decodeResource(context.getResources(), R.drawable.birds);
        this.resForBigEyeBlue[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.bbe1);
        this.resForBigEyeBlue[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.bbe2);
        this.resForBigEyeBlue[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.bbe3);
        this.resForBigEyeBlue[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.bbe4);
        this.resForBigEyeBlue[4] = BitmapFactory.decodeResource(context.getResources(), R.drawable.bbe5);
        this.resForBigEyeBlue[5] = BitmapFactory.decodeResource(context.getResources(), R.drawable.bbe6);
        this.resForBigEyeBlue[6] = BitmapFactory.decodeResource(context.getResources(), R.drawable.bbe7);
        this.resForBigEyeBlue[7] = BitmapFactory.decodeResource(context.getResources(), R.drawable.bbe8);
    }

    private int selectBird() {
        double intermedia = random.nextDouble() * 100;
        if (intermedia > 0 && intermedia <= 40) {
            return 0;
        } else if (intermedia >= 70) {
            return 1;
        } else {
            return 2;
        }
    }



    public void update() {
        //Log.e("MonsterCommander:","MonsterCommander update!!!!!");
        if (!ifMonsterOccurInThisRound) {
            monsterStartAppearTime = System.nanoTime();
            ifMonsterOccurInThisRound = true;
        }
        elaspedTime = (System.nanoTime() - monsterStartAppearTime) / 1000000;


        if(!GamePanel.unstoppableMode) {
            if (fish.pos_y < 20 || fish.pos_y > 546) {
                fish.setIfcurrentlyplaying(false);
            }


            for (int i = 0; i < monsters.size(); i++) {

                monsters.get(i).update();
                Log.e("MonsterCommander","Monster position_x"+monsters.get(i).pos_x);
                if (collision(monsters.get(i), fish)) {
                    monsters.remove(i);
                    fish.setIfcurrentlyplaying(false);
                    break;
                }
                if (monsters.get(i).getPos_x() < -180) {
                    monsters.remove(i);
                }
            }
        }else{
            for (int i = 0; i < monsters.size(); i++) {
                monsters.get(i).update();
                if (monsters.get(i).getPos_x() < -180) {
                    monsters.remove(i);
                }
                if (collisionUnstoppableMode(monsters.get(i), fish)) {
                    monsters.remove(i);
                    break;
                }

            }
        }

        int timeForMonsterOut = (int) (2000 + random.nextFloat() * 3000- Fish.score *10);
        if(timeForMonsterOut < 500){
            timeForMonsterOut = 500;
        }
        if (elaspedTime > timeForMonsterOut) {
            birdSelector = selectBird();
            if (birdSelector == 0) {
                monsters.add(new BigyellowBird(resForBigYellow, GamePanel.WIDTH + 10,
                        (int) (random.nextDouble() * (GamePanel.HEIGHT - heightForBigYellow)), numOfPosForBY));
            } else if (birdSelector == 1) {
                monsters.add(new ColorfulBird(resForColorfulBird, GamePanel.WIDTH + 10,
                        (int) (random.nextDouble() * (GamePanel.HEIGHT - heightForColorfulBird))
                        , numOfPosForCB));
            } else {
                monsters.add(new Bigeyebluebird(resForBigEyeBlue, GamePanel.WIDTH + 10,
                        (int) (random.nextDouble() * (GamePanel.HEIGHT - heightForBigEyeBlue)), numOfPosForBEB));
            }
            monsterStartAppearTime = System.nanoTime();
        }
    }


    public void draw(Canvas canvas) {
        for (Bird bird : monsters) {
            bird.draw(canvas);
        }
    }

    public boolean collision(GameObject a, GameObject b) {
        if (Rect.intersects(a.getRectangle(), b.getRectangle())) {
            return true;
        }
        return false;
    }
    public boolean collisionUnstoppableMode(GameObject a, Fish b) {
        if ((Rect.intersects(a.getRectangle(), b.getBiggerUnstoppableRectangle()))||
                (Rect.intersects(a.getRectangle(), b.getSmallerUnstoppableRectangle()))) {
            return true;
        }
        return false;
    }

    public void setIfMonsterOccurInThisRound(boolean ifMonsterOccurInThisRound) {
        this.ifMonsterOccurInThisRound = ifMonsterOccurInThisRound;
    }


    public static void backup(){
        int backuparraysize =  monsters.size();
        backup_bird_pos_x = new int[backuparraysize];
        backup_bird_pos_y = new int[backuparraysize];
        backup_bird_speed = new int[backuparraysize];
        for (int i = 0; i < monsters.size(); i++) {

            backup_bird_pos_y[i]=monsters.get(i).pos_y;
            backup_bird_pos_x[i]=monsters.get(i).pos_x;
            backup_bird_speed[i]=monsters.get(i).Velocity_x;

        }
    }

}
