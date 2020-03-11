package net.androidx.gestureanimate

import android.content.Context
import android.util.SparseArray
import android.view.MotionEvent
import net.androidx.gestureanimate.util.ScrollConfiguration
import net.slog.SLoggerFactory

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

    private var activePointerId = MotionEvent.INVALID_POINTER_ID
    private val initialMotionX = SparseArray<Float>() //记录点击开始时的X
    private val initialMotionY = SparseArray<Float>() //记录点击开始时的Y
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private val scrollConfiguration = ScrollConfiguration(context) // 从系统及通过算法处理后的滚动参数

    private fun initMotion(event: MotionEvent) {
        // Remember where we started (for dragging)
        val pointerIndex = event.actionIndex
        lastTouchX = event.getX(pointerIndex)
        lastTouchY = event.getY(pointerIndex)
        activePointerId = event.getPointerId(pointerIndex)
        initialMotionX.put(activePointerId, lastTouchX)
        initialMotionY.put(activePointerId, lastTouchY)
    }

    private fun resetMotion(event: MotionEvent, newPointerIndex: Int) {
        lastTouchX = event.getX(newPointerIndex)
        lastTouchY = event.getY(newPointerIndex)
        activePointerId = event.getPointerId(newPointerIndex)

        initialMotionX.clear()
        initialMotionY.clear()
    }

    /**
     * 调用点击事件的处理逻辑
     */
    fun onTouchEvent(event: MotionEvent): Boolean {
        //log.debug("onTouchEvent $event")
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                initMotion(event)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // 移动的距离
                val (x: Float, y: Float) =
                    event.findPointerIndex(activePointerId).let { pointerIndex ->
                        // Calculate the distance moved
                        event.getX(pointerIndex) to
                            event.getY(pointerIndex)
                    }

                //从开始触摸到现在总移动距离
                val dx = x - initialMotionX[activePointerId]
                val dy = y - initialMotionY[activePointerId]

                val checkSlop = dx * dx + dy * dy <= (scrollConfiguration.touchSlop * scrollConfiguration.touchSlop)
                    .toFloat()
                if (checkSlop) {
                    callback.onUnsmoothClick(event)
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {

                event.actionIndex.also { pointerIndex ->
                    event.getPointerId(pointerIndex)
                        .takeIf { it == activePointerId }
                        ?.run {
                            // This was our active pointer going up. Choose a new
                            // active pointer and adjust accordingly.
                            val newPointerIndex = if (pointerIndex == 0) 1 else 0
                            resetMotion(event, newPointerIndex)
                        }
                }
            }
        }
        return true
    }
}

interface UnsmoothClickCallback {

    fun onUnsmoothClick(event: MotionEvent)
}
