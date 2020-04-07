package com.goxod.freedom.data.event

import com.goxod.freedom.data.entity.PageEntity

data class FavoriteEvent(
    var favorite: Int,
    var item: PageEntity
)