package net.androidx.gestureanimate.sample

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.ChangeInterceptorListener
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.WeChatLikeAppBarBehavior
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_wechat_like.app_bar
import kotlinx.android.synthetic.main.activity_wechat_like.expand
import kotlinx.android.synthetic.main.activity_wechat_like.shrink
import kotlinx.android.synthetic.main.layout_content_scrolling.recycler_view
import kotlinx.android.synthetic.main.layout_coordinator_header.motionLayout
import net.androidx.gestureanimate.util.createRandomString
import net.androidx.kangga.sample.MyAdapter
import net.slog.SLoggerFactory

class WechatLikeActivity : AppCompatActivity() {
    private val myAdapter = MyAdapter(mutableListOf()).apply {
        for (i in 0 until 50) {
            myDataset.add(createRandomString(10))
        }
    }

    companion object {
        private val log = SLoggerFactory.getLogger("WechatLikeActivity")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wechat_like)

        //large_tv.text = createRandomString(1024 * 8)
        recycler_view.apply {
            layoutManager = LinearLayoutManager(this@WechatLikeActivity)
            adapter = myAdapter
        }

        val headerLayoutParam = motionLayout.layoutParams as AppBarLayout.LayoutParams
//        headerLayoutParam.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams
//            .SCROLL_FLAG_EXIT_UNTIL_COLLAPSED or AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP

        app_bar.addOnOffsetChangedListener(
            ChangeInterceptorListener(this, headerLayoutParam))

        val appbarLayoutParam = app_bar.layoutParams as CoordinatorLayout.LayoutParams
        val appBarBehavior = appbarLayoutParam.behavior as WeChatLikeAppBarBehavior
        expand.setOnClickListener {
            app_bar.setExpanded(true)
            appBarBehavior.allowScroll = true
        }

        shrink.setOnClickListener {
            app_bar.setExpanded(false)
            appBarBehavior.allowScroll = false
        }
    }
}

