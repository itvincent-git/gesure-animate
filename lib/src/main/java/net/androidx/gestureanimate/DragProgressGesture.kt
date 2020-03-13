package net.androidx.gestureanimate

import android.content.Context
import android.graphics.Rect
import android.util.SparseArray
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import net.androidx.gestureanimate.util.ScrollConfiguration
import net.slog.SLoggerFactory
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

/**
 * 提供拖动手势的处理能力，支持横行/纵向的拖动。
 * 暂不支持多点触摸划动。
 * Created by zhongyongsheng on 2019-11-20.
 */
class DragProgressGesture constructor(
    private val context: Context,
    private val callback: DragProgressCallback,
    private val edgeCallback: DragEdgeCallback?
) {

    companion object {
        /**
         * Edge flag indicating that the left edge should be affected.
         */
        const val EDGE_LEFT = 1 shl 0

        /**
         * Edge flag indicating that the right edge should be affected.
         */
        const val EDGE_RIGHT = 1 shl 1

        /**
         * Edge flag indicating that the top edge should be affected.
         */
        const val EDGE_TOP = 1 shl 2

        /**
         * Edge flag indicating that the bottom edge should be affected.
         */
        const val EDGE_BOTTOM = 1 shl 3

        /**
         * 触摸边缘的宽度
         */
        const val EDGE_SIZE = 20 // dp

        private val log = SLoggerFactory.getLogger("SwipeGestureHandler")
    }

    private var velocityTracker: VelocityTracker? = null
    private val minimumFlingVelocity = ViewConfiguration.getMinimumFlingVelocity().toFloat()
    private val maximumFlingVelocity = ViewConfiguration.getMaximumFlingVelocity().toFloat()
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var activePointerId = MotionEvent.INVALID_POINTER_ID
    private var dragStarted = false
    private val density = context.resources.displayMetrics.density
    private var edgesTouched = 0
    private var state = DragState.Idle
    private val scrollConfiguration = ScrollConfiguration(context) // 从系统及通过算法处理后的滚动参数
    private val initialMotionX = SparseArray<Float>() //记录点击开始时的X, Key为pointId
    private val initialMotionY = SparseArray<Float>() //记录点击开始时的Y, Key为pointId

    private fun getEdgesTouched(x: Int, y: Int): Int {
        var result = 0
        if (edgeCallback != null) {
            if (x < edgeCallback.getViewRect().left + edgeSize) result =
                result or EDGE_LEFT
            if (y < edgeCallback.getViewRect().top + edgeSize) result =
                result or EDGE_TOP
            if (x > edgeCallback.getViewRect().right - edgeSize) result =
                result or EDGE_RIGHT
            if (y > edgeCallback.getViewRect().bottom - edgeSize) result =
                result or EDGE_BOTTOM
        }
        return result
    }

    private fun resetVelocityTracker() {
        // Return a VelocityTracker object back to be re-used by others.
        velocityTracker?.recycle()
        velocityTracker = null
    }

    private fun stopDrag() {
        dragStarted = false
        if (state != DragState.Idle) {
            state = DragState.Idle
            callback.onDragStateChange(DragState.Idle)
        }
    }

    private fun startDrag() {
        if (!dragStarted) {
            dragStarted = true
            if (state != DragState.Dragging) {
                state = DragState.Dragging
                callback.onDragStateChange(DragState.Dragging)
            }
        }
    }

    private fun shouldInterceptEdge(edgeCallback: DragEdgeCallback?): Boolean {
        var shouldIntercept = false
        edgesTouched = getEdgesTouched(lastTouchX.toInt(), lastTouchY.toInt())
        if (edgesTouched and trackingEdges != 0 && edgeCallback != null) {
            shouldIntercept = edgeCallback.onEdgeTouched(edgesTouched and trackingEdges)
        }
        return shouldIntercept
    }

    private fun resetNewPointer(event: MotionEvent, newPointerIndex: Int) {
        lastTouchX = event.getX(newPointerIndex)
        lastTouchY = event.getY(newPointerIndex)
        activePointerId = event.getPointerId(newPointerIndex)

        initialMotionX.clear()
        initialMotionY.clear()
        //已经换了另外一个触摸点后，需要重新初始化当前的激活的点
        initialMotionX.put(activePointerId, lastTouchX)
        initialMotionY.put(activePointerId, lastTouchY)
    }

    private fun initDragState() {
        //stop drag
        dragStarted = false

        if (state != DragState.Start) {
            state = DragState.Start
            callback.onDragStateChange(DragState.Start)
        }
    }

    private fun initMotion(event: MotionEvent) {
        // Remember where we started (for dragging)
        val pointerIndex = event.actionIndex
        lastTouchX = event.getX(pointerIndex)
        lastTouchY = event.getY(pointerIndex)
        activePointerId = event.getPointerId(pointerIndex)
        initialMotionX.clear()
        initialMotionY.clear()
        initialMotionX.put(activePointerId, lastTouchX)
        initialMotionY.put(activePointerId, lastTouchY)
    }

    private fun initVelocityTracker(event: MotionEvent) {
        // Reset the velocity tracker back to its initial state.
        velocityTracker?.clear()
        // If necessary retrieve a new VelocityTracker object to watch the
        // velocity of a motion.
        velocityTracker = velocityTracker ?: VelocityTracker.obtain()
        // Add a user's movement to the tracker.
        velocityTracker?.addMovement(event)
    }

    //<editor-fold desc="Open API">
    var edgeSize = (EDGE_SIZE * density + 0.5f).toInt()
    var trackingEdges = 0

    /**
     * 调用点击事件的处理逻辑
     */
    fun onTouchEvent(event: MotionEvent): Boolean {
        var pos = 0f
        var movementDirection = 0f
        var change = 0f
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                initMotion(event)
                initDragState()
                var shouldIntercept = shouldInterceptEdge(edgeCallback)
                initVelocityTracker(event)
                return shouldIntercept
            }
            MotionEvent.ACTION_MOVE -> {
                // 移动的距离
                val (x: Float, y: Float) =
                    event.findPointerIndex(activePointerId).let { pointerIndex ->
                        // Calculate the distance moved
                        event.getX(pointerIndex) to
                            event.getY(pointerIndex)
                    }

                velocityTracker?.apply {
                    //从开始触摸到现在总移动距离
                    val dx = x - initialMotionX[activePointerId]
                    val dy = y - initialMotionY[activePointerId]
                    addMovement(event)
                    // When you want to determine the velocity, call
                    // computeCurrentVelocity(). Then call getXVelocity()
                    // and getYVelocity() to retrieve the velocity for each pointer ID.
                    computeCurrentVelocity(1000, maximumFlingVelocity)

                    val dragDistance =
                        if (callback.getMovementDirection() == MovementDirection.Horizontal) {
                            dx
                        } else {
                            dy
                        }
                    //log.debug("dragDistance $dragDistance (${scrollConfiguration.touchSlop})")

                    //如果拖动的速度足够或已经在拖动中
                    if (abs(dragDistance) > scrollConfiguration.touchSlop || dragStarted) {
                        pos = callback.getCurrentProgress()
                        startDrag()

                        val scrollX = x - lastTouchX
                        val scrollY = y - lastTouchY
                        movementDirection = callback.getMovementDistance()
                        //根据方向计算拖动的比例
                        change = if (callback.getMovementDirection() == MovementDirection.Horizontal) {
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
                    //根据方向取速度
                    val velocity =
                        if (callback.getMovementDirection() == MovementDirection.Horizontal) {
                            getXVelocity(activePointerId)
                        } else {
                            getYVelocity(activePointerId)
                        }
                    change = callback.getCurrentProgress()
                    //按照速度计算松开后还要滑动的比例
                    movementDirection = callback.getMovementDistance()
                    val totalDistance = scrollConfiguration.getSplineFlingDistance(velocity)
                    val distance = (totalDistance * sign(velocity)).toFloat()
                    change += (distance / movementDirection) / 3f //调整的参数，避免甩动太容易
                    change = change.coerceIn(0f, 1f)

                    //百分比过半则做动画到结束，否则返回开始
                    if (change < 0.5f) {
                        callback.onAnimateToStart()
                    } else {
                        callback.onAnimateToEnd()
                    }
                }

                activePointerId = MotionEvent.INVALID_POINTER_ID
                resetVelocityTracker()
                stopDrag()
            }
            MotionEvent.ACTION_POINTER_UP -> {

                event.actionIndex.also { pointerIndex ->
                    event.getPointerId(pointerIndex)
                        .takeIf { it == activePointerId }
                        ?.run {
                            // This was our active pointer going up. Choose a new
                            // active pointer and adjust accordingly.
                            val newPointerIndex = if (pointerIndex == 0) 1 else 0
                            resetNewPointer(event, newPointerIndex)
                        }
                }
            }
        }
        return false
    }
    //</editor-fold>
}

