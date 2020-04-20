package com.goxod.freedom.data.adapter

import android.annotation.SuppressLint
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.goxod.freedom.R
import com.goxod.freedom.config.type.DownloadEventType
import com.goxod.freedom.config.type.FavoriteType
import com.goxod.freedom.data.db.Db
import com.goxod.freedom.data.entity.GoodsEntity
import com.goxod.freedom.data.entity.PageEntity
import com.goxod.freedom.data.event.DownloadEvent
import com.goxod.freedom.data.event.FavoriteEvent
import com.goxod.freedom.service.DownloadService
import com.goxod.freedom.utils.Mo
import com.goxod.freedom.utils.S
import com.jeffmony.downloader.utils.Utility
import com.jeffmony.downloader.utils.VideoDownloadUtils
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.view.IconicsImageView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File


class ItemAdapter : BaseQuickAdapter<PageEntity, BaseViewHolder>(R.layout.item_video) {

    companion object {
        private const val FAVORITE_PAGE_TAG = 1
    }

    init {
        EventBus.getDefault().register(this)
    }

    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDownload(event: DownloadEvent) {
        S.log("onDownload = TASK = " + event.task!!.url)
        val url = DownloadService.getUrl(event.task!!.url) ?: return
        data.indices.map {
            if (data[it].url == url) {
                val progress = getViewByPosition(it,R.id.tv_progress) as AppCompatTextView
                when(event.type){
                    DownloadEventType.Prepare ->{
                        progress.text = "正在准备下载..."
                        progress.visibility = View.VISIBLE
                        S.toast(context,"正在准备下载\n" + data[it].title)
                    }
                    DownloadEventType.Start ->{
                        progress.text = "已开始下载..."
                        progress.visibility = View.VISIBLE
                        S.toast(context,"已开始下载\n" + data[it].title)
                    }
                    DownloadEventType.Progress ->{
                        progress.text = "" + event.task!!.downloadSizeString + " / " + Utility.getSize(event.task!!.totalSize) + "\n\n" + event.task!!.percentString + " - " + event.task!!.speedString
                        progress.visibility = View.VISIBLE
                    }
                    DownloadEventType.Success ->{
                        data[it].goods.clear()
                        data[it].goods.add(GoodsEntity("本地",event.task!!.filePath))
                        progress.visibility = View.GONE
                        S.success(context,"下载完成\n" + data[it].title)
                        notifyItemChanged(it)
                    }
                    DownloadEventType.Error ->{
                        progress.visibility = View.GONE
                        S.error(context,"下载错误\n" + data[it].title)
                        notifyItemChanged(it)
                    }
                    DownloadEventType.DELETE ->{
                        progress.visibility = View.GONE
                        S.info(context,"取消下载\n" + data[it].title)
                        notifyItemChanged(it)
                    }
                }
                return
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFavorite(favorite: FavoriteEvent) {
        S.log("onFavorite =  " + Mo.string(FavoriteEvent::class.java, favorite))
        data.indices.map {
            if (data[it].url == favorite.item.url) {
                data[it].favorite = favorite.favorite
                if (getViewByPosition(
                        it,
                        R.id.btn_favorite
                    )?.getTag(R.id.btn_favorite) == FAVORITE_PAGE_TAG
                ) {
                    data.removeAt(it)
                    notifyItemRemoved(it)
                    notifyItemRangeChanged(it, data.size - it)
                } else {
                    notifyItemChanged(it)
                }
                return
            }
        }
    }


    override fun convert(helper: BaseViewHolder, item: PageEntity) {
        helper.setText(R.id.tv_title,item.title)
        Glide.with(context)
            .load(if (S.fake()) R.mipmap.ic_launcher else item.cover)
            .centerCrop()
            .into(helper.getView(R.id.iv_cover))
        helper.getView<IconicsImageView>(R.id.btn_play).apply {
            visibility = if(item.favorite == FavoriteType.DOWNLOAD.ordinal){
                if(item.goods.isEmpty()) {
                    View.GONE
                }else{
                    View.VISIBLE
                }
            }else{
                View.VISIBLE
            }

        }
        helper.getView<AppCompatTextView>(R.id.tv_duration).apply {
            text = if (item.favoriteTime.isNotBlank()){
                visibility = View.VISIBLE
                if (item.duration.isNotBlank()) item.duration + " [" + item.favoriteTime + "]" else "[" + item.favoriteTime + "]"
            }else{
                visibility = if (item.duration.isNotBlank()) View.VISIBLE else View.GONE
                item.duration
            }
        }
        helper.getView<IconicsImageView>(R.id.btn_preview).apply {
            visibility = if (item.preview.isNotBlank()) View.VISIBLE else View.GONE
        }
        helper.getView<IconicsImageView>(R.id.btn_favorite)
            .apply {
                visibility = View.VISIBLE
                icon?.apply {
                    icon = if (item.isFavoritePage) {
                        setTag(R.id.btn_favorite, FAVORITE_PAGE_TAG)
                        when (item.favorite) {
                            FavoriteType.COLLECTION.ordinal -> {
                                FontAwesome.Icon.faw_heart_broken
                            }
                            FavoriteType.DOWNLOAD.ordinal -> {
                                FontAwesome.Icon.faw_trash_alt
                            }
                            else -> {
                                FontAwesome.Icon.faw_heart1
                            }
                        }
                    } else {
                        setTag(R.id.btn_favorite, 0)
                        when (item.favorite) {
                            FavoriteType.COLLECTION.ordinal -> {
                                FontAwesome.Icon.faw_heart1
                            }
                            FavoriteType.DOWNLOAD.ordinal -> {
                                FontAwesome.Icon.faw_download
                            }
                            else -> {
                                FontAwesome.Icon.faw_heart1
                            }
                        }
                    }
                    isSelected = when (item.favorite) {
                        FavoriteType.COLLECTION.ordinal -> true
                        FavoriteType.DOWNLOAD.ordinal -> true
                        else -> false
                    }
                }
            }
    }
}