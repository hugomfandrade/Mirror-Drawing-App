package org.hugoandrade.mirrordrawing;

import android.content.ClipData;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private int screenHeight = 0, screenWidth = 0;
    private int padding = 20;

    private boolean isButtonShown = false;
    private DrawingView drawingView;
    private TextView tvDividerNumber;
    private View view, ivClose, ivDivider, ivDividerPlus, ivDividerMinus, rclDivider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawingView = (DrawingView) findViewById(R.id.drawingView);
        view = findViewById(R.id.fab);
        ivClose = findViewById(R.id.iv_close);
        ivDivider = findViewById(R.id.iv_divider);
        rclDivider = findViewById(R.id.rcl_divider);
        ivDividerMinus = findViewById(R.id.iv_divider_minus);
        ivDividerPlus = findViewById(R.id.iv_divider_plus);
        tvDividerNumber = (TextView) findViewById(R.id.tv_divider_number);

        View v1 = findViewById(R.id.rl_fab);


        screenHeight = DrawingView.getDisplayHeight(this);
        screenWidth = DrawingView.getDisplayWidth(this);

        ivClose.setOnClickListener(mClickListener);
        ivDivider.setOnClickListener(mClickListener);
        ivDividerPlus.setOnClickListener(mClickListener);
        ivDividerMinus.setOnClickListener(mClickListener);
        view.setOnLongClickListener(mLongClickListener);//new MyLongClickListener());
        v1.setOnDragListener(mDragListener);



        //view.setTranslationX(padding);
        //view.setTranslationY(padding);

        setupEditModeVisibility();
    }

    private void setupEditModeVisibility() {
        ivClose.setVisibility(isButtonShown? View.VISIBLE: View.INVISIBLE);
        view.setVisibility(isButtonShown? View.INVISIBLE: View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        isButtonShown = !isButtonShown;
        setupEditModeVisibility();

    }

    private View.OnLongClickListener mLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
            v.startDrag(data, shadowBuilder, v, 0);
            v.setVisibility(View.INVISIBLE);
            return false;
        }
    };

    private final View.OnDragListener mDragListener = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, final DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DROP:
                    if (event.getLocalState() == view) {
                        view.setVisibility(View.VISIBLE);

                        final float toX = event.getX() > screenWidth / 2 ?
                                screenWidth - padding - view.getMeasuredWidth() :
                                padding;
                        MTranslateAnimation animation =
                                new MTranslateAnimation(view, event.getX(), toX, event.getY(), event.getY());
                        animation.setDuration(500);
                        animation.setAnimationListener(new EndAnimationListener() {
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                view.setTranslationX(toX);
                                view.setTranslationY(event.getY());
                            }
                        });

                        view.startAnimation(animation);
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == ivClose)
                finish();
            else if (v == ivDivider)
                showHideDivider();
            else if (v == ivDividerMinus) {
                if (drawingView.getDividerNumber() != 0) {
                    drawingView.subtractDivider();
                    tvDividerNumber.setText(String.valueOf(drawingView.getDividerNumber()));
                }
            }
            else if (v == ivDividerPlus) {
                if (drawingView.getDividerNumber() != 20) {
                    drawingView.addDivider();
                    tvDividerNumber.setText(String.valueOf(drawingView.getDividerNumber()));
                }
            }
        }
    };

    private void showHideDivider() {
        if (rclDivider.getVisibility() == View.INVISIBLE) {
            MTranslateAnimation animation =
                    new MTranslateAnimation(rclDivider, 0, 0, 0, rclDivider.getMeasuredHeight());
            animation.setDuration(500);
            animation.setAnimationListener(new EndAnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    rclDivider.setTranslationY(rclDivider.getMeasuredHeight());
                }
            });

            rclDivider.startAnimation(animation);
            rclDivider.setVisibility(View.VISIBLE);
        }
        else {
            MTranslateAnimation animation =
                    new MTranslateAnimation(rclDivider, 0, 0, rclDivider.getMeasuredHeight(), 0);
            animation.setDuration(500);
            animation.setAnimationListener(new EndAnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    rclDivider.setTranslationY(0);
                    rclDivider.setVisibility(View.INVISIBLE);
                }
            });

            rclDivider.startAnimation(animation);
        }
    }

    private class MTranslateAnimation extends Animation {

        private float mFromXValue = 0.0f;
        private float mToXValue = 0.0f;
        private float mFromYValue = 0.0f;
        private float mToYValue = 0.0f;
        private View view;

        MTranslateAnimation(View v, float fromX, float toX, float fromY, float toY) {
            mFromXValue = fromX;
            mToXValue = toX;
            mFromYValue = fromY;
            mToYValue = toY;
            view = v;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            if (view.getTranslationX() != mToXValue) {
                view.setTranslationX(
                        mFromXValue + (int) ((mToXValue - mFromXValue) * interpolatedTime));
            }
            if (view.getTranslationY() != mToYValue) {
                view.setTranslationY(
                        mFromYValue + (int) ((mToYValue - mFromYValue) * interpolatedTime));
            }
        }
    }
}
