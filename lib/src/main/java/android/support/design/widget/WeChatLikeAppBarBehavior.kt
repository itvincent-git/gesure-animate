package android.support.design.widget

import android.content.Context
import android.support.annotation.Keep
import android.util.AttributeSet
import android.view.View
import net.slog.SLoggerFactory
import kotlin.math.absoluteValue

/**
 * 实现相似微信2楼的交互效果，这是2楼使用的AppBarLayout使用的Behavior
 * Created by zhongyongsheng on 2020-01-02.
 */
@Keep
open class WeChatLikeAppBarBehavior @JvmOverloads constructor(
    context: Context? = null, attrs: AttributeSet? = null
) : WeChatBaseBehavior<AppBarLayout>() {
    var allowScroll = true

    override fun onStartNestedScroll(
        parent: CoordinatorLayout, child: AppBarLayout, directTargetChild: View, target: View, nestedScrollAxes: Int,
        type: Int
    ): Boolean {
        if (!allowScroll) return false
        return super.onStartNestedScroll(parent, child, directTargetChild, target, nestedScrollAxes, type)
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout, child: AppBarLayout, target: View, dxConsumed: Int, dyConsumed: Int,
        dxUnconsumed: Int, dyUnconsumed: Int, type: Int
    ) {
        //处理向下滚动的逻辑
        if (dyUnconsumed < 0) {
            //recyclerview滚到顶部fling的时候则不触发appbar的滚动
            if (topAndBottomOffset.absoluteValue != child.downNestedScrollRange.absoluteValue || type == 0) {
                if (log.isDebugEnable) log.debug("onNestedScroll [x:%d %d] [y:%d %d] type:%d", dxConsumed, dxUnconsumed,
                    dyConsumed, dyUnconsumed, type)
                scroll(coordinatorLayout, child, dyUnconsumed, -child.getDownNestedScrollRange(), 0)
                stopNestedScrollIfNeeded(dyUnconsumed, child, target, type)
            }
        }
        if (child.isLiftOnScroll()) {
            child.setLiftedState(target.scrollY > 0)
        }
    }

    companion object {
        private val log = SLoggerFactory.getLogger("WeChatLikeAppBarBehavior")
    }
}