package com.goxod.freedom.view

import android.content.Context
import android.content.Intent
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.goxod.freedom.BuildConfig
import com.goxod.freedom.R
import com.goxod.freedom.config.type.ApiItem
import com.goxod.freedom.config.type.FavoriteType
import com.goxod.freedom.data.db.Db
import com.goxod.freedom.data.db.LocalVideo
import com.goxod.freedom.data.entity.GoodsEntity
import com.goxod.freedom.data.entity.PageEntity
import com.goxod.freedom.utils.S
import com.tencent.bugly.beta.Beta
import org.litepal.LitePal
import kotlin.system.exitProcess

object XDialog{


    fun settings(context: Context){
        S.info(context,"暂无设置选项")
    }


    fun about(context: Context){
        MaterialDialog(context)
            .show {
                title(text = "关于 v" + BuildConfig.VERSION_NAME)
                listItems(items = context.resources.getStringArray(R.array.change_log).asList())
                noAutoDismiss()
                cancelable(false)
                positiveButton(text = "好的"){
                    dismiss()
                }
                negativeButton(text = "检查更新"){
                    Beta.checkUpgrade()
                }
            }
    }

    fun close(context: Context){
        MaterialDialog(context).show {
            title(text = "关闭" + context.getString(R.string.app_name) + "?")
            positiveButton(text = "关闭")
            positiveButton {
                android.os.Process.killProcess(android.os.Process.myPid())
                exitProcess(0)
            }
            negativeButton(text = "取消")
            negativeButton {
                dismiss()
            }
        }
    }

    fun tips(context: Context): MaterialDialog {
        return MaterialDialog(context).apply {
            title(text = "提示信息")
            message(text = "提示内容")
            noAutoDismiss()
            cancelable(false)
            positiveButton(text = "好的") {
                dismiss()
            }
        }
    }

    fun collection(context: Context, item: PageEntity) {
        favorite(context, FavoriteType.COLLECTION, item, null)
    }

    fun download(context: Context, item: PageEntity, video: GoodsEntity) {
        favorite(context, FavoriteType.DOWNLOAD, item, video)
    }

    private fun favorite(
        context: Context,
        favoriteType: FavoriteType,
        item: PageEntity,
        video: GoodsEntity?
    ) {
        var actionFavoriteType = favoriteType

        if (item.apiId == ApiItem.FAVORITE.apiId) {
            if (favoriteType == FavoriteType.COLLECTION) {
                actionFavoriteType = FavoriteType.DEL_COLLECTION
            } else if (favoriteType == FavoriteType.DOWNLOAD) {
                actionFavoriteType = FavoriteType.DEL_DOWNLOAD
            }
        } else {
            val first = Db.first(item.url, favoriteType)
            if (first != null) {
                if (favoriteType == FavoriteType.COLLECTION) {
                    actionFavoriteType = FavoriteType.DEL_COLLECTION
                } else if (favoriteType == FavoriteType.DOWNLOAD) {
                    actionFavoriteType = FavoriteType.DEL_DOWNLOAD
                }
            }
        }
        MaterialDialog(context).show {
            title(text = actionFavoriteType.title)
            val message = when (actionFavoriteType) {
                FavoriteType.DOWNLOAD -> {
                    item.title + ".mp4\n[" + video?.definition + "]"
                }
                FavoriteType.COLLECTION -> {
                    item.title
                }
                FavoriteType.DEL_DOWNLOAD -> {
                    item.title
                }
                FavoriteType.DEL_COLLECTION -> {
                    item.title
                }
            }
            message(text = message)
            noAutoDismiss()
            cancelable(false)
            positiveButton(text = actionFavoriteType.positiveButton) {
                when (actionFavoriteType) {
                    FavoriteType.DOWNLOAD,
                    FavoriteType.COLLECTION -> {
                        val dv = LocalVideo(item.url).apply {
                            this.apiId = item.apiId
                            this.favoriteType = favoriteType.ordinal
                            this.title = item.title
                            this.cover = item.cover
                            this.duration = item.duration
                            this.preview = item.preview
                            if (video != null) {
                                this.definition = video.definition
                            }
                            this.taskId = 0
                        }
                        dv.saveAndNotify(item)
                    }
                    FavoriteType.DEL_DOWNLOAD,
                    FavoriteType.DEL_COLLECTION -> {
                        val dv = LocalVideo(item.url)
                        dv.deleteAndNotify(item)
                    }
                }
                dismiss()
            }
            negativeButton(text = "取消") {
                dismiss()
            }
        }
    }
}