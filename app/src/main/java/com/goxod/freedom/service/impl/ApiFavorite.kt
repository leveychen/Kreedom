package com.goxod.freedom.service.impl

import android.content.Context
import com.goxod.freedom.config.type.ApiItem
import com.goxod.freedom.config.type.FavoriteType
import com.goxod.freedom.data.db.Db
import com.goxod.freedom.data.entity.CategoryEntity
import com.goxod.freedom.data.entity.GoodsEntity
import com.goxod.freedom.data.entity.PageEntity
import com.goxod.freedom.service.ApiAbstract
import com.goxod.freedom.service.VideoService
import com.goxod.freedom.utils.S

class ApiFavorite : ApiAbstract() {

    override fun api(): ApiItem {
        return ApiItem.FAVORITE
    }

    override fun pageSize(): Int {
        return 10
    }

    override fun originPage(): Int {
        return 0
    }

    override var categories: List<CategoryEntity> = arrayListOf(
        CategoryEntity("收藏", "collection"),
        CategoryEntity("下载", "download")
    )

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun loadPageItems(page: Int): MutableList<PageEntity> {
        val list = arrayListOf<PageEntity>()
        val dd = Db.favorite(FavoriteType.values()[categoryIndex], page, 10)
        dd?.map {
            val item = PageEntity.generate(it)
            if (it.url == item.url) {
                item.favorite = it.favoriteType
                //当本地视频存在时则认为是已下载，直接赋予本地视频
                if(it.video.isNotBlank()){
                    item.goods.add(GoodsEntity("本地",it.video))
                }
            }
            list.add(item)
        }
        return list
    }

    override suspend fun loadGoodsItems(
        context: Context,
        page: PageEntity
    ): MutableList<GoodsEntity> {
        return when (page.apiId) {
            ApiItem.API_40001.apiId -> {
                VideoService.fromApi40001(page)
            }
            ApiItem.API_20001.apiId -> {
                VideoService.fromApi20001(context,page)
            }
            ApiItem.API_10001.apiId -> {
                arrayListOf(GoodsEntity("短片", page.preview))
            }
            else -> {
                arrayListOf()
            }
        }
    }
}