package android.support.design.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import net.androidx.gestureanimate.R;

import java.util.List;

/**
 * 从AppBarLayout.ScrollingViewBehavior抽离出来，便于自定义
 * Created by zhongyongsheng on 2020-01-06.
 */
public class WeChatScrollingViewBehavior extends HeaderScrollingViewBehavior {
    public WeChatScrollingViewBehavior() {
    }

    public WeChatScrollingViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a =
                context.obtainStyledAttributes(attrs, R.styleable.ScrollingViewBehavior_Layout);
        this.setOverlayTop(
                a.getDimensionPixelSize(R.styleable.ScrollingViewBehavior_Layout_behavior_overlapTop,
                        0));
        a.recycle();
    }

    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        this.offsetChildAsNeeded(child, dependency);
        this.updateLiftedStateIfNeeded(child, dependency);
        return false;
    }

    public boolean onRequestChildRectangleOnScreen(CoordinatorLayout parent, View child,
                                                   Rect rectangle, boolean immediate) {
        AppBarLayout header = this.findFirstDependency(parent.getDependencies(child));
        if (header != null) {
            rectangle.offset(child.getLeft(), child.getTop());
            Rect parentRect = this.tempRect1;
            parentRect.set(0, 0, parent.getWidth(), parent.getHeight());
            if (!parentRect.contains(rectangle)) {
                header.setExpanded(false, !immediate);
                return true;
            }
        }

        return false;
    }

    private void offsetChildAsNeeded(View child, View dependency) {
        android.support.design.widget.CoordinatorLayout.Behavior behavior =
                ((android.support.design.widget.CoordinatorLayout.LayoutParams) dependency
                        .getLayoutParams()).getBehavior();
        if (behavior instanceof WeChatBaseBehavior) {
            WeChatBaseBehavior ablBehavior = (WeChatBaseBehavior) behavior;
            ViewCompat.offsetTopAndBottom(child,
                    dependency.getBottom() - child.getTop() + ablBehavior.offsetDelta +
                            this.getVerticalLayoutGap() -
                            this.getOverlapPixelsForOffset(dependency));
        }

    }

    float getOverlapRatioForOffset(View header) {
        if (header instanceof AppBarLayout) {
            AppBarLayout abl = (AppBarLayout) header;
            int totalScrollRange = abl.getTotalScrollRange();
            int preScrollDown = abl.getDownNestedPreScrollRange();
            int offset = getAppBarLayoutOffset(abl);
            if (preScrollDown != 0 && totalScrollRange + offset <= preScrollDown) {
                return 0.0F;
            }

            int availScrollRange = totalScrollRange - preScrollDown;
            if (availScrollRange != 0) {
                return 1.0F + (float) offset / (float) availScrollRange;
            }
        }

        return 0.0F;
    }

    private static int getAppBarLayoutOffset(AppBarLayout abl) {
        android.support.design.widget.CoordinatorLayout.Behavior behavior =
                ((android.support.design.widget.CoordinatorLayout.LayoutParams) abl
                        .getLayoutParams()).getBehavior();
        return behavior instanceof WeChatBaseBehavior ?
                ((WeChatBaseBehavior) behavior).getTopBottomOffsetForScrollingSibling() : 0;
    }

    AppBarLayout findFirstDependency(List<View> views) {
        int i = 0;

        for (int z = views.size(); i < z; ++i) {
            View view = (View) views.get(i);
            if (view instanceof AppBarLayout) {
                return (AppBarLayout) view;
            }
        }

        return null;
    }

    int getScrollRange(View v) {
        return v instanceof AppBarLayout ? ((AppBarLayout) v).getTotalScrollRange() :
                super.getScrollRange(v);
    }

    private void updateLiftedStateIfNeeded(View child, View dependency) {
        if (dependency instanceof AppBarLayout) {
            AppBarLayout appBarLayout = (AppBarLayout) dependency;
            if (appBarLayout.isLiftOnScroll()) {
                appBarLayout.setLiftedState(child.getScrollY() > 0);
            }
        }

    }

}
