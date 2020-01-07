package android.support.design.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.design.animation.AnimationUtils;
import android.support.v4.math.MathUtils;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;

import net.slog.SLogger;
import net.slog.SLoggerFactory;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * 从AppBarLayout.BaseBehavior抽离出来
 * Created by zhongyongsheng on 2020-01-06.
 */
public class WeChatBaseBehavior<T extends AppBarLayout> extends HeaderBehavior<T> {
    private static final SLogger log = SLoggerFactory.getLogger("WeChatBaseBehavior");
    private static final int MAX_OFFSET_ANIMATION_DURATION = 600;
    private static final int INVALID_POSITION = -1;
    int offsetDelta;
    private int lastStartedType;
    private ValueAnimator offsetAnimator;
    private int offsetToChildIndexOnLayout = -1;
    private boolean offsetToChildIndexOnLayoutIsMinHeight;
    private float offsetToChildIndexOnLayoutPerc;
    private WeakReference<View> lastNestedScrollingChildRef;
    private AppBarLayout.BaseBehavior.BaseDragCallback onDragCallback;

    public WeChatBaseBehavior() {
    }

    public WeChatBaseBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean onStartNestedScroll(CoordinatorLayout parent, T child, View directTargetChild,
                                       View target, int nestedScrollAxes, int type) {
        boolean started = (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0
                && (child.isLiftOnScroll() || this.canScrollChildren(parent, child, directTargetChild));
        if (started && this.offsetAnimator != null) {
            this.offsetAnimator.cancel();
        }

        this.lastNestedScrollingChildRef = null;
        this.lastStartedType = type;
        return started;
    }

    private boolean canScrollChildren(CoordinatorLayout parent, T child, View directTargetChild) {
        return child.hasScrollableChildren() &&
                parent.getHeight() - directTargetChild.getHeight() <= child.getHeight();
    }

    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, T child, View target, int dx,
                                  int dy, int[] consumed, int type) {
        if (dy != 0) {
            int min;
            int max;
            if (dy < 0) {
                min = -child.getTotalScrollRange();
                max = min + child.getDownNestedPreScrollRange();
            } else {
                min = -child.getUpNestedPreScrollRange();
                max = 0;
            }

            if (min != max) {
                consumed[1] = this.scroll(coordinatorLayout, child, dy, min, max);
                this.stopNestedScrollIfNeeded(dy, child, target, type);
            }
        }

    }

    public void onNestedScroll(CoordinatorLayout coordinatorLayout, T child, View target,
                               int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed,
                               int type) {
        if (dyUnconsumed < 0) {
            this.scroll(coordinatorLayout, child, dyUnconsumed, -child.getDownNestedScrollRange(),
                    0);
            this.stopNestedScrollIfNeeded(dyUnconsumed, child, target, type);
        }

        if (child.isLiftOnScroll()) {
            child.setLiftedState(target.getScrollY() > 0);
        }

    }

