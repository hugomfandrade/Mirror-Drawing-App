package org.hugoandrade.mirrordrawing;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class RoundedCornerLayout extends LinearLayout {

    @SuppressWarnings("unused")
    private static final String TAG = RoundedCornerLayout.class.getSimpleName();

    private float[] cornerRadius = new float[8];
    private float elevationStart, elevationTop, elevationEnd, elevationBottom;
    private float percentileHeight, percentileWidth;
    private int borderColor;
    private int backgroundColor;
    private int backgroundColorSelected;

    private OnTouchStateChangeListener mOnTouchStateListener;
    private OnClickListener mOnClickListener;

    private Rect rect;
    private RectF rect2 = new RectF();
    private boolean wasPressedOutside;
    private Path path = new Path();

    public RoundedCornerLayout(Context context) {
        super(context);
        init(context, null);
    }

    public RoundedCornerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RoundedCornerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundedCornerLayout, 0, 0);

        float cornerRadiusGlobal =
                a.getDimension(R.styleable.RoundedCornerLayout_corner_radius, 0f);
        float cornerRadiusTopStart =
                a.getDimension(R.styleable.RoundedCornerLayout_corner_radius_top_start, cornerRadiusGlobal);
        float cornerRadiusTopEnd =
                a.getDimension(R.styleable.RoundedCornerLayout_corner_radius_top_end, cornerRadiusGlobal);
        float cornerRadiusBottomStart =
                a.getDimension(R.styleable.RoundedCornerLayout_corner_radius_bottom_start, cornerRadiusGlobal);
        float cornerRadiusBottomEnd =
                a.getDimension(R.styleable.RoundedCornerLayout_corner_radius_bottom_end, cornerRadiusGlobal);
        float elevation =
                a.getDimension(R.styleable.RoundedCornerLayout_border_elevation, 0f);
        elevationStart =
                a.getDimension(R.styleable.RoundedCornerLayout_border_elevation_start, elevation);
        elevationTop =
                a.getDimension(R.styleable.RoundedCornerLayout_border_elevation_top, elevation);
        elevationEnd =
                a.getDimension(R.styleable.RoundedCornerLayout_border_elevation_end, elevation);
        elevationBottom =
                a.getDimension(R.styleable.RoundedCornerLayout_border_elevation_bottom, elevation);
        borderColor =
                a.getColor(R.styleable.RoundedCornerLayout_border_color, Color.parseColor("#CABBBBBB"));
        backgroundColor =
                a.getColor(R.styleable.RoundedCornerLayout_background_color, Color.TRANSPARENT);
        backgroundColorSelected =
                a.getColor(R.styleable.RoundedCornerLayout_background_color_selected, Color.TRANSPARENT);
        percentileHeight =
                a.getFloat(R.styleable.RoundedCornerLayout_percentile_height, Float.NaN);
        percentileWidth =
                a.getFloat(R.styleable.RoundedCornerLayout_percentile_width, Float.NaN);
        boolean isClickable =
                attrs.getAttributeBooleanValue("http://schemas.android.com/apk/res/android", "clickable", true);
        a.recycle();

        if (!Float.isNaN(percentileWidth) && !Float.isNaN(percentileHeight)) {
            percentileHeight = Float.NaN;
            percentileWidth = Float.NaN;
        }
        else {
            if (!Float.isNaN(percentileWidth)) {
                if (percentileWidth < 0)
                    percentileWidth = 0;
                else if (percentileWidth > 100)
                    percentileWidth = 100;
            }
            if (!Float.isNaN(percentileHeight)) {
                if (percentileHeight < 0)
                    percentileHeight = 0;
                else if (percentileHeight > 100)
                    percentileHeight = 100;
            }
        }

        cornerRadius[0] = cornerRadiusTopStart;
        cornerRadius[1] = cornerRadiusTopStart;
        cornerRadius[2] = cornerRadiusTopEnd;
        cornerRadius[3] = cornerRadiusTopEnd;
        cornerRadius[4] = cornerRadiusBottomEnd;
        cornerRadius[5] = cornerRadiusBottomEnd;
        cornerRadius[6] = cornerRadiusBottomStart;
        cornerRadius[7] = cornerRadiusBottomStart;

        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
        setClickable(isClickable);
        setOnTouchListener(mTouchListener);
        setBackground(
                makeSelectorBackgroundDrawable(
                        backgroundColor, backgroundColorSelected, borderColor, cornerRadius,
                        new float[] {elevationStart, elevationTop, elevationEnd, elevationBottom}));

        setWillNotDraw(false);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(
                (int) (left + (elevationStart < 0 ? 0 : elevationStart)),
                (int) (top + (elevationTop < 0 ? 0 : elevationTop)),
                (int) (right + (elevationEnd < 0 ? 0 : elevationEnd)),
                (int) (bottom + (elevationBottom < 0 ? 0 : elevationBottom)));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // compute the path
        path.reset();
        rect2.set(
                0 + (elevationStart < 0 ? 0 : elevationStart),
                0 + (elevationTop < 0 ? 0 : elevationTop),
                w - (elevationEnd < 0 ? 0 : elevationEnd),
                h - (elevationBottom < 0 ? 0 : elevationBottom));
        path.addRoundRect(rect2, cornerRadius, Path.Direction.CW);
        path.close();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        /*super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        if (!Float.isNaN(percentileHeight)) {
            setMeasuredDimension(width, (int) (width * percentileHeight / 100f));
        } else if (!Float.isNaN(percentileWidth)) {
            setMeasuredDimension((int) (height * percentileWidth / 100f), height);
        }/**/

        /*int MAX_TEXT_WIDTH = (int) (heigthWithoutPadding * RATIO);
        int maxHeight = (int) (widthWithoutPadding / RATIO);

        if (widthWithoutPadding  &gt; MAX_TEXT_WIDTH) {
            width = MAX_TEXT_WIDTH + getPaddingLeft() + getPaddingRight();
        } else {
            height = maxHeight + getPaddingTop() + getPaddingBottom();
        }/**/

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (!Float.isNaN(percentileHeight)) {
            //setMeasuredDimension(lesserDimension, lesserDimension);
            super.onMeasure(
                    widthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(
                            (int) (width * percentileHeight / 100f),
                            MeasureSpec.EXACTLY
                    ));
        } else if (!Float.isNaN(percentileWidth)) {
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(
                            (int) (height * percentileWidth / 100f),
                            MeasureSpec.EXACTLY
                    ), heightMeasureSpec);
        }
        else
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);/**/
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int save = canvas.save();
        canvas.clipPath(path);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(save);
    }

    @Override
    public void setBackgroundColor(@ColorInt int color) {
        backgroundColor = color;
        setBackground(makeSelectorBackgroundDrawable(
                backgroundColor, backgroundColorSelected, borderColor, cornerRadius,
                new float[] {elevationStart, elevationTop, elevationEnd, elevationBottom}));
    }

    public void setBackgroundSelectedColor(@ColorInt int color) {
        backgroundColorSelected = color;
        setBackground(makeSelectorBackgroundDrawable(
                backgroundColor, backgroundColorSelected, borderColor, cornerRadius,
                new float[] {elevationStart, elevationTop, elevationEnd, elevationBottom}));
    }

    public void setBorderColor(@ColorInt int color) {
        borderColor = color;
        setBackground(makeSelectorBackgroundDrawable(
                backgroundColor, backgroundColorSelected, borderColor, cornerRadius,
                new float[] {elevationStart, elevationTop, elevationEnd, elevationBottom}));
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        setClickable(true);
        mOnClickListener = listener;
    }

    public void disableHeightPercentile() {
        percentileHeight = Float.NaN;
    }

    public void disableWidthPercentile() {
        percentileWidth = Float.NaN;
    }

    interface OnTouchStateChangeListener {
        void onTouchState(View v, int state);
    }
    @SuppressWarnings("unused")
    public void setOnTouchStateChangeListener(OnTouchStateChangeListener listener) {
        mOnTouchStateListener = listener;
    }
    private OnTouchListener mTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!isClickable())
                return false;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                setPressed(true);
                wasPressedOutside = false;
                rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                setTouchStateAndSendToListener(v, MotionEvent.ACTION_DOWN);

            } else if (wasPressedOutside) {
                return true;
            }
            else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if(!rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())){
                    // User moved outside bounds
                    wasPressedOutside = true;
                    setPressed(false);
                    setTouchStateAndSendToListener(v, MotionEvent.ACTION_UP);

                    return true;
                }
            }
            else if (event.getAction() == MotionEvent.ACTION_UP) {
                setPressed(false);
                setTouchStateAndSendToListener(v, MotionEvent.ACTION_UP);
                if (mOnClickListener != null && isClickable())
                    mOnClickListener.onClick(v);
            }
            else if (event.getAction() == MotionEvent.ACTION_CANCEL ) {
                setPressed(false);
                setTouchStateAndSendToListener(v, MotionEvent.ACTION_UP);
            }
            return true;
        }

        private void setTouchStateAndSendToListener(View v, int action) {
            if (mOnTouchStateListener != null)
                mOnTouchStateListener.onTouchState(v, action);
        }
    };

    private static Drawable makeSelectorBackgroundDrawable(@ColorInt int backgroundColor,
                                                           @ColorInt int backgroundColorSelected,
                                                           @ColorInt int borderColor,
                                                           float[] cornerRadius,
                                                           float[] elevation) {
        StateListDrawable res = new StateListDrawable();
        res.addState(new int[]{android.R.attr.state_pressed},
                makeBackgroundDrawable(backgroundColorSelected, borderColor, cornerRadius, elevation));
        res.addState(new int[]{android.R.attr.state_selected},
                makeBackgroundDrawable(backgroundColorSelected, borderColor, cornerRadius, elevation));
        res.addState(new int[]{},
                makeBackgroundDrawable(backgroundColor, borderColor, cornerRadius, elevation));
        return res;
    }

    private static Drawable makeBackgroundDrawable(@ColorInt int backgroundColor,
                                                   @ColorInt int borderColor,
                                                   float[] cornerRadius,
                                                   float[] elevation) {

        //ShapeDrawable border = new ShapeDrawable(new RoundRectShape(cornerRadius, null, null));
        //border.getPaint().setColor(borderColor);
        //border.getPaint().setStrokeWidth(max(elevation));

        GradientDrawable border = new GradientDrawable();
        border.setColor(Color.parseColor("#00FFFFFF")); //white background
        border.setStroke((int) max(elevation) + 1,
                elevation[0] == 0 && elevation[1] == 0 && elevation[2] == 0 && elevation[3] == 0 ?
                        Color.TRANSPARENT :
                        borderColor); //black border with full opacity
        border.setCornerRadii(cornerRadius); //black border with full opacity

        ShapeDrawable background = new ShapeDrawable(new RoundRectShape(cornerRadius, null, null));
        background.getPaint().setColor(backgroundColor);

        Drawable[] drawableArray = {border, background};
        LayerDrawable l = new LayerDrawable(drawableArray);
        l.setLayerInset(0,
                (int) (elevation[0] < 0? -elevation[0] : 0),
                (int) (elevation[1] < 0? -elevation[1] : 0),
                (int) (elevation[2] < 0? -elevation[2] : 0),
                (int) (elevation[3] < 0? -elevation[3] : 0));
        l.setLayerInset(1,
                (int) (elevation[0] < 0? 0 : elevation[0]),
                (int) (elevation[1] < 0? 0 : elevation[1]),
                (int) (elevation[2] < 0? 0 : elevation[2]),
                (int) (elevation[3] < 0? 0 : elevation[3]));

        return l;
    }

    private static float max(float[] elevation) {
        float max = Float.NaN;
        for (float f : elevation)
            if (Float.isNaN(max) || f > max)
                max = f;
        return max;
    }
}
