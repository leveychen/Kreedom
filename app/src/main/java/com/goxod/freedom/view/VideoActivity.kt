package com.goxod.freedom.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.goxod.freedom.R
import com.goxod.freedom.config.ApiConstants
import com.goxod.freedom.data.entity.PageEntity
import com.goxod.freedom.utils.Mo
import com.goxod.freedom.utils.S
import com.shuyu.gsyvideoplayer.GSYVideoManager
import kotlinx.android.synthetic.main.activity_video.*
import kotlinx.android.synthetic.main.x_player.view.*

class VideoActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context, item: PageEntity, seekTo: Long) {
            val intent = Intent(context, VideoActivity::class.java).apply {
                putExtra(ApiConstants.DATA, Mo.string(PageEntity::class.java, item))
                putExtra(ApiConstants.SEEK_TO, seekTo)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        initPlayer()
    }

    private fun initPlayer() {
        x_player.apply {
            pageItem = Mo.obj(PageEntity::class.java, intent.getStringExtra(ApiConstants.DATA)!!)!!
            currentPlayer.isRotateViewAuto = true
            currentPlayer.isLockLand = true
            currentPlayer.isShowFullAnimation = false
            currentPlayer.dismissControlTime = 6000
            currentPlayer.currentPlayer.backButton.setOnClickListener { this@VideoActivity.finish() }
            val seekTo = intent.getLongExtra(ApiConstants.SEEK_TO, -1L)
            currentPlayer.seekOnStart = seekTo
            currentPlayer.isIfCurrentIsFullscreen = true
            currentPlayer.isNeedLockFull = true
            currentPlayer.fullscreen.visibility = View.GONE
            currentPlayer.fullscreenButton.visibility = View.GONE
            currentPlayer.startWindowFullscreen(this@VideoActivity, false, false)
            setUp(pageItem, false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            GSYVideoManager.releaseAllVideos()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            GSYVideoManager.onPause()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            GSYVideoManager.onResume()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                return S.quitClick(this, event, "再按一次关闭视频", false)
            }
        }
        return super.onKeyDown(keyCode, event)
    }

}
