package com.goxod.freedom

import android.app.Application
import com.goxod.freedom.config.sp.Sp
import com.goxod.freedom.service.DownloadService
import com.goxod.freedom.utils.AesUtil
import com.goxod.freedom.utils.S
import com.mikepenz.iconics.Iconics
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
        initYoutubeDL()
        initDrawer()
        initPlayer()
        initRefreshLayout()
        initDataAndSp()
        initDownloadService()
        genAes()
    }

    private fun initDownloadService(){
        DownloadService.init(this)
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

    private fun initYoutubeDL() {
//        GlobalScope.launch {
//            try {
//                YoutubeDL.getInstance().init(this@App)
//                S.log("init YoutubeDL OK")
//            } catch (e: YoutubeDLException) {
//                S.log("init YoutubeDL ERROR")
//                S.postError("初始化错误", "核心组件加载失败")
//            }
//            try {
//                S.log("init updateYoutubeDL OK")
//                YoutubeDL.getInstance().updateYoutubeDL(this@App)
//            } catch (e: YoutubeDLException) {
//                S.log("init updateYoutubeDL ERROR")
//            }
//        }
    }

    private fun initDrawer() {
        Iconics.init(this)
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

    private fun genAes(){
    }

}