    protected void stopNestedScrollIfNeeded(int dy, T child, View target, int type) {
        if (type == 1) {
            int curOffset = this.getTopBottomOffsetForScrollingSibling();
            if (dy < 0 && curOffset == 0 ||
                    dy > 0 && curOffset == -child.getDownNestedScrollRange()) {
                ViewCompat.stopNestedScroll(target, 1);
            }
        }

    }

    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, T abl, View target,
                                   int type) {
        if (this.lastStartedType == 0 || type == 1) {
            log.debug("onStopNestedScroll snapToChildIfNeeded");
            this.snapToChildIfNeeded(coordinatorLayout, abl);
        }

        this.lastNestedScrollingChildRef = new WeakReference(target);
    }

    public void setDragCallback(@Nullable AppBarLayout.BaseBehavior.BaseDragCallback callback) {
        this.onDragCallback = callback;
    }

    private void animateOffsetTo(CoordinatorLayout coordinatorLayout, T child, int offset,
                                 float velocity) {
        int distance = Math.abs(this.getTopBottomOffsetForScrollingSibling() - offset);
        velocity = Math.abs(velocity);
        int duration;
        if (velocity > 0.0F) {
            duration = 3 * Math.round(1000.0F * ((float) distance / velocity));
        } else {
            float distanceRatio = (float) distance / (float) child.getHeight();
            duration = (int) ((distanceRatio + 1.0F) * 150.0F);
        }

        this.animateOffsetWithDuration(coordinatorLayout, child, offset, duration);
    }

    private void animateOffsetWithDuration(final CoordinatorLayout coordinatorLayout, final T child,
                                           int offset, int duration) {
        int currentOffset = this.getTopBottomOffsetForScrollingSibling();
        if (currentOffset == offset) {
            if (this.offsetAnimator != null && this.offsetAnimator.isRunning()) {
                this.offsetAnimator.cancel();
            }

        } else {
            if (this.offsetAnimator == null) {
                this.offsetAnimator = new ValueAnimator();
                this.offsetAnimator.setInterpolator(AnimationUtils.DECELERATE_INTERPOLATOR);
                this.offsetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animator) {
                        setHeaderTopBottomOffset(coordinatorLayout, child,
                                        (Integer) animator.getAnimatedValue());
                    }
                });
            } else {
                this.offsetAnimator.cancel();
            }

            this.offsetAnimator.setDuration((long) Math.min(duration, MAX_OFFSET_ANIMATION_DURATION));
            this.offsetAnimator.setIntValues(new int[]{currentOffset, offset});
            this.offsetAnimator.start();
        }
    }

    private int getChildIndexOnOffset(T abl, int offset) {
        int i = 0;

        for (int count = abl.getChildCount(); i < count; ++i) {
            View child = abl.getChildAt(i);
            int top = child.getTop();
            int bottom = child.getBottom();
            AppBarLayout.LayoutParams lp = (AppBarLayout.LayoutParams) child.getLayoutParams();
            if (checkFlag(lp.getScrollFlags(), AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP_MARGINS)) {
                top -= lp.topMargin;
                bottom += lp.bottomMargin;
            }

            if (top <= -offset && bottom >= -offset) {
                return i;
            }
        }

        return -1;
    }

    private void snapToChildIfNeeded(CoordinatorLayout coordinatorLayout, T abl) {
        int offset = this.getTopBottomOffsetForScrollingSibling();
        int offsetChildIndex = this.getChildIndexOnOffset(abl, offset);
        if (offsetChildIndex >= 0) {
            View offsetChild = abl.getChildAt(offsetChildIndex);
            AppBarLayout.LayoutParams lp =
                    (AppBarLayout.LayoutParams) offsetChild.getLayoutParams();
            int flags = lp.getScrollFlags();
            if ((flags & AppBarLayout.LayoutParams.FLAG_SNAP) == AppBarLayout.LayoutParams.FLAG_SNAP) {
                int snapTop = -offsetChild.getTop();
                int snapBottom = -offsetChild.getBottom();
                if (offsetChildIndex == abl.getChildCount() - 1) {
                    snapBottom += abl.getTopInset();
                }

                int seam;
                if (checkFlag(flags, AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED)) {
                    snapBottom += ViewCompat.getMinimumHeight(offsetChild);
                } else if (checkFlag(flags, AppBarLayout.LayoutParams.FLAG_QUICK_RETURN)) {
                    seam = snapBottom + ViewCompat.getMinimumHeight(offsetChild);
                    if (offset < seam) {
                        snapTop = seam;
                    } else {
                        snapBottom = seam;
                    }
                }

                if (checkFlag(flags, AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP_MARGINS)) {
                    snapTop += lp.topMargin;
                    snapBottom -= lp.bottomMargin;
                }

                seam = offset < (snapBottom + snapTop) / 2 ? snapBottom : snapTop;
                this.animateOffsetTo(coordinatorLayout, abl, android.support.v4.math.MathUtils
                        .clamp(seam, -abl.getTotalScrollRange(), 0), 0.0F);
            }
        }

    }

    private static boolean checkFlag(int flags, int check) {
        return (flags & check) == check;
    }

    public boolean onMeasureChild(CoordinatorLayout parent, T child, int parentWidthMeasureSpec,
                                  int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        android.support.design.widget.CoordinatorLayout.LayoutParams lp =
                (android.support.design.widget.CoordinatorLayout.LayoutParams) child
                        .getLayoutParams();
        if (lp.height == AppBarLayout.LayoutParams.WRAP_CONTENT) {
            parent.onMeasureChild(child, parentWidthMeasureSpec, widthUsed, View.MeasureSpec
                    .makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED), heightUsed);
            return true;
        } else {
            return super.onMeasureChild(parent, child, parentWidthMeasureSpec, widthUsed,
                    parentHeightMeasureSpec, heightUsed);
        }
    }

    public boolean onLayoutChild(CoordinatorLayout parent, T abl, int layoutDirection) {
        boolean handled = super.onLayoutChild(parent, abl, layoutDirection);
        int pendingAction = abl.getPendingAction();
        int offset;
        if (this.offsetToChildIndexOnLayout >= 0 && (pendingAction & AppBarLayout.PENDING_ACTION_FORCE) == 0) {
            View child = abl.getChildAt(this.offsetToChildIndexOnLayout);
            offset = -child.getBottom();
            if (this.offsetToChildIndexOnLayoutIsMinHeight) {
                offset += ViewCompat.getMinimumHeight(child) + abl.getTopInset();
            } else {
                offset +=
                        Math.round((float) child.getHeight() * this.offsetToChildIndexOnLayoutPerc);
            }

            this.setHeaderTopBottomOffset(parent, abl, offset);
        } else if (pendingAction != AppBarLayout.PENDING_ACTION_NONE) {
            boolean animate = (pendingAction & AppBarLayout.PENDING_ACTION_ANIMATE_ENABLED) != 0;
            if ((pendingAction & AppBarLayout.PENDING_ACTION_COLLAPSED) != 0) {
                offset = -abl.getUpNestedPreScrollRange();
                if (animate) {
                    this.animateOffsetTo(parent, abl, offset, 0.0F);
                } else {
                    this.setHeaderTopBottomOffset(parent, abl, offset);
                }
            } else if ((pendingAction & AppBarLayout.PENDING_ACTION_EXPANDED) != 0) {
                if (animate) {
                    this.animateOffsetTo(parent, abl, 0, 0.0F);
                } else {
                    this.setHeaderTopBottomOffset(parent, abl, 0);
                }
            }
        }

        abl.resetPendingAction();
        this.offsetToChildIndexOnLayout = -1;
        this.setTopAndBottomOffset(android.support.v4.math.MathUtils
                .clamp(this.getTopAndBottomOffset(), -abl.getTotalScrollRange(), 0));
        this.updateAppBarLayoutDrawableState(parent, abl, this.getTopAndBottomOffset(), 0, true);
        abl.dispatchOffsetUpdates(this.getTopAndBottomOffset());
        return handled;
    }

    boolean canDragView(T view) {
        if (this.onDragCallback != null) {
            return this.onDragCallback.canDrag(view);
        } else if (this.lastNestedScrollingChildRef == null) {
            return true;
        } else {
            View scrollingView = (View) this.lastNestedScrollingChildRef.get();
            return scrollingView != null && scrollingView.isShown() &&
                    !scrollingView.canScrollVertically(-1);
        }
    }

    void onFlingFinished(CoordinatorLayout parent, T layout) {
        this.snapToChildIfNeeded(parent, layout);
    }

    int getMaxDragOffset(T view) {
        return -view.getDownNestedScrollRange();
    }

    int getScrollRangeForDragFling(T view) {
        return view.getTotalScrollRange();
    }

    int setHeaderTopBottomOffset(CoordinatorLayout coordinatorLayout, T appBarLayout, int newOffset,
                                 int minOffset, int maxOffset) {
        int curOffset = this.getTopBottomOffsetForScrollingSibling();
        int consumed = 0;
        if (minOffset != 0 && curOffset >= minOffset && curOffset <= maxOffset) {
            newOffset = MathUtils.clamp(newOffset, minOffset, maxOffset);
            if (curOffset != newOffset) {
                int interpolatedOffset = appBarLayout.hasChildWithInterpolator() ?
                        this.interpolateOffset(appBarLayout, newOffset) : newOffset;
                boolean offsetChanged = this.setTopAndBottomOffset(interpolatedOffset);
                consumed = curOffset - newOffset;
                this.offsetDelta = newOffset - interpolatedOffset;
                if (!offsetChanged && appBarLayout.hasChildWithInterpolator()) {
                    coordinatorLayout.dispatchDependentViewsChanged(appBarLayout);
                }

                appBarLayout.dispatchOffsetUpdates(this.getTopAndBottomOffset());
                this.updateAppBarLayoutDrawableState(coordinatorLayout, appBarLayout, newOffset,
                        newOffset < curOffset ? -1 : 1, false);
            }
        } else {
            this.offsetDelta = 0;
        }

        return consumed;
    }

    @VisibleForTesting
    boolean isOffsetAnimatorRunning() {
        return this.offsetAnimator != null && this.offsetAnimator.isRunning();
    }

    private int interpolateOffset(T layout, int offset) {
        int absOffset = Math.abs(offset);
        int i = 0;

        for (int z = layout.getChildCount(); i < z; ++i) {
            View child = layout.getChildAt(i);
            AppBarLayout.LayoutParams childLp = (AppBarLayout.LayoutParams) child.getLayoutParams();
            Interpolator interpolator = childLp.getScrollInterpolator();
            if (absOffset >= child.getTop() && absOffset <= child.getBottom()) {
                if (interpolator != null) {
                    int childScrollableHeight = 0;
                    int flags = childLp.getScrollFlags();
                    if ((flags & AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
                        childScrollableHeight +=
                                child.getHeight() + childLp.topMargin + childLp.bottomMargin;
                        if ((flags & AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
                            childScrollableHeight -= ViewCompat.getMinimumHeight(child);
                        }
                    }

                    if (ViewCompat.getFitsSystemWindows(child)) {
                        childScrollableHeight -= layout.getTopInset();
                    }

                    if (childScrollableHeight > 0) {
                        int offsetForView = absOffset - child.getTop();
                        int interpolatedDiff = Math.round((float) childScrollableHeight *
                                interpolator.getInterpolation(
                                        (float) offsetForView / (float) childScrollableHeight));
                        return Integer.signum(offset) * (child.getTop() + interpolatedDiff);
                    }
                }
                break;
            }
        }

        return offset;
    }

    private void updateAppBarLayoutDrawableState(CoordinatorLayout parent, T layout, int offset,
                                                 int direction, boolean forceJump) {
        View child = getAppBarChildOnOffset(layout, offset);
        if (child != null) {
            AppBarLayout.LayoutParams childLp = (AppBarLayout.LayoutParams) child.getLayoutParams();
            int flags = childLp.getScrollFlags();
            boolean lifted = false;
            if ((flags & AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
                int minHeight = ViewCompat.getMinimumHeight(child);
                if (direction > 0 && (flags & AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
                        | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED) != 0) {
                    lifted = -offset >= child.getBottom() - minHeight - layout.getTopInset();
                } else if ((flags & AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
                    lifted = -offset >= child.getBottom() - minHeight - layout.getTopInset();
                }
            }

            if (layout.isLiftOnScroll()) {
                View scrollingChild = this.findFirstScrollingChild(parent);
                if (scrollingChild != null) {
                    lifted = scrollingChild.getScrollY() > 0;
                }
            }

            boolean changed = layout.setLiftedState(lifted);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB &&
                    (forceJump || changed && this.shouldJumpElevationState(parent, layout))) {
                layout.jumpDrawablesToCurrentState();
            }
        }

    }

    private boolean shouldJumpElevationState(CoordinatorLayout parent, T layout) {
        List<View> dependencies = parent.getDependents(layout);
        int i = 0;

        for (int size = dependencies.size(); i < size; ++i) {
            View dependency = (View) dependencies.get(i);
            android.support.design.widget.CoordinatorLayout.LayoutParams lp =
                    (android.support.design.widget.CoordinatorLayout.LayoutParams) dependency
                            .getLayoutParams();
            android.support.design.widget.CoordinatorLayout.Behavior behavior = lp.getBehavior();
            if (behavior instanceof AppBarLayout.ScrollingViewBehavior) {
                return ((AppBarLayout.ScrollingViewBehavior) behavior).getOverlayTop() != 0;
            }
        }

        return false;
    }

    private static View getAppBarChildOnOffset(AppBarLayout layout, int offset) {
        int absOffset = Math.abs(offset);
        int i = 0;

        for (int z = layout.getChildCount(); i < z; ++i) {
            View child = layout.getChildAt(i);
            if (absOffset >= child.getTop() && absOffset <= child.getBottom()) {
                return child;
            }
        }

        return null;
    }

    @Nullable
    private View findFirstScrollingChild(CoordinatorLayout parent) {
        int i = 0;

        for (int z = parent.getChildCount(); i < z; ++i) {
            View child = parent.getChildAt(i);
            if (child instanceof NestedScrollingChild) {
                return child;
            }
        }

        return null;
    }

    int getTopBottomOffsetForScrollingSibling() {
        return this.getTopAndBottomOffset() + this.offsetDelta;
    }

    public Parcelable onSaveInstanceState(CoordinatorLayout parent, T abl) {
        Parcelable superState = super.onSaveInstanceState(parent, abl);
        int offset = this.getTopAndBottomOffset();
        int i = 0;

        for (int count = abl.getChildCount(); i < count; ++i) {
            View child = abl.getChildAt(i);
            int visBottom = child.getBottom() + offset;
            if (child.getTop() + offset <= 0 && visBottom >= 0) {
                AppBarLayout.BaseBehavior.SavedState ss =
                        new AppBarLayout.BaseBehavior.SavedState(superState);
                ss.firstVisibleChildIndex = i;
                ss.firstVisibleChildAtMinimumHeight =
                        visBottom == ViewCompat.getMinimumHeight(child) + abl.getTopInset();
                ss.firstVisibleChildPercentageShown = (float) visBottom / (float) child.getHeight();
                return ss;
            }
        }

        return superState;
    }

    public void onRestoreInstanceState(CoordinatorLayout parent, T appBarLayout, Parcelable state) {
        if (state instanceof AppBarLayout.BaseBehavior.SavedState) {
            AppBarLayout.BaseBehavior.SavedState ss = (AppBarLayout.BaseBehavior.SavedState) state;
            super.onRestoreInstanceState(parent, appBarLayout, ss.getSuperState());
            this.offsetToChildIndexOnLayout = ss.firstVisibleChildIndex;
            this.offsetToChildIndexOnLayoutPerc = ss.firstVisibleChildPercentageShown;
            this.offsetToChildIndexOnLayoutIsMinHeight = ss.firstVisibleChildAtMinimumHeight;
        } else {
            super.onRestoreInstanceState(parent, appBarLayout, state);
            this.offsetToChildIndexOnLayout = -1;
        }

    }

    protected static class SavedState extends AbsSavedState {
        int firstVisibleChildIndex;
        float firstVisibleChildPercentageShown;
        boolean firstVisibleChildAtMinimumHeight;
        public static final Creator<AppBarLayout.BaseBehavior.SavedState> CREATOR =
                new ClassLoaderCreator<AppBarLayout.BaseBehavior.SavedState>() {
                    public AppBarLayout.BaseBehavior.SavedState createFromParcel(Parcel source,
                                                                                 ClassLoader loader) {
                        return new AppBarLayout.BaseBehavior.SavedState(source, loader);
                    }

                    public AppBarLayout.BaseBehavior.SavedState createFromParcel(Parcel source) {
                        return new AppBarLayout.BaseBehavior.SavedState(source, (ClassLoader) null);
                    }

                    public AppBarLayout.BaseBehavior.SavedState[] newArray(int size) {
                        return new AppBarLayout.BaseBehavior.SavedState[size];
                    }
                };

        public SavedState(Parcel source, ClassLoader loader) {
            super(source, loader);
            this.firstVisibleChildIndex = source.readInt();
            this.firstVisibleChildPercentageShown = source.readFloat();
            this.firstVisibleChildAtMinimumHeight = source.readByte() != 0;
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.firstVisibleChildIndex);
            dest.writeFloat(this.firstVisibleChildPercentageShown);
            dest.writeByte((byte) (this.firstVisibleChildAtMinimumHeight ? 1 : 0));
        }
    }

    public abstract static class BaseDragCallback<T extends AppBarLayout> {
        public BaseDragCallback() {
        }

        public abstract boolean canDrag(@NonNull T var1);
    }
}
