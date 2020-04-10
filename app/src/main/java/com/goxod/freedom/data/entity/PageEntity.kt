package com.goxod.freedom.data.entity

import com.goxod.freedom.data.db.LocalVideo
import com.goxod.freedom.utils.S
import java.util.*

data class PageEntity(
    var title: String = "",
    var apiId: Int = 0,
    var cover: String = "",
    var url: String = "",
    var duration: String = "",
    var preview: String = "",
    var favoriteTime: String = "",
    var favorite: Int = -1,
    var isFavoritePage: Boolean = false,
    var goods: MutableList<GoodsEntity> = arrayListOf()
){
    companion object{
        fun generate(it:LocalVideo):PageEntity{
            return PageEntity().apply {
                title = it.title
                apiId = it.apiId
                url = it.url
                duration = it.duration
                cover = it.cover
                preview = it.preview
                favorite = it.favoriteType
                isFavoritePage = true
                favoriteTime = S.localVideoSfd.format(Date(it.time))
            }
        }
    }
}