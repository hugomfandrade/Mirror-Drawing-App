package org.hugoandrade.mirrordrawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DrawingView extends View {

    @SuppressWarnings("unused") private final static String TAG = DrawingView.class.getSimpleName();


    private int nDivider = 0;//16;

    // setup initial color
    private final int paintColor = Color.BLACK;
    // defines paint and canvas
    private Paint drawPaint;

    private Point midPoint;

    private List<Point> mDividerPointList = new ArrayList<>();
    private List<Point> mLastPointList = new ArrayList<>();
    private List<Path> mPathList = new ArrayList<>();

    private final Paint mBitmapPaint;
    private Bitmap mBitmap;
    private Canvas mCanvas;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setupPaint();

        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        setupDividers();
    }

    private void setupPaint() {
        // Setup paint with color and stroke styles
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(5);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    private void setupDividers() {
        // Get height or width of screen
        int screenHeight = getHeight() == 0? getDisplayHeight(getContext()) : getHeight();
        int screenWidth = getWidth() == 0? getDisplayWidth(getContext()) : getWidth();


        // Find middle point
        midPoint = new Point(screenWidth / 2, screenHeight / 2);
        double screenSlopes = (screenHeight / 2d) / (screenWidth / 2d);

        mPathList.clear();
        mLastPointList.clear();
        mDividerPointList.clear();

        double i = 0;
        Point initDividerPoint = new Point(screenWidth / 2, 0);
        if (nDivider == 0) {
            mPathList.add(new Path());
            mLastPointList.add(new Point(0, 0));
        }
        while (i != nDivider) {
            Point vector = Point.subtract(initDividerPoint, midPoint);
            double angle = (360d / nDivider) * (i) / 180d * Math.PI;

            Point dividerVector = Point.rotateBy(vector, angle);
            dividerVector.X = Math.abs(dividerVector.X) < 0.01? 0 : dividerVector.X;
            dividerVector.Y = Math.abs(dividerVector.Y) < 0.01? 0 : dividerVector.Y;

            double slope = Math.abs(dividerVector.Y / dividerVector.X);

            if (slope < screenSlopes) {
                double denominator = Math.abs(dividerVector.X);
                mDividerPointList.add(new Point(
                        dividerVector.X / denominator * screenWidth / 2 + midPoint.X,
                        dividerVector.Y / denominator * screenWidth / 2 + midPoint.Y));
            }
            else {
                double denominator = Math.abs(dividerVector.Y);
                denominator = denominator < 0.1? 1: denominator;
                mDividerPointList.add(new Point(
                        dividerVector.X / denominator * screenHeight / 2 + midPoint.X,
                        dividerVector.Y / denominator * screenHeight / 2 + midPoint.Y));
            }

            mPathList.add(new Path());
            mLastPointList.add(new Point(0, 0));
            i++;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        }

        // Draw Divider Lines
        for (Point line : mDividerPointList)
            canvas.drawLine((float) line.X, (float) line.Y, (float) midPoint.X, (float) midPoint.Y, drawPaint);

        // Draw current path
        for (Path path : mPathList)
            canvas.drawPath(path, drawPaint);
    }

    private void touchStart(List<Point> pointList) {
        if (nDivider == 0) {
            mPathList.get(0).reset();
            mPathList.get(0).moveTo((float) pointList.get(0).X, (float) pointList.get(0).Y);
            mLastPointList.get(0).X = pointList.get(0).X;
            mLastPointList.get(0).Y = pointList.get(0).Y;
        }
        else {
            for (int i = 0; i < nDivider; i++) {
                mPathList.get(i).reset();
                mPathList.get(i).moveTo((float) pointList.get(i).X, (float) pointList.get(i).Y);
                mLastPointList.get(i).X = pointList.get(i).X;
                mLastPointList.get(i).Y = pointList.get(i).Y;
            }
        }
    }

    private void touchMove(List<Point> pointList) {
        if (nDivider == 0) {
            mPathList.get(0).lineTo((float) pointList.get(0).X, (float) pointList.get(0).Y);
            mLastPointList.get(0).X = pointList.get(0).X;
            mLastPointList.get(0).Y = pointList.get(0).Y;
        }
        else {
            for (int i = 0; i < nDivider; i++) {
                mPathList.get(i).lineTo((float) pointList.get(i).X, (float) pointList.get(i).Y);
                mLastPointList.get(i).X = pointList.get(i).X;
                mLastPointList.get(i).Y = pointList.get(i).Y;
            }
        }
    }

    private void touchUp() {
        if (nDivider == 0) {
            mPathList.get(0).lineTo((float) mLastPointList.get(0).X, (float) mLastPointList.get(0).Y);
            mCanvas.drawPath(mPathList.get(0),  drawPaint);
            mPathList.get(0).reset();
        }
        else {
            for (int i = 0; i < mLastPointList.size(); i++) {
                mPathList.get(i).lineTo((float) mLastPointList.get(i).X, (float) mLastPointList.get(i).Y);
                mCanvas.drawPath(mPathList.get(i), drawPaint);
                mPathList.get(i).reset();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Point point = new Point(event.getX(), event.getY());

        List<Point> mPointList = getDividerPointList(point);

        // Checks for the event that occurs
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(mPointList);
                return true;
            case MotionEvent.ACTION_MOVE:
                touchMove(mPointList);
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                break;
            default:
                return false;
        }
        // Force a view to draw again
        postInvalidate();
        return true;
    }

    private List<Point> getDividerPointList(Point point) {
        if (nDivider == 0)
            return Collections.singletonList(point);

        List<Point> mPointList = new ArrayList<>(nDivider);

        for (int i = 0; i < nDivider / 2 ; i++) {
        //for (int i = 0; i < nDivider ; i++) {

            Point dividerVector = Point.subtract(mDividerPointList.get(2 * i), midPoint);
            Point normDividerVector = Point.normalize(dividerVector);

            double angle = (360d / nDivider) * (2 * i) / 180d * Math.PI; /**/

            // Point dividerVector = Point.subtract(mDividerPointList.get(i), midPoint);
            // Point normDividerVector = Point.normalize(dividerVector);
            //
            // double angle = (360d / nDivider) * (i) / 180d * Math.PI; /**/

            Point pointVector = Point.subtract(point, midPoint);
            Point pointVectorRotated = Point.rotateBy(pointVector, angle);
            Point pointVectorRotatedReflected = Point.reflect(pointVectorRotated, normDividerVector);

            mPointList.add(Point.add(pointVectorRotated, midPoint));
            mPointList.add(Point.add(pointVectorRotatedReflected, midPoint)); /**/
        }
        return mPointList;
    }

    public static int getDisplayWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    // DeviceDimensionsHelper.getDisplayHeight(context) => (display height in pixels)
    public static int getDisplayHeight(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }

    public int getDividerNumber() {
        return nDivider;
    }

    public void subtractDivider() {
        if (nDivider != 0) {
            nDivider = nDivider - 2;
            setupDividers();
            invalidate();
        }
    }

    public void addDivider() {
        nDivider = nDivider + 2;
        setupDividers();
        invalidate();
    }
}
