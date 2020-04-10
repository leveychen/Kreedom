package com.goxod.freedom.data.event

import com.goxod.freedom.config.type.DownloadEventType
import com.jeffmony.downloader.model.VideoTaskItem

data class DownloadEvent(
    var type: DownloadEventType,
    var task: VideoTaskItem?
)