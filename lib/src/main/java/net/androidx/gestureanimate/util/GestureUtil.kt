package net.androidx.gestureanimate.util

import android.content.Context
import java.util.Random

/**
 * 工具类
 * Created by zhongyongsheng on 2019-12-17.
 */
/**
 * dp convert to pixel
 */
fun Context.dipToPx(dpValue: Float): Int {
    val scale = resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}

/**
 * create random string
 */
fun createRandomString(length: Int): String {
    val str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    val random = Random()
    val sb = StringBuffer()
    for (i in 0 until length) {
        val number: Int = random.nextInt(62)
        sb.append(str[number])
    }
    return sb.toString()
}