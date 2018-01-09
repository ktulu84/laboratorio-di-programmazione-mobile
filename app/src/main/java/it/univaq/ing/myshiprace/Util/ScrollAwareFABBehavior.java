package it.univaq.ing.myshiprace.Util;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by ktulu on 09/01/18.
 */

public class ScrollAwareFABBehavior extends FloatingActionButton.Behavior
{

    public ScrollAwareFABBehavior(Context context, AttributeSet attrs)
    {
        super();
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout,
                                       FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes, int type)
    {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target,
                        nestedScrollAxes, type);
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child,
                               View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type)
    {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed,
                dyUnconsumed, type);

        if (dyConsumed > 0 && child.getVisibility() == View.VISIBLE)
        {
            child.hide(new FloatingActionButton.OnVisibilityChangedListener()
            {
                @Override
                public void onHidden(FloatingActionButton fab)
                {
                    super.onHidden(fab);
                    fab.setVisibility(View.INVISIBLE);
                }
            });
        }
        else if (dyConsumed < 0 && child.getVisibility() != View.VISIBLE)
        {
            child.show();
        }
    }

}