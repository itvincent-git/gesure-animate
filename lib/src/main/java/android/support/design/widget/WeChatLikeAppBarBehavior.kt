package android.support.design.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import net.slog.SLoggerFactory

/**
 * 实现相似微信2楼的交互效果，这是2楼使用的AppBarLayout使用的Behavior
 * Created by zhongyongsheng on 2020-01-02.
 */
class WeChatLikeAppBarBehavior @JvmOverloads constructor(
    context: Context? = null, attrs: AttributeSet? = null
) : WeChatBaseBehavior<AppBarLayout>() {

//    override fun onFlingFinished(parent: CoordinatorLayout?, layout: AppBarLayout?) {
//        log.debug("onFlingFinished")
//    }

//    override fun onStartNestedScroll(
//        parent: CoordinatorLayout, child: AppBarLayout, directTargetChild: View, target: View, nestedScrollAxes: Int,
//        type: Int
//    ): Boolean {
//        log.debug(
//            "onStartNestedScroll ${child.javaClass} ${directTargetChild.javaClass} ${target.javaClass} $nestedScrollAxes $type")
//        return super.onStartNestedScroll(parent, child, directTargetChild, target, nestedScrollAxes, type)
//    }

//    override fun onNestedPreScroll(
//        coordinatorLayout: CoordinatorLayout, child: AppBarLayout, target: View, dx: Int, dy: Int, consumed: IntArray,
//        type: Int
//    ) {
//        log.debug(
//            "onNestedPreScroll ${child.javaClass} ${target.javaClass} $dx $dy [${consumed.joinToString()}] $type")
//        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
//    }

//    override fun onNestedScroll(
//        coordinatorLayout: CoordinatorLayout, child: AppBarLayout, target: View, dxConsumed: Int, dyConsumed: Int,
//        dxUnconsumed: Int, dyUnconsumed: Int, type: Int
//    ) {
//        log.debug("onNestedScroll $dyUnconsumed")
//        if (dyUnconsumed < 0) {
//            scroll(coordinatorLayout, child, dyUnconsumed, -child.downNestedScrollRange,
//                0)
//            stopNestedScrollIfNeeded(dyUnconsumed, child, target, type)
//        }
//
//        if (child.isLiftOnScroll) {
//            child.setLiftedState(target.scrollY > 0)
//        }
//    }

    companion object {
        private val log = SLoggerFactory.getLogger("WeChatLikeAppBarBehavior")
    }
}