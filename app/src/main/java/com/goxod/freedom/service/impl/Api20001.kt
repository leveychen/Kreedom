package com.goxod.freedom.service.impl

import android.content.Context
import com.goxod.freedom.config.type.ApiItem
import com.goxod.freedom.config.ApiConstants
import com.goxod.freedom.data.entity.CategoryEntity
import com.goxod.freedom.data.entity.GoodsEntity
import com.goxod.freedom.data.entity.PageEntity
import com.goxod.freedom.service.ApiAbstract
import com.goxod.freedom.service.VideoService
import com.goxod.freedom.utils.ApiUtil
import com.goxod.freedom.utils.S
import org.jsoup.Jsoup

class Api20001 : ApiAbstract() {

    override fun api(): ApiItem {
        return ApiItem.API_20001
    }

    override fun pageSize(): Int {
        return 20
    }

    override var categories: List<CategoryEntity> = arrayListOf(
        CategoryEntity("最新视频", "v.php?next=watch"),
        CategoryEntity("当前最热", "v.php?category=hot"),
        CategoryEntity("本月最热", "v.php?category=top"),
        CategoryEntity("上月最热", "v.php?category=top&m=-1"),
        CategoryEntity("十分以上", "v.php?category=long"),
        CategoryEntity("本月收藏", "v.php?category=tf"),
        CategoryEntity("收藏最多", "v.php?category=mf"),
        CategoryEntity("最近加精", "v.php?category=rf"),
        CategoryEntity("最近得分", "v.php?category=rp"),
        CategoryEntity("高清视频", "v.php?category=hd"),
        CategoryEntity("本月讨论", "v.php?category=md")
    )

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun loadPageItems(page: Int): MutableList<PageEntity> {
        //修正此处两个分类无限加载的问题
        if (categories[categoryIndex].category == "当前最热" && page > 3) return arrayListOf()
        if (categories[categoryIndex].category == "本月最热" && page > 5) return arrayListOf()
        refreshCollection(page)
        val request = realHost + categories[categoryIndex].part + "&page=" + page
        S.log("PAGE = $request")
        val list = arrayListOf<PageEntity>()
        try {
            val doc = Jsoup
                .connect(request)
                .apply {
                    header("User-Agent", ApiUtil.ua())
                    header("X-Forwarded-For", ApiUtil.ip())
                    header("Accept-Encoding", "gzip, deflate")
                    header("Cache-Control", "max-age=0")
                    header("Upgrade-Insecure-Requests", "1")
                    header("Accept-Language", "zh-CN,zh;q=0.9")
                    header(
                        "Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3"
                    )
                    timeout(ApiConstants.TIMEOUT)
                }
                .execute()
                .parse()
            doc.select("div.well.well-sm")?.map {
                val item = PageEntity().apply {
                    apiId = ApiItem.API_20001.apiId
                    title = it.select("span[class=video-title title-truncate m-t-5]").text()
                    cover = it.select("img[class=img-responsive]").attr("src")
                    url = it.select("a").attr("href")
                    duration = it.select("span[class=duration]").text()
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
        S.log("GOODS = " + page.url)
        return VideoService.fromApi20001(context, page)
    }
}