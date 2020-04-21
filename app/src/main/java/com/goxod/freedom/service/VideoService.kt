package com.goxod.freedom.service

import android.content.Context
import com.goxod.freedom.config.ApiConstants
import com.goxod.freedom.data.entity.GoodsEntity
import com.goxod.freedom.data.entity.PageEntity
import com.goxod.freedom.utils.ApiUtil
import com.goxod.freedom.utils.JsEngine
import com.goxod.freedom.utils.S
import org.jsoup.Jsoup


object VideoService {
    private var api20001Engine :JsEngine ?= null

    fun fromApi20001(context: Context, page: PageEntity): MutableList<GoodsEntity> {
        val list = arrayListOf<GoodsEntity>()
        try {
            val doc = Jsoup
                .connect(page.url.replace("view_video_hd.php","view_video.php"))
                .apply {
                    header("User-Agent", ApiUtil.ua())
                    header("X-Forwarded-For", ApiUtil.ip())
                    timeout(ApiConstants.TIMEOUT)
                }
                .execute().parse()
            val videoUrl = doc.select("source").attr("src")
            if(!videoUrl.isNullOrBlank()){
                list.add(GoodsEntity("默认", videoUrl))
            }else {
                doc.getElementsByTag("script").map {
                    if (it.data().contains("document.write(strencode(")) {
                        val origin = it.data()
                            .replace("<!--", "")
                            .replace("//-->", "")
                            .trim()
                            .replace("document.write(strencode(\"", "")
                            .replace("\"));", "")
                        val args = origin.split("\",\"")
                        if (api20001Engine == null) {
                            api20001Engine = JsEngine(context, "js/api20001.js")
                        }
                        val s = api20001Engine?.request("strencode", args)
                        if (s is String) {
                            val video = Jsoup.parse(s).select("source").attr("src")
                            S.log("api20001Engine VIDEO = $video")
                            list.add(GoodsEntity("默认", video))
                        }
                        return@map
                    }
                }
            }
            if(list.isEmpty()){
                val video = Jsoup.parse(doc.select("textarea[id=video_link]").text()).select("source").attr("src")
                list.add(GoodsEntity("默认", video))
            }
        } catch (e: Exception) {
            S.log("fromApi20001 = " + e.localizedMessage)
        }
        return list
    }


    fun fromApi40001(page: PageEntity): MutableList<GoodsEntity> {
        S.log("ORG = " + page.url)
        val link = try {
            val doc = Jsoup
                .connect(page.url)
                .apply {
                    header("User-Agent", ApiUtil.ua())
                    header("X-Forwarded-For", ApiUtil.ip())
                    timeout(ApiConstants.TIMEOUT)
                }
                .execute().parse()
            doc.select("link[rel=preload]").attr("href")
        } catch (e: Exception) {
            return arrayListOf()
        }
        if (link != null && link.endsWith(".m3u8")) {
            val list = arrayListOf<GoodsEntity>()
            list.add(GoodsEntity("默认", link))
            return list
        }
        return arrayListOf()
    }
}



