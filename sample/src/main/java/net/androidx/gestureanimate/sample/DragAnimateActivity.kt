package net.androidx.gestureanimate.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import net.slog.SLoggerFactory

class DragAnimateActivity : AppCompatActivity() {
    val log = SLoggerFactory.getLogger(javaClass)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drag_animate)
    }

    fun backgroundOnClick(view: View) {
        log.debug("backgroundOnClick")
    }
}
