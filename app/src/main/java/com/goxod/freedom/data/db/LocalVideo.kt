package com.goxod.freedom.data.db

import android.content.Context
import com.goxod.freedom.config.type.FavoriteType
import com.goxod.freedom.data.entity.GoodsEntity
import com.goxod.freedom.data.entity.PageEntity
import com.goxod.freedom.data.event.FavoriteEvent
import com.goxod.freedom.service.DownloadService
import com.goxod.freedom.utils.S
import org.greenrobot.eventbus.EventBus
import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport


class LocalVideo(@Column(unique = true, defaultValue = "unknown") var url: String) :
    LitePalSupport() {
    var apiId: Int = 0 //api类型id

    /**
     * @see com.goxod.freedom.service.impl.ApiFavorite
     * 最喜爱的类型，1.收藏，2.下载
     * */
    var favoriteType: Int = 0
    var taskId: String = "" //下载任务id
    var title: String = "" //标题
    var cover: String = "" //封面
    var preview: String = "" //预览
    var video: String = "" //本地视频
    var duration: String = "" //时长
    var definition: String = "" //分辨率
    var totalSize: String = "" //文件长度
    var ext: String = "" //其他附属属性
    var time: Long = System.currentTimeMillis() //收藏或下载的时间
    var downloadId :Long = -1

    fun saveAndNotify(
        context: Context,
        item: PageEntity,
        video: GoodsEntity?
    ) {
        S.log("saveAndNotify URL  $url / favoriteType = $favoriteType")
        //当视频不为空时判定为下载
        if(favoriteType == FavoriteType.DOWNLOAD.ordinal && video != null){
            taskId = video.url
            downloadId = DownloadService.download(context,taskId,item)
            S.log("saveAndNotify DOWNLOAD = $taskId / downloadId = $downloadId")
        }
        save()
        notifyItemChanged(favoriteType, item)
    }

    fun deleteAndNotify(context: Context,item: PageEntity) {
        S.log("deleteAndNotify URL = $url")
        Db.delete(context,url)
        item.goods.clear()
        notifyItemChanged(-1, item)
    }

    private fun notifyItemChanged(favorite: Int, item: PageEntity) {
        EventBus.getDefault().post(FavoriteEvent(favorite, item))
    }
}