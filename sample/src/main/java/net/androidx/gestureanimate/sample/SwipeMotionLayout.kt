package net.androidx.gestureanimate.sample

import android.content.Context
import android.graphics.Rect
import android.support.constraint.motion.MotionLayout
import android.util.AttributeSet
import android.view.MotionEvent
import net.androidx.gestureanimate.DragEdgeCallback
import net.androidx.gestureanimate.DragProgressGesture
import net.androidx.gestureanimate.DragProgressCallback
import net.androidx.gestureanimate.DragState
import net.androidx.gestureanimate.MovementDirection
import net.slog.SLoggerFactory

/**
 * 支持左右滑动做动画的MotionLayout
 * Created by zhongyongsheng on 2019-11-20.
 */
class SwipeMotionLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MotionLayout(context, attrs, defStyleAttr) {

    companion object {
        val log = SLoggerFactory.getLogger("SwipeMotionLayout")
    }

    var isOnTouchLeft = false

    private val callback = object : DragProgressCallback {
        override fun onDragStateChange(state: DragState) {
            log.debug("onDragStateChange $state")
            when (state) {
                DragState.Start -> isOnTouchLeft = false
            }
        }

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
            return MovementDirection.Vertical
        }

        override fun onProgressChange(value: Float) {
            log.debug("onProgressChange $progress")
            if (!isOnTouchLeft) {
                progress = value
            }
        }

        override fun onAnimateToStart() {
            if (!isOnTouchLeft) {
                transitionToStart()
            }
        }

        override fun onAnimateToEnd() {
            if (!isOnTouchLeft) {
                transitionToEnd()
            }
        }
    }

    private val edgeCallback = object : DragEdgeCallback {
        override fun getViewRect(): Rect {
            return Rect(left, top, right, bottom)
        }

        override fun onEdgeTouched(edgesTouched: Int) {
            //左边边缘触摸，则不能拖动
            if (edgesTouched and DragProgressGesture.EDGE_LEFT == 1) {
                log.debug("onEdgeTouched Left")
                isOnTouchLeft = true
            }
        }
    }

    private val gesture = DragProgressGesture(context, callback, edgeCallback).apply {
        trackingEdges = DragProgressGesture.EDGE_LEFT
    }

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
        this.gesture.onTouchEvent(event)
        return true
    }
}