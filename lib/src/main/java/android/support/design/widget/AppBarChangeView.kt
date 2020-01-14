package android.support.design.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import net.slog.SLoggerFactory

/**
 * 将AppBarLayout滚动事件，触发为MotionLayout.progress的变化
 * AppBarLayout滑动向上，则verticalOffset为负数并且越来越小，progress则由0向1变化
 * @author zhongyongsheng
 */
private val log = SLoggerFactory.getLogger("AppBarChangeView")

abstract class AppBarChangeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), AppBarLayout.OnOffsetChangedListener {

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        val progress = -verticalOffset / appBarLayout.totalScrollRange.toFloat()
        onProgressChange(progress)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        var appBarLayout = findAppBarLayout(parent)
        if (appBarLayout == null) {
            appBarLayout = findAppBarLayout(parent.parent)
        }
        log.debug("found appBarLayout $appBarLayout")
        appBarLayout?.addOnOffsetChangedListener(this)
    }

    private fun findAppBarLayout(viewParent: ViewParent) = when (viewParent) {
        is AppBarLayout -> viewParent
        is CoordinatorLayout -> viewParent.findChild<AppBarLayout> { it is AppBarLayout }
        else -> null
    }

    private fun <T> ViewGroup.findChild(predicate: (View) -> Boolean): T? {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child != null && predicate(child)) {
                return child as T
            }
            if (child is ViewGroup) {
                val found = child.findChild<T>(predicate)
                if (found != null) return found
            }
        }
        return null
    }

    abstract fun onProgressChange(progress: Float)
}