/**
 * 拖动的回调
 */
interface DragProgressCallback {

    fun onDragStateChange(state: DragState)

    /**
     * 返回当前的MotionLayout.progress
     */
    fun getCurrentProgress(): Float

    /**
     * 横向/纵向的最大移动距离，一般指view.width/height
     */
    fun getMovementDistance(): Float

    /**
     * 横向/纵向拖动
     */
    fun getMovementDirection(): MovementDirection

    /**
     * 拖动时progress变化的通知
     */
    fun onProgressChange(progress: Float)

    /**
     * 实现触发直接做动画到开始
     */
    fun onAnimateToStart()

    /**
     * 实现触发直接做动画到结束
     */
    fun onAnimateToEnd()
}

interface DragEdgeCallback {
    /**
     * 返回拖动的view的上下左右位置
     */
    fun getViewRect(): Rect

    /**
     * 当边缘被触碰时回调
     * @param edgesTouched 边缘标记的组合，描述了当前触摸的边缘
     * @see #EDGE_LEFT
     * @see #EDGE_TOP
     * @see #EDGE_RIGHT
     * @see #EDGE_BOTTOM
     *
     * @return true 拦截Down事件，false不拦截
     */
    fun onEdgeTouched(edgesTouched: Int): Boolean
}

/**
 * 拖拉的状态
 */
enum class DragState { Idle, Start, Dragging }

/**
 * 滚动的方向
 */
enum class MovementDirection { Horizontal, Vertical }

