package com.shichen.android.flyingfish.GameCharacter;

import android.graphics.Canvas;

/**
 * Created by hsctn on 2016-07-15.
 */
public abstract class Bonus extends GameObject{
    public int bonusindex;
    public abstract void update(long elaspedtime);
    public abstract void draw(Canvas canvas);
}
