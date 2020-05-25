package com.goxod.freedom.data.event

import com.arialyy.aria.core.download.DownloadEntity
import com.goxod.freedom.config.type.DownloadEventType

data class DownloadEvent(
    var type: DownloadEventType,
    var task: DownloadEntity?
)