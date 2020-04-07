package com.goxod.freedom.data.entity

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
)