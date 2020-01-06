package android.support.design.widget

import android.content.Context
import android.util.AttributeSet
import net.slog.SLoggerFactory

/**
 * 实现相似微信2楼的交互效果，这是2楼使用的AppBarLayout使用的Behavior
 * Created by zhongyongsheng on 2020-01-02.
 */
class WeChatLikeAppBarBehavior @JvmOverloads constructor(
    context: Context? = null, attrs: AttributeSet? = null
) : AppBarLayout.Behavior() {

    override fun onFlingFinished(parent: CoordinatorLayout?, layout: AppBarLayout?) {
        log.debug("onFlingFinished")
    }

    companion object {
        private val log = SLoggerFactory.getLogger("WeChatLikeAppBarBehavior")
    }
}