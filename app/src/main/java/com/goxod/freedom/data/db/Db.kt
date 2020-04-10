package com.goxod.freedom.data.db

import com.goxod.freedom.config.type.ApiItem
import com.goxod.freedom.config.type.FavoriteType
import com.goxod.freedom.utils.Mo
import com.goxod.freedom.utils.S
import org.litepal.LitePal

object Db {


    fun delete(url: String) {
        val ok = LitePal.deleteAll(LocalVideo::class.java, "url = ?", url)
        S.log("DELETE OK = $ok")
    }


    fun collection(api: ApiItem): MutableList<LocalVideo>? {
        val list = LitePal.where("apiId = ?", "" + api.apiId).find(LocalVideo::class.java)
        S.log("collection LIST = " + Mo.string(LocalVideo::class.java, list))
        return list
    }

    fun favorite(type: FavoriteType, page: Int, size: Int): MutableList<LocalVideo>? {
        val list =
            LitePal.where("favoriteType = ?", "" + type.ordinal).order("time DESC").limit(size)
                .offset(page * size)
                .find(LocalVideo::class.java)
        S.log("favorite LIST = " + Mo.string(LocalVideo::class.java, list))
        return list
    }

    fun first(url: String, type: FavoriteType): LocalVideo? {
        val video = LitePal.where("url = ? and favoriteType = ?", url, "" + type.ordinal)
            .findFirst(LocalVideo::class.java)
        S.log("first = " + Mo.string(LocalVideo::class.java, video))
        return video
    }

    fun first(url: String): LocalVideo? {
        val video = LitePal.where("url = ?", url)
            .findFirst(LocalVideo::class.java)
        S.log("first = " + Mo.string(LocalVideo::class.java, video))
        return video
    }

    fun task(taskId: String): LocalVideo? {
        val video = LitePal.where("taskId = ? and favoriteType = ?", taskId, "" + FavoriteType.DOWNLOAD.ordinal)
            .findFirst(LocalVideo::class.java)
        S.log("task = " + Mo.string(LocalVideo::class.java, video))
        return video
    }
}