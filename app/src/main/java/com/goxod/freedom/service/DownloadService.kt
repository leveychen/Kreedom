package com.goxod.freedom.service

import android.content.Context
import com.goxod.freedom.config.ApiConstants
import com.goxod.freedom.config.type.DownloadEventType
import com.goxod.freedom.config.type.FavoriteType
import com.goxod.freedom.data.db.Db
import com.goxod.freedom.data.db.LocalVideo
import com.goxod.freedom.data.event.DownloadEvent
import com.goxod.freedom.utils.S
import com.jeffmony.downloader.VideoDownloadManager
import com.jeffmony.downloader.listener.DownloadListener
import com.jeffmony.downloader.model.VideoTaskItem
import com.jeffmony.downloader.utils.Utility
import com.jeffmony.downloader.utils.VideoDownloadUtils
import org.greenrobot.eventbus.EventBus
import java.io.File


object DownloadService {

    var downloadList: MutableList<LocalVideo> = arrayListOf()

    private var context:Context ?=null

    private fun refreshDownloadList(){
        Db.favorite(FavoriteType.DOWNLOAD,0, Int.MAX_VALUE)?.let { downloadList.addAll(it) }
    }

    fun getUrl(taskId:String):String?{
        return downloadList.find {
            it.taskId == taskId
        }?.url
    }

    fun init(context: Context){
        this.context = context
        val config = VideoDownloadManager.Build(context)
            .setCacheRoot(context.getExternalFilesDir("video"))
            .setUrlRedirect(true)
            .setTimeOut(ApiConstants.TIMEOUT, ApiConstants.TIMEOUT)
            .setConcurrentCount(ApiConstants.TIMEOUT)
            .setIgnoreCertErrors(true)
            .buildConfig()
        VideoDownloadManager.getInstance().initConfig(config)
        VideoDownloadManager.getInstance().setGlobalDownloadListener(downloadListener)
    }

    private val downloadListener: DownloadListener = object : DownloadListener() {

        private val UI_REFRESH_INTERVAL = 500
        private var lastUiRefreshTime = 0L

        override fun onDownloadPrepare(item: VideoTaskItem?) {
            if(item == null) return
            S.log("onDownloadPrepare = " + item.url)
            refreshDownloadList()
            postEvent(DownloadEventType.Prepare,item)
        }
        override fun onDownloadStart(item: VideoTaskItem?) {
            if(item == null) return
            S.log("onDownloadStart = " + item.url)
            postEvent(DownloadEventType.Start,item)
        }
        override fun onDownloadProgress(item: VideoTaskItem?) {
            if(item == null) return
            val ct = System.currentTimeMillis()
            if(ct - lastUiRefreshTime > UI_REFRESH_INTERVAL) {
                lastUiRefreshTime = ct
                S.log(
                    "onDownloadProgress = " + item.speedString + " / " + item.downloadSizeString + " / " + Utility.getSize(
                        item.totalSize
                    )
                )
                postEvent(DownloadEventType.Progress, item)
            }
        }

        override fun onDownloadSuccess(item: VideoTaskItem?) {
            if(item == null) return
            item.filePath = item.filePath.replace("/.",".")
            S.log("onDownloadSuccess filePath = " + item.filePath)
            Db.task(item.url)?.apply {
                video = item.filePath
                S.log("onDownloadSuccess AbsPath = $video")
                totalSize = Utility.getSize(item.totalSize)
            }?.save()
            refreshDownloadList()
            postEvent(DownloadEventType.Success,item)
        }
        override fun onDownloadError(item: VideoTaskItem?) {
            if(item == null) return
            S.log("onDownloadError = " + item.url)
            Db.task(item.url)?.delete()
            refreshDownloadList()
            postEvent(DownloadEventType.Error,item)
        }

        override fun onDownloadPause(item: VideoTaskItem?) {
            super.onDownloadPause(item)
            S.log("onDownloadPause = " + item?.url)
            postEvent(DownloadEventType.DELETE,item)
        }
    }

    private fun postEvent(type:DownloadEventType,item: VideoTaskItem?){
        EventBus.getDefault().post(DownloadEvent(type,item))
    }

    fun download(taskId:String){
        VideoDownloadManager.getInstance().startDownload(VideoTaskItem(taskId))
    }

    fun stop(url:String){
        S.log("DownloadService stop")
        VideoDownloadManager.getInstance().deleteVideoTask(VideoTaskItem(url),true)
    }
}