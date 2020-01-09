package android.support.design.widget

import android.view.animation.Interpolator
import net.slog.SLoggerFactory
import kotlin.math.pow

/**
 * 实现wechat缓动曲线的插值器
 * Created by zhongyongsheng on 2020-01-08.
 */
class MoreSlowInterpolator : Interpolator {
    override fun getInterpolation(input: Float): Float {
//        val ret = (1.0f - (input - 1.0f).toDouble().pow(2.0)).toFloat()
//        val ret = 1.0f - (1.0f - input.toDouble().pow(2.0)).pow(0.5).toFloat()
        var ret = input
        if (input < 0.9f) {
            ret = ((1.0f - (1.0f - input).toDouble().pow(3.0))).toFloat() * 0.9009009f
        }
        //log.debug("getInterpolation $input > $ret")
        return ret
    }

    companion object {
        private val log = SLoggerFactory.getLogger("MoreSlowInterpolator")
    }
}