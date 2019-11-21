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
 * 提供滑动事件的处理能力
 * Created by zhongyongsheng on 2019-11-20.
 */
class GestureHandler constructor(
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
    private var currentDownEvent: MotionEvent? = null
    private var dragStarted = false

    /**
     * 调用点击事件的处理逻辑
     */
    fun onTouchEvent(event: MotionEvent): Boolean {
        log.debug("onTouchEvent $event")
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

                if (currentDownEvent != null) {
                    currentDownEvent!!.recycle()
                }
                currentDownEvent = MotionEvent.obtain(event)

                // Reset the velocity tracker back to its initial state.
                velocityTracker?.clear()
                // If necessary retrieve a new VelocityTracker object to watch the
                // velocity of a motion.
                velocityTracker = velocityTracker ?: VelocityTracker.obtain()
                // Add a user's movement to the tracker.
                velocityTracker?.addMovement(event)

                //stop drag
                dragStarted = false
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

                log.debug("move x:$scrollX y:$scrollY")
                //listener.onScroll(currentDownEvent, event, scrollX, scrollY, x, y)

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
                    log.debug("velocity x: $xVelocity y: $yVelocity")

                    val drag = xVelocity
                    if (abs(drag) > 10f || dragStarted) {
                        pos = callback.getCurrentProgress()
                        if (!dragStarted) {
                            dragStarted = true
                        }

                        movementDirection = callback.getMovementDistance()
                        change = scrollX / movementDirection
                        pos = max(min(pos + change, 1.0f), 0.0f)
                        callback.onProgressChange(pos)
                    }
                }

                // Remember this touch position for the next move event
                lastTouchX = x
                lastTouchY = y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                pos = velocityTracker?.xVelocity ?: 0f
                change = callback.getCurrentProgress()
                val velocityToMove = pos / callback.getMovementDistance()
                if (!velocityToMove.isNaN()) {
                    change += velocityToMove / 3f
                }
                if (change != 0f && change != 1f) {
                    if (change < 0.5f) {
                        callback.onAnimateToStart()
                    } else {
                        callback.onAnimateToEnd()
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

