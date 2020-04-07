package com.goxod.freedom.config.type

enum class FavoriteType(val existTips: String, val positiveButton: String, val title: String) {
    COLLECTION("该视频已收藏", "收藏", "是否收藏视频?"),
    DOWNLOAD("该视频已下载", "下载", "是否下载视频?"),
    DEL_COLLECTION("移除收藏", "移除", "是否移除收藏?"),
    DEL_DOWNLOAD("删除下载", "删除", "是否删除视频?")
}