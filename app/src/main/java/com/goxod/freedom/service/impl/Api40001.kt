package com.goxod.freedom.service.impl

import android.content.Context
import com.goxod.freedom.config.ApiConstants
import com.goxod.freedom.config.type.ApiItem
import com.goxod.freedom.data.entity.CategoryEntity
import com.goxod.freedom.data.entity.GoodsEntity
import com.goxod.freedom.data.entity.PageEntity
import com.goxod.freedom.service.ApiAbstract
import com.goxod.freedom.service.VideoService
import com.goxod.freedom.utils.ApiUtil
import com.goxod.freedom.utils.S
import org.jsoup.Jsoup

@Suppress("BlockingMethodInNonBlockingContext")
class Api40001 : ApiAbstract() {

    override fun api(): ApiItem {
        return ApiItem.API_40001
    }

    override fun pageSize(): Int {
        return 24
    }

    override var categories: List<CategoryEntity> = arrayListOf(
        CategoryEntity("最新发布", "latest-updates/?sort_by=post_date&from="),
        CategoryEntity("全部热门", "hot/?sort_by=video_viewed&from="),
        CategoryEntity("本周热门", "hot/?sort_by=video_viewed_week&from="),
        CategoryEntity("中字最新", "categories/chinese-subtitle/?sort_by=post_date&from="),
        CategoryEntity("中字好评", "categories/chinese-subtitle/?sort_by=post_date_and_popularity&from="),
        CategoryEntity("中字热门", "categories/chinese-subtitle/?sort_by=video_viewed&from="),
        CategoryEntity("無碼解放", "categories/uncensored/?sort_by=post_date&from="),
        CategoryEntity("制服誘惑", "categories/uniform/?sort_by=post_date&from="),
        CategoryEntity("角色劇情", "categories/roleplay/?sort_by=post_date&from="),
        CategoryEntity("絲襪美腿", "categories/pantyhose/?sort_by=post_date&from="),
        CategoryEntity("女同歡愉", "categories/lesbian/?sort_by=post_date&from=")

    )


    override suspend fun loadPageItems(page: Int): MutableList<PageEntity> {
        refreshCollection(page)
        val request = realHost + categories[categoryIndex].part + page
        S.log("PAGE = $request")
        val list = arrayListOf<PageEntity>()
        try {
            val doc = Jsoup
                .connect(request)
                .apply {
                    header("User-Agent", ApiUtil.ua())
                    header("X-Forwarded-For", ApiUtil.ip())
                    timeout(ApiConstants.TIMEOUT)
                }
                .execute()
                .parse()
            doc.select("div[class=video-img-box mb-e-20]")?.map {
                val item = PageEntity().apply {
                    apiId = api().apiId
                    title = it.select("div[class=detail]").select("a").text()
                    cover = it.select("div[class=img-box cover-md]").select("img").attr("data-src")
                    url = it.select("div[class=img-box cover-md]").select("a").attr("href")
                    duration =
                        it.select("div[class=img-box cover-md]").select("span[class=label]").text()
                    preview =
                        it.select("div[class=img-box cover-md]").select("img").attr("data-preview")
                }
                checkFavorite(item)
                list.add(item)
            }
        } catch (e: Exception) {
            S.log("load page error = " + e.localizedMessage)
            S.networkError()
        }
        return list
    }

    override suspend fun loadGoodsItems(
        context: Context,
        page: PageEntity
    ): MutableList<GoodsEntity> {
        return VideoService.fromApi40001(page)
    }

}