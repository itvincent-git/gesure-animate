package android.support.design.widget

import android.content.Context
import android.util.AttributeSet

/**
 * 将AppBarLayout滚动事件，改变alpha值
 * @author zhongyongsheng
 */
class AppBarChangeAlphaView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppBarChangeView(context, attrs, defStyleAttr) {


    override fun onProgressChange(progress: Float) {
        alpha = 1f - progress
    }
}