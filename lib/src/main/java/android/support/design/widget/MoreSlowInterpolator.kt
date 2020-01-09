package android.support.design.widget

import android.view.animation.Interpolator
import net.slog.SLoggerFactory
import kotlin.math.pow

/**
 * 测试用
 * Created by zhongyongsheng on 2020-01-08.
 */
class MoreSlowInterpolator : Interpolator {
    //(1 - (x-1)^2 )* 0.5
    override fun getInterpolation(input: Float): Float {
//        val ret = (1.0f - (input - 1.0f).toDouble().pow(2.0)).toFloat()
        val ret = 1.0f - (1.0f - input.toDouble().pow(2.0)).pow(0.5).toFloat()
        log.debug("getInterpolation $input > $ret")
        return ret
    }

    companion object {
        private val log = SLoggerFactory.getLogger("MoreSlowInterpolator")
    }
}