package net.androidx.gestureanimate.sample

import android.content.Context
import android.support.constraint.motion.MotionLayout
import android.util.AttributeSet
import android.view.MotionEvent
import net.androidx.gestureanimate.GestureHandler
import net.androidx.gestureanimate.ScrollListener
import net.slog.SLoggerFactory

/**
 * 支持左右滑动做动画的MotionLayout
 * Created by zhongyongsheng on 2019-11-20.
 */
class SwipeMotionLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MotionLayout(context, attrs, defStyleAttr) {

    private val listener = object : ScrollListener {
        override fun onScroll(
            e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float, currentX: Float,
            currentY: Float
        ) {
            val w = width
            val xPercent = currentX / w
            log.debug("onScroll $xPercent")
            setInterpolatedProgress(xPercent)
        }

        override fun getProgress(): Float {
            return progress
        }

        override fun getMovementDistance(): Float {
            return width.toFloat()
        }

        override fun onProgressChange(value: Float) {
            log.debug("onProgressChange $progress")
            progress = value
        }
    }
    private val swipeGestureHandler = GestureHandler(context, listener)

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        //log.debug("onInterceptTouchEvent $event")
        //根据flingTouchListener决定是否拦截事件
        return if (this.swipeGestureHandler.onTouchEvent(event)) {
            true
        } else super.onInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        //log.debug("onTouchEvent $event")
        //避免子view都不处理down事件时，无法收到其余的事件
        return true
    }

    /*override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return super.dispatchTouchEvent(ev).apply {
            log.debug("dispatchTouchEvent $this $ev")
        }
    }*/

    companion object {
        val log = SLoggerFactory.getLogger("SwipeMotionLayout")
    }
}