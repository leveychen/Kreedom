package com.goxod.freedom.data.adapter

import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.goxod.freedom.R
import com.goxod.freedom.config.type.FavoriteType
import com.goxod.freedom.data.entity.PageEntity
import com.goxod.freedom.data.event.FavoriteEvent
import com.goxod.freedom.utils.Mo
import com.goxod.freedom.utils.S
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.view.IconicsImageView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class ItemAdapter : BaseQuickAdapter<PageEntity, BaseViewHolder>(R.layout.item_video) {

    companion object {
        private const val FAVORITE_PAGE_TAG = 1
    }

    init {
        EventBus.getDefault().register(this)
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
            visibility = View.VISIBLE
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
                        FontAwesome.Icon.faw_trash_alt
                    } else {
                        setTag(R.id.btn_favorite, 0)
                        FontAwesome.Icon.faw_heart1
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