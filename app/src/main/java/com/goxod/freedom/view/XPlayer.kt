package com.goxod.freedom.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.goxod.freedom.R
import com.goxod.freedom.config.sp.Sp
import com.goxod.freedom.config.type.FavoriteType
import com.goxod.freedom.data.db.Db
import com.goxod.freedom.data.entity.SpeedEntity
import com.goxod.freedom.data.entity.GoodsEntity
import com.goxod.freedom.data.entity.PageEntity
import com.goxod.freedom.utils.S
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.impl.AttachListPopupView
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import kotlinx.android.synthetic.main.x_player.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class XPlayer(context: Context?, attrs: AttributeSet?) : StandardGSYVideoPlayer(context, attrs) {
    private lateinit var currentGoods: GoodsEntity
    lateinit var pageItem: PageEntity
    private var switchPop: AttachListPopupView? = null
    private var speedPop: AttachListPopupView? = null
    private var speedList = arrayListOf<SpeedEntity>().apply {
        add(SpeedEntity("0.5x", 0.5f))
        add(SpeedEntity("1.0x", 1.0f))
        add(SpeedEntity("1.5x", 1.5f))
        add(SpeedEntity("2.0x", 1.95f))
    }

    override fun init(context: Context?) {
        super.init(context)
        switch_size.setOnClickListener {
            if (switchPop == null) {
                switchPop = XPopup.Builder(context)
                    .atView(it)
                    .asAttachList(
                        pageItem.goods.map(GoodsEntity::definition).toTypedArray(),
                        null
                    ) { position, text ->
                        if (switch_size.text == text) {
                            return@asAttachList
                        }
                        if (!mHadPlay) {
                            return@asAttachList
                        }
                        currentGoods = pageItem.goods[position]
                        switch_size.text = text
                        setUpAndStartPlay(currentGoods.url)
                        S.toast(it.context, "切换到 $text")
                    }
            }
            switchPop?.show()
        }

        change_speed.setOnClickListener {
            if (speedPop == null) {
                speedPop = XPopup.Builder(context)
                    .atView(it)
                    .asAttachList(
                        speedList.map(SpeedEntity::speed).toTypedArray(),
                        null
                    ) { position, text ->
                        if (change_speed.text == text) {
                            return@asAttachList
                        }
                        if (!mHadPlay) {
                            return@asAttachList
                        }
                        change_speed.text = text
                        speed = speedList[position].rate
                        S.toast(it.context, "播放速度 $text")
                    }
            }
            speedPop?.show()
        }

        change_rotate.setOnClickListener {
            if (!mHadPlay) {
                return@setOnClickListener
            }
            if ((mTextureView.rotation - mRotate) == 270f) {
                mTextureView.rotation = mRotate.toFloat()
                mTextureView.requestLayout()
            } else {
                mTextureView.rotation = mTextureView.rotation + 90f
                mTextureView.requestLayout()
            }
        }
        download.setOnClickListener {
            S.log("DOWN = " + currentGoods.definition + " / " + currentGoods.url)
            if(currentGoods.url.endsWith(".m3u8")){
                S.toast(it.context,"HLS视频暂不支持下载")
                return@setOnClickListener
            }
            XDialog.download(it.context, pageItem, currentGoods)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.x_player
    }

    fun setUp(item: PageEntity, preview: Boolean) {
        try {
            currentGoods = item.goods[0]
        } catch (e: Exception) {
        }
        pageItem = item
        if (preview) {
            mTitle = "[预览]" + item.title
            download.visibility = View.GONE
            fullscreen.visibility = View.GONE
            setUpAndStartPlay(item.preview)
        } else {
            mTitle = item.title
            item.goods.map {
                if (it.definition == "标清") {
                    currentGoods = it
                    return@map
                }
            }
            switch_size.text = currentGoods.definition
            if ("本地" == currentGoods.definition) {
                S.log("已下载,隐藏下载图标")
                download.visibility = View.GONE
            }else{
                download.visibility = View.VISIBLE
            }
            //4/10，v3.2.0 下载功能不完善，暂不支持下载，后续开放
           // download.visibility = View.GONE
            fullscreen.visibility = View.VISIBLE
            setUpAndStartPlay(currentGoods.url)
        }
    }

    private fun setUpAndStartPlay(url: String) {
        var playUrl = url
        if (S.fake()) {
            S.log("REAL PLAY = $playUrl")
            playUrl = "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4"
        }
        if (switch_size.text == "短片") {
            this.setUp(playUrl, true, null, mTitle)
        } else {
            this.setUp(playUrl, false, null, mTitle)
        }
        currentPlayer.isLooping = true
        S.log("PLAY = $playUrl")
        startPlay()
    }

    private fun startPlay() {
        hardwareAcceleratedSettings()
        GlobalScope.launch {
            delay(1000)
            GlobalScope.launch(Dispatchers.Main) { currentPlayer?.startPlayLogic() }
        }
    }

    private fun hardwareAcceleratedSettings(){
        if(Sp.conf.hardwareAccelerated) {
            GSYVideoType.enableMediaCodec()
        }else{
            GSYVideoType.disableMediaCodec()
        }
    }
}