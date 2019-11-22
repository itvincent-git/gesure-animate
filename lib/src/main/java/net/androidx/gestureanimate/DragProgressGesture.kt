package net.androidx.gestureanimate

import android.content.Context
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import net.slog.SLoggerFactory
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * 提供拖动手势的处理能力，支持横行/纵向的拖动
 * Created by zhongyongsheng on 2019-11-20.
 */
class DragProgressGesture constructor(
    private val context: Context,
    private val callback: DragProgressCallback
) {

    companion object {
        private val log = SLoggerFactory.getLogger("SwipeGestureHandler")
    }

    private var velocityTracker: VelocityTracker? = null
    private val minimumFlingVelocity = ViewConfiguration.getMinimumFlingVelocity().toFloat()
    private val maximumFlingVelocity = ViewConfiguration.getMaximumFlingVelocity().toFloat()
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var activePointerId = MotionEvent.INVALID_POINTER_ID
    private var dragStarted = false

    /**
     * 调用点击事件的处理逻辑
     */
    fun onTouchEvent(event: MotionEvent): Boolean {
        var pos = 0f
        var movementDirection = 0f
        var change = 0f
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // Remember where we started (for dragging)
                val pointerIndex = event.actionIndex
                lastTouchX = event.getX(pointerIndex)
                lastTouchY = event.getY(pointerIndex)
                activePointerId = event.getPointerId(pointerIndex)
                //stop drag
                dragStarted = false

                // Reset the velocity tracker back to its initial state.
                velocityTracker?.clear()
                // If necessary retrieve a new VelocityTracker object to watch the
                // velocity of a motion.
                velocityTracker = velocityTracker ?: VelocityTracker.obtain()
                // Add a user's movement to the tracker.
                velocityTracker?.addMovement(event)
            }
            MotionEvent.ACTION_MOVE -> {
                // Find the index of the active pointer and fetch its position
                val (x: Float, y: Float) =
                    event.findPointerIndex(activePointerId).let { pointerIndex ->
                        // Calculate the distance moved
                        event.getX(pointerIndex) to
                            event.getY(pointerIndex)
                    }

                val scrollX = x - lastTouchX
                val scrollY = y - lastTouchY

                velocityTracker?.apply {
                    val pointerId = event.getPointerId(event.actionIndex)
                    addMovement(event)
                    // When you want to determine the velocity, call
                    // computeCurrentVelocity(). Then call getXVelocity()
                    // and getYVelocity() to retrieve the velocity for each pointer ID.
                    computeCurrentVelocity(1000, maximumFlingVelocity)
                    // Log velocity of pixels per second
                    // Best practice to use VelocityTrackerCompat where possible.
                    val xVelocity = getXVelocity(pointerId)
                    val yVelocity = getYVelocity(pointerId)

                    val dragVelocity =
                        if (callback.getMovementDirection() == MovementDirection.Horizontal) {
                            xVelocity
                        } else {
                            yVelocity
                        }

                    //如果拖动的速度足够或已经在拖动中
                    if (abs(dragVelocity) > 10f || dragStarted) {
                        pos = callback.getCurrentProgress()
                        if (!dragStarted) {
                            dragStarted = true
                        }

                        movementDirection = callback.getMovementDistance()
                        //根据方向计算拖动的比例
                        change =
                            if (callback.getMovementDirection() == MovementDirection.Horizontal) {
                                scrollX / movementDirection
                            } else {
                                scrollY / movementDirection
                            }
                        //加入到当前的比例中
                        pos = max(min(pos + change, 1.0f), 0.0f)
                        callback.onProgressChange(pos)
                    }
                }

                // Remember this touch position for the next move event
                lastTouchX = x
                lastTouchY = y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {

                velocityTracker?.apply {
                    //计算速度
                    computeCurrentVelocity(1000, maximumFlingVelocity)
                    val pointerId = event.getPointerId(event.actionIndex)
                    //根据方向取速度
                    val velocity =
                        if (callback.getMovementDirection() == MovementDirection.Horizontal) {
                            getXVelocity(pointerId)
                        } else {
                            getYVelocity(pointerId)
                        }
                    change = callback.getCurrentProgress()
                    //按照速度计算松开后还要滑动的比例
                    val velocityToMove = velocity / callback.getMovementDistance()
                    if (!velocityToMove.isNaN()) {
                        change += velocityToMove / 3f
                    }
                    //百分比过半则做动画到结束，否则返回开始
                    if (change != 0f && change != 1f) {
                        if (change < 0.5f) {
                            callback.onAnimateToStart()
                        } else {
                            callback.onAnimateToEnd()
                        }
                    }
                }

                activePointerId = MotionEvent.INVALID_POINTER_ID
                // Return a VelocityTracker object back to be re-used by others.
                velocityTracker?.recycle()
                velocityTracker = null
                dragStarted = false
            }
            MotionEvent.ACTION_POINTER_UP -> {

                event.actionIndex.also { pointerIndex ->
                    event.getPointerId(pointerIndex)
                        .takeIf { it == activePointerId }
                        ?.run {
                            // This was our active pointer going up. Choose a new
                            // active pointer and adjust accordingly.
                            val newPointerIndex = if (pointerIndex == 0) 1 else 0
                            lastTouchX = event.getX(newPointerIndex)
                            lastTouchY = event.getY(newPointerIndex)
                            activePointerId = event.getPointerId(newPointerIndex)
                        }
                }
            }
        }
        return false
    }
}

interface DragProgressCallback {

    fun getCurrentProgress(): Float

    fun getMovementDistance(): Float

    fun getMovementDirection(): MovementDirection

    fun onProgressChange(progress: Float)

    fun onAnimateToStart()

    fun onAnimateToEnd()
}

enum class MovementDirection { Horizontal, Vertical }

