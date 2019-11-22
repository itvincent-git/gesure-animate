package net.androidx.gestureanimate.sample

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import net.androidx.gestureanimate.UnsmoothClickCallback
import net.androidx.gestureanimate.UnsmoothClickGesture

class UnsmoothClickActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unsmooth_click)
    }
}

class CustomView @JvmOverloads constructor(
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