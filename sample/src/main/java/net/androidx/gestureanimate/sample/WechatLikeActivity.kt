package net.androidx.gestureanimate.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.layout_content_scrolling.large_tv
import net.androidx.gestureanimate.util.createRandomString

class WechatLikeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wechat_like)

        large_tv.text = createRandomString(1024 * 8)
    }
}
