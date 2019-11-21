package net.androidx.gestureanimate.sample

import android.app.Application
import net.slog.SLoggerFactory
import net.slog.composor.ComposorBinderBuilder
import net.slog.composor.LogLevel
import net.slog.composor.logcat.LogcatDispatcher

/**
 * 自定义application
 * Created by zhongyongsheng on 2018/7/20.
 */
class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        application = this
        SLoggerFactory.initialize(
            ComposorBinderBuilder()
                .run {
                    //定义日志级别
                    this.addDispatcher(LogcatDispatcher())
                    this.logLevel(LogLevel.Verbose)
                    this
                }
                .build())
    }

    companion object {
        @JvmStatic
        var application: SampleApplication? = null
    }
}