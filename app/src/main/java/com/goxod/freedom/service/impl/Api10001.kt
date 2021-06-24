package com.goxod.freedom.service.impl

import android.content.Context
import com.goxod.freedom.config.ApiConstants
import com.goxod.freedom.data.entity.PageEntity
import com.goxod.freedom.data.entity.CategoryEntity
import com.goxod.freedom.data.entity.GoodsEntity
import com.goxod.freedom.config.type.ApiItem
import com.goxod.freedom.service.ApiAbstract
import com.goxod.freedom.utils.ApiUtil
import com.goxod.freedom.utils.S
import org.jsoup.Jsoup

class Api10001 : ApiAbstract() {

    override fun api(): ApiItem {
        return ApiItem.API_10001
    }

    override fun pageSize(): Int {
        return 33
    }

    override var categories: List<CategoryEntity> = arrayListOf(
        CategoryEntity("最新发布", "japanese&o=mr"),
        CategoryEntity("当日热门", "japanese&o=mv&t=t"),
        CategoryEntity("本周热门", "japanese&o=mv&t=w"),
        CategoryEntity("本月热门", "japanese&o=mv&t=m"),
        CategoryEntity("全部热门", "japanese&o=mv&t=a"),
        CategoryEntity("当日好评", "japanese&o=tr&t=t"),
        CategoryEntity("本周好评", "japanese&o=tr&t=w"),
        CategoryEntity("本月好评", "japanese&o=tr&t=m"),
        CategoryEntity("全部好评", "japanese&o=tr&t=a")
    )

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun loadPageItems(page: Int): MutableList<PageEntity> {
        refreshCollection(page)
        val request =
            realHost + "gifs/search?search=" + categories[categoryIndex].part + "&page=" + page
        S.log("PAGE = $request")
        val list = arrayListOf<PageEntity>()
        try {
            val doc = Jsoup
                .connect(request)
                .header("Connection", "close")
                .apply {
                    header(
                        "User-Agent",
                        ApiUtil.ua()
                    )
                    header("Host", "www." + realHost.replace("http:","").replace("https:","").replace("/",""))
                    timeout(ApiConstants.TIMEOUT)
                }
                .execute()
                .parse()
            doc.select("li[class=gifVideoBlock js-gifVideoBlock]")?.map {
                val item = PageEntity().apply {
                    apiId = api().apiId
                    title = it.select("span[class=title]").text()
                    cover =
                        it.select("video[class=gifVideo js-gifVideo lazyVideo]").attr("data-poster")
                    url = realHost + it.select("a").attr("href")
                    preview =
                        it.select("video[class=gifVideo js-gifVideo lazyVideo]").attr("data-mp4")
                }
                checkFavoriteAndGoods(item)
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
        return arrayListOf(GoodsEntity("短片", page.preview))
    }
}