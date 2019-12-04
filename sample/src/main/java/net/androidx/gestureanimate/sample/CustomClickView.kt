package net.androidx.gestureanimate.sample

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import net.androidx.gestureanimate.UnsmoothClickCallback
import net.androidx.gestureanimate.UnsmoothClickGesture

/**
 * 实现UnsmoothClickGesture的View
 * Created by zhongyongsheng on 2019-12-04.
 */

class CustomClickView @JvmOverloads constructor(
    context: Context, attr: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attr, defStyleAttr) {
    private val gesture = UnsmoothClickGesture(context, object : UnsmoothClickCallback {
        override fun onUnsmoothClick(event: MotionEvent) {
            Toast.makeText(context, "view on click", Toast.LENGTH_SHORT).show()
        }
    })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gesture.onTouchEvent(event)
    }
}