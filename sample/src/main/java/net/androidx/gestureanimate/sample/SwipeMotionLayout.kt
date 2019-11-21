package net.androidx.gestureanimate.sample

import android.content.Context
import android.support.constraint.motion.MotionLayout
import android.util.AttributeSet
import android.view.MotionEvent
import net.androidx.gestureanimate.DragProgressGesture
import net.androidx.gestureanimate.DragProgressCallback
import net.androidx.gestureanimate.MovementDirection
import net.slog.SLoggerFactory

/**
 * 支持左右滑动做动画的MotionLayout
 * Created by zhongyongsheng on 2019-11-20.
 */
class SwipeMotionLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MotionLayout(context, attrs, defStyleAttr) {

    private val callback = object : DragProgressCallback {

        override fun getCurrentProgress(): Float {
            return progress
        }

        override fun getMovementDistance(): Float {
            return if (getMovementDirection() == MovementDirection.Horizontal) {
                width.toFloat()
            } else {
                height.toFloat()
            }
        }

        override fun getMovementDirection(): MovementDirection {
            return MovementDirection.Horizontal
        }

        override fun onProgressChange(value: Float) {
            log.debug("onProgressChange $progress")
            progress = value
        }

        override fun onAnimateToStart() {
            transitionToStart()
        }

        override fun onAnimateToEnd() {
            transitionToEnd()
        }
    }
    private val gesture = DragProgressGesture(context, callback)

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        //log.debug("onInterceptTouchEvent $event")
        //根据flingTouchListener决定是否拦截事件
        return if (this.gesture.onTouchEvent(event)) {
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