package android.support.design.widget

import android.content.Context
import android.support.constraint.motion.MotionLayout
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import net.slog.SLoggerFactory

/**
 * 将AppBarLayout滚动事件，触发为MotionLayout.progress的变化
 * AppBarLayout滑动向上，则verticalOffset为负数并且越来越小，progress则由0向1变化
 * @author zhongyongsheng
 */
private val log = SLoggerFactory.getLogger("MotionalAppBarContent")
class MotionalAppBarContent @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MotionLayout(context, attrs, defStyleAttr), AppBarLayout.OnOffsetChangedListener {

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        super.onNestedPreScroll(target, dx, dy, consumed)
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        progress = -verticalOffset / appBarLayout?.totalScrollRange?.toFloat()!!
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val appBarLayout = findAppBarLayout()
        log.debug("found appBarLayout $appBarLayout")
        appBarLayout?.addOnOffsetChangedListener(this)
    }

    protected fun findAppBarLayout(): AppBarLayout? {
        val _parent = parent
        if (_parent is AppBarLayout) {
            return _parent
        } else if (_parent is CoordinatorLayout) {
            return _parent.findChild<AppBarLayout> { it is AppBarLayout }
        }
        return null
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
}