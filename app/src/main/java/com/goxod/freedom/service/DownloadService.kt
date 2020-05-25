package com.goxod.freedom.service

import android.content.Context
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.download.DownloadEntity
import com.arialyy.aria.core.download.m3u8.M3U8VodOption
import com.arialyy.aria.core.processor.IVodTsUrlConverter
import com.goxod.freedom.config.type.DownloadEventType
import com.goxod.freedom.config.type.FavoriteType
import com.goxod.freedom.data.db.Db
import com.goxod.freedom.data.db.LocalVideo
import com.goxod.freedom.data.event.DownloadEvent
import com.goxod.freedom.utils.S
import org.greenrobot.eventbus.EventBus
import java.util.*


object DownloadService {

    private var downloadList: MutableList<LocalVideo> = arrayListOf()

    private fun refreshDownloadList() {
        Db.favorite(FavoriteType.DOWNLOAD, 0, Int.MAX_VALUE)?.let { downloadList.addAll(it) }
    }


    fun getUrl(taskId: String): String? {
        return downloadList.find {
            it.taskId == taskId
        }?.url
    }

    private fun getId(taskId: String): Long? {
        return downloadList.find {
            it.taskId == taskId
        }?.downloadId
    }

    fun onDownloadStart(item: DownloadEntity?) {
        if (item == null) return
        refreshDownloadList()
        S.log("onDownloadStart = " + item.url)
        postEvent(DownloadEventType.Start, item)
    }

    fun onDownloadProgress(item: DownloadEntity?) {
        postEvent(DownloadEventType.Progress, item)
    }

    fun onDownloadSuccess(item: DownloadEntity?) {
        if (item == null) return
        S.log("onDownloadSuccess filePath = " + item.filePath)
        Db.task(item.url)?.apply {
            video = item.filePath
            S.log("onDownloadSuccess AbsPath = $video")
            totalSize = ""
        }?.save()
        refreshDownloadList()
        postEvent(DownloadEventType.Success, item)
    }

    fun onDownloadDelete(item: DownloadEntity?) {
        S.log("onDownloadPause = " + item?.url)
        refreshDownloadList()
        postEvent(DownloadEventType.DELETE, item)
    }

    private fun postEvent(type: DownloadEventType, item: DownloadEntity?) {
        EventBus.getDefault().post(DownloadEvent(type, item))
    }

    fun download(context: Context,taskId: String):Long{
        S.log("DownloadService download = $taskId")
        val file = context.getExternalFilesDir("video")?.absolutePath + "/V" + System.currentTimeMillis() + ".mp4"
        if(taskId.endsWith(".m3u8")){
            val option = M3U8VodOption()
            option.setVodTsUrlConvert(vodTsUrlConverter)
            option.setMaxTsQueueNum(32)
            return Aria.download(context).load(taskId).m3u8VodOption(option).setFilePath(file).ignoreFilePathOccupy().create()
        }
        return Aria.download(context).load(taskId).setFilePath(file).ignoreFilePathOccupy().create()
    }

    fun stop(context: Context,taskId: String) {
        val id = getId(taskId)
        S.log("DownloadService stop = $taskId")
        if(id != null && id > 0) {
            Aria.download(context).load(id).cancel(true)
        }
    }
    val vodTsUrlConverter = IVodTsUrlConverter{
        m3u8Url, tsUrls ->
        val host = m3u8Url.substring(0,m3u8Url.lastIndexOf("/")) + "/"
        val newUrls: MutableList<String> = ArrayList()
        S.log("host = $host")
        for (url in tsUrls) {
            if(url.startsWith("http")){
                newUrls.add(url)
            }else{
                newUrls.add(host + url)
            }
        }
        newUrls // 返回有效的ts文件url集合
    }
}