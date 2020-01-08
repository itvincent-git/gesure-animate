package android.support.design.widget

import android.content.Context
import android.support.annotation.Keep
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import net.slog.SLoggerFactory

/**
 * 实现相似微信2楼的交互效果，这是1楼ScrollingView/RecycleView使用的Behavior
 * Created by zhongyongsheng on 2020-01-02.
 */
@Keep
class WeChatLikeScrollingViewBehavior @JvmOverloads constructor(
    context: Context? = null, attrs: AttributeSet? = null
) : WeChatScrollingViewBehavior() {

    //把ablBehavior.offsetDelta去掉了，实现微信的效果
    override fun offsetChildAsNeeded(child: View, dependency: View) {
        val behavior = (dependency
            .layoutParams as CoordinatorLayout.LayoutParams).behavior
        if (behavior is WeChatBaseBehavior<*>) {
            ViewCompat.offsetTopAndBottom(child,
                dependency.bottom - child.top +
                    verticalLayoutGap -
                    getOverlapPixelsForOffset(dependency))
        }
    }

    companion object {
        private val log = SLoggerFactory.getLogger("WeChatLikeScrollingViewBehavior")
    }
}