package android.support.design.widget

import android.content.Context
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import net.slog.SLoggerFactory
import kotlin.math.absoluteValue

/**
 * 实现往下拉的阻尼效果
 */
class ChangeInterceptorListener(val context: Context, val layoutParam: AppBarLayout.LayoutParams) :
    AppBarLayout.OnOffsetChangedListener {
    private var lastOffset = 0
    val interpolator: Interpolator = DecelerateInterpolator(1.5f)

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        if (lastOffset != verticalOffset) {
            lastOffset = verticalOffset
            val scrollRange = appBarLayout.totalScrollRange
            if (verticalOffset.absoluteValue == scrollRange) {
                // appbar最小化
                if (log.isDebugEnable) log.debug("onAppBarMinimize")
                layoutParam.scrollInterpolator = interpolator
            } else if (verticalOffset.absoluteValue == 0) {
                // appbar最大化
                if (log.isDebugEnable) log.debug("onAppBarMaximize")
                layoutParam.scrollInterpolator = null
            }
        }
    }

    companion object {
        private val log = SLoggerFactory.getLogger("ChangeInterceptorListener")
    }
}