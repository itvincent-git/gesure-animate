package net.androidx.gestureanimate.util

import android.content.Context
import android.hardware.SensorManager
import android.view.ViewConfiguration
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln

/**
 * 取滚动配置参数
 * Created by zhongyongsheng on 2020-01-19.
 */
class ScrollConfiguration(context: Context) {
    companion object {
        //拐点
        private const val INFLEXION = 0.35f
        //减速率
        private val DECELERATION_RATE = (ln(0.78) / ln(0.9)).toFloat()
    }

    /**
     * 滚动的摩擦力
     */
    val mFlingFriction = ViewConfiguration.getScrollFriction()
    /**
     * PPI
     */
    private val mPpi = context.resources.displayMetrics.density * 160.0f
    /**
     * 计算的减速物理系数
     */
    private val mPhysicalCoeff = computeDeceleration(0.84f)

    /**
     * fling甩动的距离
     */
    fun getSplineFlingDistance(velocity: Float): Double {
        val l: Double = getSplineDeceleration(velocity)
        val decelMinusOne: Double = DECELERATION_RATE - 1.0
        return mFlingFriction * mPhysicalCoeff * exp(DECELERATION_RATE / decelMinusOne * l)
    }

    /**
     * 减速曲线值
     */
    fun getSplineDeceleration(velocity: Float): Double {
        return ln(INFLEXION * abs(velocity) / (mFlingFriction * mPhysicalCoeff).toDouble())
    }

    /**
     * 计算减速值
     */
    private fun computeDeceleration(friction: Float): Float {
        // g (m/s^2)
        // inch/meter
        // pixels per inch
        return (SensorManager.GRAVITY_EARTH * 39.37f * mPpi * friction)
    }
}