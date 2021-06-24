package com.goxod.freedom

import android.app.Application
import com.arialyy.aria.core.Aria
import com.goxod.freedom.config.sp.Sp
import com.goxod.freedom.service.DownloadService
import com.goxod.freedom.utils.AesUtil
import com.goxod.freedom.utils.S
import com.mikepenz.iconics.Iconics
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.tencent.bugly.Bugly
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.litepal.LitePal
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager


class App : Application() {
    override fun onCreate() {
        super.onCreate()
        initBugly()
        initIconics()
        initPlayer()
        initRefreshLayout()
        initDataAndSp()
        S.log(AesUtil.encrypt("https://0310.workgreat17.live/"))
    }

    private fun initBugly(){
        Bugly.init(applicationContext, "0454d7274b", BuildConfig.DEBUG)
    }

    private fun initDataAndSp() {
        Sp.load(this)
        GlobalScope.launch {
            LitePal.initialize(this@App)
        }
    }

    private fun initIconics() {
        Iconics.init(this)
        Iconics.registerFont(FontAwesome)
    }

    private fun initPlayer() {
        PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
    }

    private fun initRefreshLayout(){
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, _ ->
            ClassicsHeader(context)
        }
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ ->
            ClassicsFooter(context)
        }
    }
}