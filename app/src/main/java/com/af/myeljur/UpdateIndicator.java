package com.af.myeljur;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Peter on 17.01.2017.
 */

public class UpdateIndicator extends View {
    Paint BackgroundPaint;
    Paint CirclePaint;
    Paint SpinPaint;
    Context context;
    int height;
    int width;
    int h0;
    int w0;
    int aW;
    int aH;
    float arc;
    boolean spinning;
    ScheduledThreadPoolExecutor animator;
    float[] aPoints;
    float firstY;
    boolean needUpdate;
    int revealedY;
    Callback callback;
    Activity activity;
    ListView cView;
    int scrollY;
    Runnable anim;
    ScheduledFuture t;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    interface Callback{
        void onReady();
        void onUpdate();
    }
    public UpdateIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        this.context=context;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width=w-w/8;
        height=h-h/8;
        w0=w/8;
        h0=h/8;
        CirclePaint.setStrokeWidth(w/14);
        SpinPaint.setStrokeWidth(w/14);
        aW=width/6;
        aH=height/6;
        invalidate();
        callback.onReady();

    }

    public void hide(){
        if(spinning) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) UpdateIndicator.this.getLayoutParams();
            params.setMargins(params.leftMargin, (int) (0 - Math.round(height * 1.1)), 0, 0);
            UpdateIndicator.this.setLayoutParams(params);
            setArc(0);
            stopSpin();
            invalidate();
        }
    }

    void init(){
        width=0;
        height=0;
        revealedY=0;
        arc=0;
        BackgroundPaint = new Paint();
        CirclePaint = new Paint();
        SpinPaint = new Paint();
        BackgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        BackgroundPaint.setColor(getResources().getColor(R.color.colorPrimaryDark));
        BackgroundPaint.setStrokeWidth(3);
        BackgroundPaint.setAntiAlias(true);
        CirclePaint.setStyle(Paint.Style.STROKE);
        CirclePaint.setColor(getResources().getColor(R.color.colorAccent));
        CirclePaint.setStrokeWidth(2);
        CirclePaint.setStrokeCap(Paint.Cap.BUTT);
        CirclePaint.setAntiAlias(true);
        SpinPaint.setStyle(Paint.Style.STROKE);
        SpinPaint.setColor(Color.WHITE);
        SpinPaint.setStrokeWidth(2);
        SpinPaint.setStrokeCap(Paint.Cap.BUTT);
        SpinPaint.setAntiAlias(true);
        aPoints = new float[]{-90f,90f};
        animator = new ScheduledThreadPoolExecutor(1);
        anim = new Runnable() {
            @Override
            public void run() {
                if(aPoints[1]>0){
                    aPoints[0]+=0.1f;
                    aPoints[1]+=0.2f;
                }else {
                    aPoints[0]+=0.2f;
                    aPoints[1]+=0.1f;
                }
                if(aPoints[1]>=360){
                    aPoints[1]=-aPoints[1];
                }
                postInvalidate();
            }
        };


    }

    public void setArc(float degree){
            if(degree>=0) {
                arc = degree;
                invalidate();
            }

    }

    public void animateSpin(){
            aPoints[0]=-90;
            aPoints[1]=90;
            if (!spinning) {
                t=animator.scheduleAtFixedRate(anim,0,1,TimeUnit.MILLISECONDS);
                spinning = true;
            }

    }
    public void stopSpin(){
        t.cancel(true);
        spinning=false;
        postInvalidate();
    }


    public void setRevealed(int r){
        int margin;
        if(r>=0&&r<=revealedY) {
            margin = (int) (r - Math.round(height * 1.1));
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) UpdateIndicator.this.getLayoutParams();
            //params.setMargins(x/2-100,10,0,0);
            params.setMargins(params.leftMargin, margin, 0, 0);
            UpdateIndicator.this.setLayoutParams(params);
            invalidate();
        }

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawOval(w0,h0,width,height,BackgroundPaint);
        canvas.drawArc(aW+w0,aH+h0,width-aW,height-aH, -90, arc,false,CirclePaint);
        if(spinning){
            canvas.drawArc(aW+w0,aH+h0,width-aW,height-aH, aPoints[0], aPoints[1],false,SpinPaint);
            //canvas.drawArc(aW+w0,aH+h0,width-aW,height-aH, aPoints[1], 90,false,SpinPaint);
        }
    }

    public void setListView(ListView lView, Activity activity){
        cView = lView;
        this.activity=activity;
        int y = Utils.getScreenHeight(activity);
        firstY = 0;
        revealedY = height*2;

        final int pm = y/4;
        final float offset = 360/(float)pm;
        final float rOffset = revealedY/(float)pm;


        lView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!EljurApi.offline()){
                    View c = cView.getChildAt(0);
                    try {
                        scrollY = -c.getTop() + cView.getFirstVisiblePosition() * c.getHeight();
                    }catch (NullPointerException e){
                        return false;
                    }
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if(spinning)
                            return true;
                        if (scrollY == 0 && firstY == 0) {
                            firstY = motionEvent.getY();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (spinning)
                            return true;
                        if (scrollY == 0) {
                            float move = motionEvent.getY() - firstY;
                            if (firstY == 0) {
                                firstY = motionEvent.getY();
                            } else if (move > pm) {
                                needUpdate = true;
                                setArc(move * offset);
                                setRevealed(Math.round(revealedY));
                            } else {
                                needUpdate=false;
                                setArc(move * offset);
                                setRevealed(Math.round(move * rOffset));

                            }
                            if(move>0)
                                return true;

                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if(spinning)
                            return true;
                        if (needUpdate) {
                            callback.onUpdate();
                            animateSpin();
                            needUpdate = false;
                        } else {
                            setRevealed(0);
                        }
                        firstY = 0;

                }
                return false;
            }else return false;
            }
        });
    }
}
