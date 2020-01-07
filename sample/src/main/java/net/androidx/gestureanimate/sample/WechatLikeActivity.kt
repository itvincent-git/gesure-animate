package net.androidx.gestureanimate.sample

import android.content.Context
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v7.app.AppCompatActivity
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import kotlinx.android.synthetic.main.activity_wechat_like.app_bar
import kotlinx.android.synthetic.main.layout_content_scrolling.large_tv
import kotlinx.android.synthetic.main.layout_coordinator_header.motionLayout
import net.androidx.gestureanimate.util.createRandomString
import net.slog.SLoggerFactory
import kotlin.math.absoluteValue

class WechatLikeActivity : AppCompatActivity() {

    companion object {
        private val log = SLoggerFactory.getLogger("WechatLikeActivity")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wechat_like)

        large_tv.text = createRandomString(1024 * 8)

        val headerLayoutParam = motionLayout.layoutParams as AppBarLayout.LayoutParams
//        headerLayoutParam.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams
//            .SCROLL_FLAG_EXIT_UNTIL_COLLAPSED or AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP

        app_bar.addOnOffsetChangedListener(ChangeInterceptorListener(this, headerLayoutParam))
    }
}

/**
 * 实现往下拉的阻尼效果
 */
class ChangeInterceptorListener(val context: Context, val layoutParam: AppBarLayout.LayoutParams) :
    AppBarLayout.OnOffsetChangedListener {
    private var lastOffset = 0
    val interpolator: Interpolator = DecelerateInterpolator(1.8f)

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        if (lastOffset != verticalOffset) {
            lastOffset = verticalOffset
            val scrollRange = appBarLayout.totalScrollRange
            if (verticalOffset.absoluteValue == scrollRange) {
                // appbar最小化
                log.debug("onAppBarMinimize")
                layoutParam.scrollInterpolator = interpolator
            } else if (verticalOffset.absoluteValue == 0) {
                // appbar最大化
                log.debug("onAppBarMaximize")
                layoutParam.scrollInterpolator = null
            }
        }
    }

    companion object {
        private val log = SLoggerFactory.getLogger("ChangeInterceptorListener")
    }
}
