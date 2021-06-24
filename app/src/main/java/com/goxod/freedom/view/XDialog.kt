package com.goxod.freedom.view

import android.content.Context
import android.view.ViewGroup
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.list.listItems
import com.goxod.freedom.BuildConfig
import com.goxod.freedom.R
import com.goxod.freedom.config.type.FavoriteType
import com.goxod.freedom.data.db.Db
import com.goxod.freedom.data.db.LocalVideo
import com.goxod.freedom.data.entity.GoodsEntity
import com.goxod.freedom.data.entity.PageEntity
import com.tencent.bugly.beta.Beta
import kotlin.system.exitProcess

object XDialog{


    fun settings(context: Context){
        MaterialDialog(context)
            .show {
                title(text = "设置")
                noAutoDismiss()
                cancelable(false)
                customView(viewRes = R.layout.dialog_settings)
                positiveButton(text = "确定"){
                    dismiss()
                }
            }
    }


    fun about(context: Context){
        val dialog  = MaterialDialog(context)
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
        //val group:ViewGroup = dialog.getCustomView() as ViewGroup
    }

    fun close(context: Context){
        MaterialDialog(context).show {
            title(text = "关闭" + context.getString(R.string.app_name) + "?")
            message(text = "由于视频存在访问有效期限制,无法支持断点续传。\n关闭应用将停止并移除所有正在下载的视频!!!三思 :D")
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
        val first = Db.first(item.url)
        if (first != null) {
            if (first.favoriteType == FavoriteType.COLLECTION.ordinal) {
                if(actionFavoriteType != FavoriteType.DOWNLOAD){
                    actionFavoriteType = FavoriteType.DEL_COLLECTION
                }
            } else if (first.favoriteType == FavoriteType.DOWNLOAD.ordinal) {
                actionFavoriteType = FavoriteType.DEL_DOWNLOAD
            }
        }
        MaterialDialog(context).show {
            title(text = actionFavoriteType.title)
            val message = when (actionFavoriteType) {
                FavoriteType.DOWNLOAD -> {
                    item.title
                }
                else-> item.title

            }
            message(text = message)
            noAutoDismiss()
            cancelable(false)
            positiveButton(text = actionFavoriteType.positiveButton) {
                when (actionFavoriteType) {
                    FavoriteType.DOWNLOAD,
                    FavoriteType.COLLECTION -> {
                        item.favorite = favoriteType.ordinal
                        if(first != null){
                            first.favoriteType = item.favorite
                            first.time = System.currentTimeMillis()
                            first.saveAndNotify(this.windowContext,item, video)
                        }else{
                            val dv = LocalVideo(item.url).apply {
                                this.apiId = item.apiId
                                this.favoriteType = item.favorite
                                this.title = item.title
                                this.cover = item.cover
                                this.duration = item.duration
                                this.preview = item.preview
                                if (video != null) {
                                    this.definition = video.definition
                                }
                            }
                            dv.saveAndNotify(this.windowContext,item, video)
                        }
                    }
                    FavoriteType.DEL_DOWNLOAD,
                    FavoriteType.DEL_COLLECTION -> {
                        val dv = LocalVideo(item.url)
                        dv.deleteAndNotify(context,item)
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