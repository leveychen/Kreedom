package com.goxod.freedom.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.arialyy.aria.core.download.DownloadEntity
import com.arialyy.aria.core.listener.ISchedulers
import com.arialyy.aria.core.task.DownloadTask
import com.goxod.freedom.utils.Mo
import com.goxod.freedom.utils.S

class AriaBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            if(intent?.action.equals(ISchedulers.ARIA_TASK_INFO_ACTION)) {
                val type = intent?.getIntExtra(ISchedulers.TASK_TYPE, -1)
                if (type == 1 || type == 4) {
                    val entity = intent.getParcelableExtra<DownloadEntity>(ISchedulers.TASK_ENTITY)
                    S.log("entity?.taskType = " + entity?.taskType )
                    S.log("entity = " + entity?.toString())
                    S.log("entity = " + entity?.convertFileSize + " / " + entity?.convertSpeed)
                    when(entity?.taskType){
                        ISchedulers.START -> DownloadService.onDownloadStart(entity)
                        ISchedulers.STOP -> DownloadService.onDownloadDelete(entity)
                        ISchedulers.FAIL -> DownloadService.onDownloadDelete(entity)
                        ISchedulers.CANCEL -> DownloadService.onDownloadDelete(entity)
                        ISchedulers.COMPLETE -> DownloadService.onDownloadSuccess(entity)
                        ISchedulers.RUNNING -> DownloadService.onDownloadProgress(entity)
                    }
                }
            }
        } catch (e: Exception) {
            S.log("DOWNLOAD ERROR = " + e.localizedMessage)
        }
    }
}