package net.androidx.gestureanimate

import android.content.Context
import android.support.constraint.motion.MotionLayout
import android.support.design.widget.AppBarLayout
import android.util.AttributeSet
import net.slog.SLoggerFactory

/**
 * 将AppBarLayout滚动事件，触发为MotionLayout.progress的变化
 * AppBarLayout滑动向上，则verticalOffset为负数并且越来越小，progress则由0向1变化
 * @author zhongyongsheng
 */
private val log = SLoggerFactory.getLogger("CollapsibleToolbar")
class CollapsibleToolbar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MotionLayout(context, attrs, defStyleAttr), AppBarLayout.OnOffsetChangedListener {

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        progress = -verticalOffset / appBarLayout?.totalScrollRange?.toFloat()!!
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        (parent as? AppBarLayout)?.addOnOffsetChangedListener(this)
    }
}