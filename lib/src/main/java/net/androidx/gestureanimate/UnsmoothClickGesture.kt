package net.androidx.gestureanimate

import android.content.Context
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import net.slog.SLoggerFactory
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 提供不能滑动的点击事件。当Down发生时，如果有距离远而且速度快的MOVE，则不触发click事件
 * Created by zhongyongsheng on 2019-11-20.
 */
class UnsmoothClickGesture constructor(
    private val context: Context,
    private val callback: UnsmoothClickCallback
) {

    companion object {
        private val log = SLoggerFactory.getLogger("UnsmoothClickGesture")
    }

    private var velocityTracker: VelocityTracker? = null
    private val minimumFlingVelocity = ViewConfiguration.getMinimumFlingVelocity().toFloat()
    private val maximumFlingVelocity = ViewConfiguration.getMaximumFlingVelocity().toFloat()

    /**
     * 调用点击事件的处理逻辑
     */
    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // Reset the velocity tracker back to its initial state.
                velocityTracker?.clear()
                // If necessary retrieve a new VelocityTracker object to watch the
                // velocity of a motion.
                velocityTracker = velocityTracker ?: VelocityTracker.obtain()
                // Add a user's movement to the tracker.
                velocityTracker?.addMovement(event)
            }
            MotionEvent.ACTION_MOVE -> {

                velocityTracker?.apply {
                    addMovement(event)
                    // When you want to determine the velocity, call
                    // computeCurrentVelocity(). Then call getXVelocity()
                    // and getYVelocity() to retrieve the velocity for each pointer ID.
                    computeCurrentVelocity(1000, maximumFlingVelocity)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {

                velocityTracker?.apply {
                    computeCurrentVelocity(1000, maximumFlingVelocity)
                    val pointerId = event.getPointerId(event.actionIndex)
                    val velocity = sqrt(getXVelocity(pointerId).toDouble().pow(2.0) + getYVelocity(
                        pointerId).toDouble().pow(2.0))
                    //如果速度超过vel或者距离超过dis则不分发点击

                    if (velocity < minimumFlingVelocity) {
                        callback.onUnsmoothClick(event)
                    }
                }

                // Return a VelocityTracker object back to be re-used by others.
                velocityTracker?.recycle()
                velocityTracker = null
            }
        }
        return true
    }
}

interface UnsmoothClickCallback {

    fun onUnsmoothClick(event: MotionEvent)
}
