package com.lebartodev.mercurycamera;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.OrientationEventListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.transitionseverywhere.Rotate;
import com.transitionseverywhere.Transition;
import com.transitionseverywhere.TransitionManager;

/**
 * Created by Александр on 01.12.2016.
 */

public class RotatingImageView extends ImageView {
    private OrientationEventListener myOrientationEventListener;
    private ViewGroup transitionContainer;
    private boolean transitionInProcess = false;
    private boolean needRotate = false;
    private int angle = 0;

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public RotatingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initOrientationListener(context);

    }

    public void setTransitionContainer(ViewGroup transitionContainer) {
        this.transitionContainer = transitionContainer;
    }

    public RotatingImageView(Context context) {
        super(context);
        initOrientationListener(context);
    }

    public RotatingImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initOrientationListener(context);
    }

    private void initOrientationListener(Context context) {
        myOrientationEventListener = new OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int i) {
                if (i > 80 && i < 100) {
                    if (angle != -90) {
                        needRotate = true;
                        angle = -90;
                    }
                } else if (i > 170 && i < 190) {
                    if (angle != -180) {
                        needRotate = true;
                        angle = -180;
                    }
                } else if (i > 260 && i < 280) {
                    if (angle != -270 && angle != 90)
                        needRotate = true;
                    if (angle == 0) {
                        angle = 90;
                    } else {
                        angle = -270;
                    }

                } else if (i < 10 || i > 350) {

                    if (angle != 0 && angle != -360)
                        needRotate = true;
                    if (angle == -270) {
                        angle = -360;
                    } else {
                        angle = 0;
                    }
                }

                if (needRotate) {
                    rotateTo(angle, true);
                }

                needRotate = false;
            }
        };
        if (myOrientationEventListener.canDetectOrientation()) {
            myOrientationEventListener.enable();
        }
    }

    private void rotateWithoutAnim(int degree) {
        TransitionManager.endTransitions(transitionContainer);
        angle = degree;
        this.setRotation(degree);
    }

    public void rotateTo(final int degree, boolean needAnim) {
        if (transitionContainer != null && !transitionInProcess && needAnim) {


            TransitionManager.beginDelayedTransition(transitionContainer, new Rotate().addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                    transitionInProcess = true;
                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    transition.removeListener(this);
                    transitionInProcess = false;
                    if (degree == 90) {
                        rotateWithoutAnim(-270);
                    }
                    if (degree == -360) {
                        rotateWithoutAnim(0);
                    }


                }

                @Override
                public void onTransitionCancel(Transition transition) {
                    transitionInProcess = false;
                }

                @Override
                public void onTransitionPause(Transition transition) {

                }

                @Override
                public void onTransitionResume(Transition transition) {

                }
            }));


        }
        this.setRotation(degree);

    }
}
