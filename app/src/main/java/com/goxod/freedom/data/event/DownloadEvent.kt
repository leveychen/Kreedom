package com.goxod.freedom.data.event

import com.goxod.freedom.data.entity.GoodsEntity
import com.goxod.freedom.data.entity.PageEntity

data class DownloadEvent(
    var item: PageEntity,
    var goods: GoodsEntity
)