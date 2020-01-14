package com.blogspot.darwinsapp.distancecalculator;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DrawView extends SurfaceView {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    List<Point> circlePoints;
    private Context context;

    private static int REFERENCE_POINT_COLOR = Color.YELLOW;
    private static int MEASURE_POINT_COLOR = Color.RED;
    Bitmap icon;
    public DrawView(Context context, File imageFile){
        super(context);
        this.context = context;
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        circlePoints = new ArrayList<>();
        setWillNotDraw (false);
        String str = imageFile.getAbsolutePath();
        icon = BitmapFactory.decodeFile(str);

        getHolder().setFormat(PixelFormat.TRANSLUCENT);
    }

    @Override
    protected void onDraw(Canvas canvas){
        int size = circlePoints.size();
        //canvas.setBitmap(icon);
        for(int i = 0; i < size; i++){
            //Set color based on order. First 2 points are the reference points.
            if(i < 2) paint.setColor(REFERENCE_POINT_COLOR);
            else paint.setColor(MEASURE_POINT_COLOR);
            Point p = circlePoints.get(i);
            canvas.drawCircle(p.x, p.y, 10, paint);
            if(i == 1){
                canvas.drawLine(circlePoints.get(0).x, circlePoints.get(0).y, circlePoints.get(1).x, circlePoints.get(1).y, paint);
            }
            if(i == 3){
                canvas.drawLine(circlePoints.get(2).x, circlePoints.get(2).y, circlePoints.get(3).x, circlePoints.get(3).y, paint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            if(circlePoints.size() < 4) {
                circlePoints.add(new Point(Math.round(event.getX()), Math.round(event.getY())));
                invalidate();
                if(circlePoints.size() == 2){
                    ((TextView) ((Activity)context).findViewById(R.id.info_lbl)).setText(getResources().getString(R.string.setMeasurePoints));
                }
                if(circlePoints.size() == 4){
                    ((TextView) ((Activity)context).findViewById(R.id.info_lbl)).setText(getResources().getString(R.string.setScaleValue));
                }
            }
        }
        return false;
    }

    /**
     * Clears all drawn points and shapes
     */
    public void clearCanvas(){
        circlePoints.clear();
        ((TextView) ((Activity)context).findViewById(R.id.info_lbl)).setText(getResources().getString(R.string.setPicture));
        invalidate();
    }

    /**
     * Calculates the measurement
     * @param reference The reference size
     * @param inputUnitIndex The input length unit index
     * @param outputUnitIndex The output length unit index
     * @return The value of the measurement, converted to outputUnitIndex
     */
    public double calculate(double reference, int inputUnitIndex, int outputUnitIndex){
        if(circlePoints.size() != 4){
            Toast.makeText(context, getResources().getString(R.string.error_noPoints), Toast.LENGTH_SHORT).show();
            return -1;
        }
        return Ruler.compute(circlePoints, reference, inputUnitIndex, outputUnitIndex);
    }


}
