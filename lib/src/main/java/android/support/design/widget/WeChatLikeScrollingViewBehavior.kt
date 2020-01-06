package android.support.design.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import net.slog.SLoggerFactory

/**
 * 实现相似微信2楼的交互效果，这是1楼ScrollingView/RecycleView使用的Behavior
 * Created by zhongyongsheng on 2020-01-02.
 */
class WeChatLikeScrollingViewBehavior @JvmOverloads constructor(
    context: Context? = null, attrs: AttributeSet? = null
) : AppBarLayout.ScrollingViewBehavior() {

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout, child: View, directTargetChild: View, target: View, axes: Int, type: Int
    ): Boolean {
        log.debug(
            "onStartNestedScroll ${child.javaClass} ${directTargetChild.javaClass} ${target.javaClass} $axes $type")
        return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
//        return super.onDependentViewChanged(parent, child, dependency)
        return false
    }

    companion object {
        private val log = SLoggerFactory.getLogger("WeChatLikeScrollingViewBehavior")
    }
}