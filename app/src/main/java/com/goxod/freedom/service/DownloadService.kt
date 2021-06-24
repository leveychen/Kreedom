package com.goxod.freedom.service

import android.content.Context
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.download.DownloadEntity
import com.arialyy.aria.core.download.m3u8.M3U8VodOption
import com.goxod.freedom.config.type.DownloadEventType
import com.goxod.freedom.config.type.FavoriteType
import com.goxod.freedom.data.db.Db
import com.goxod.freedom.data.db.LocalVideo
import com.goxod.freedom.data.entity.PageEntity
import com.goxod.freedom.data.event.DownloadEvent
import com.goxod.freedom.utils.S
import org.greenrobot.eventbus.EventBus
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


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

    fun encode(text: String): String {
        try {
            //获取md5加密对象
            val instance: MessageDigest = MessageDigest.getInstance("MD5")
            //对字符串加密，返回字节数组
            val digest:ByteArray = instance.digest(text.toByteArray())
            val sb = StringBuffer()
            for (b in digest) {
                //获取低八位有效值
                val i :Int = b.toInt() and 0xff
                //将整数转化为16进制
                var hexString = Integer.toHexString(i)
                if (hexString.length < 2) {
                    //如果是一位的话，补0
                    hexString = "0$hexString"
                }
                sb.append(hexString)
            }
            return sb.toString()

        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }

    fun download(context: Context,taskId: String,item: PageEntity):Long{
        S.log("DownloadService download = $taskId")
        val urlToMd5 = encode(item.url)
        S.log("DownloadService download KEY = $urlToMd5")
        val file = context.getExternalFilesDir("video")?.absolutePath + "/V" + urlToMd5 + ".mp4"
        if(taskId.endsWith(".m3u8")){
            val option = M3U8VodOption()
            option.setMaxTsQueueNum(10)
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
}