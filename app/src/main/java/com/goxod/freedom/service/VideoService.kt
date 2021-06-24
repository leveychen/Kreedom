package com.goxod.freedom.service

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.goxod.freedom.config.ApiConstants
import com.goxod.freedom.data.entity.GoodsEntity
import com.goxod.freedom.data.entity.PageEntity
import com.goxod.freedom.utils.ApiUtil
import com.goxod.freedom.utils.S
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.util.regex.Pattern


object VideoService {

    @SuppressLint("SetJavaScriptEnabled")
    fun fromApi20001(context: Context, page: PageEntity): MutableList<GoodsEntity> {
        var timeout = 10000L
        val list = arrayListOf<GoodsEntity>()
        var blocking = true
        var args = listOf<String>()
        val sUrl = page.url.replace("view_video_hd.php", "view_video.php")
        S.log("SURL = " + sUrl)
        try {
            val doc = Jsoup
                .connect(sUrl)
                .header("Connection", "close")
                .apply {
                    header("User-Agent", ApiUtil.ua())
                    header("X-Forwarded-For", ApiUtil.ip())
                    timeout(ApiConstants.TIMEOUT)
                }
                .execute().parse()
            val video = doc.select("textarea[name=video_link]").text()
            S.log("VIDEO = $video")
            //context.startActivity(Intent(context,WebActivity::class.java))
            val vd = Jsoup
                .connect(video)
                .header("Connection", "close")
                .apply {
                    header("User-Agent", ApiUtil.ua())
                    header("X-Forwarded-For", ApiUtil.ip())
                    timeout(ApiConstants.TIMEOUT)
                }
                .execute().parse()
            vd.getElementsByTag("script").map { it ->
                if (it.data().contains("document.write(strencode(")) {
                    val origin = it.data()
                        .replace("<!--", "")
                        .replace("//-->", "")
                        .trim()
                        .replace("document.write(strencode(\"", "")
                        .replace("\"));", "")
                    args = origin.split("\",\"")
                    return@map
                }
            }
            GlobalScope.launch(Dispatchers.Main) {
                try {
                    val webView = WebView(context)
                    val webSettings = webView.settings
                    webSettings.javaScriptEnabled = true
                    webSettings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                    webSettings.allowFileAccess = true //设置可以访问文件
                    webSettings.javaScriptCanOpenWindowsAutomatically = true //支持通过JS打开新窗口
                    webSettings.loadsImagesAutomatically = true //支持自动加载图片
                    webSettings.defaultTextEncodingName = "utf-8" //设置编码格式
                    val js =
                        "javascript:strencode(\"" + args[0] + "\",\"" + args[1] + "\",\"" + args[2] + "\")"
                    webView.webViewClient = WjB(js) { sdt ->
                        val url = sdt.replace("\"\\u003Csource src='", "")
                            .replace("' type='application/x-mpegURL'>\"", "")
                        list.add(GoodsEntity("标清", url))
                        blocking = false
                        return@WjB
                    }
                    webView.loadUrl(video)
                } catch (e: Exception) {
                    blocking = false
                }
            }
        } catch (e: Exception) {
            blocking = false
        }
        runBlocking {
            while (blocking && timeout >= 0) {
                delay(200)
                timeout -= 200
            }
        }
        return list
    }


    private class WjB(val js: String, val callback: (rst: String) -> Unit) : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            view?.evaluateJavascript(js) {
                callback.invoke(it)
                S.log("evaluateJavascript = $it")
            }
        }
    }


    fun fromApi40001(page: PageEntity): MutableList<GoodsEntity> {
//        S.log("ORG = " + page.url)
//        val link = try {
//            val doc = Jsoup
//                .connect(page.url)
//                .apply {
//                    header("User-Agent", ApiUtil.ua())
//                    header("X-Forwarded-For", ApiUtil.ip())
//                    timeout(ApiConstants.TIMEOUT)
//                }
//                .execute().parse()
//            doc.select("link[rel=preload]").attr("href")
//        } catch (e: Exception) {
//            return arrayListOf()
//        }
//        if (link != null && link.endsWith(".m3u8")) {
//            val list = arrayListOf<GoodsEntity>()
//            list.add(GoodsEntity("默认", link))
//            return list
//        }
        S.log("ORG = " + page.url)
        val link = try {
            val doc = Jsoup
                .connect(page.url)
                .header("Connection", "close")
//                .apply {
//                    header("User-Agent", ApiUtil.ua())
//                    header("X-Forwarded-For", ApiUtil.ip())
//                    timeout(ApiConstants.TIMEOUT)
//                }
                .execute().parse()
//            doc.select("link[rel=preload]").attr("href")
            val scp =
                doc.select("div[class=site-content]").select("section[class=pb-3 pb-e-lg-30]")
                    .select("script").last()
            val pattern = Pattern.compile("(?=https).*(?<=m3u8)")
            val matcher = pattern.matcher(scp.html())
            if (matcher.find()) {
                matcher.group().toString()
            } else {
                null
            }
        } catch (e: Exception) {
            return arrayListOf()
        }
        if (link != null && link.endsWith(".m3u8")) {
            val list = arrayListOf<GoodsEntity>()
            S.log("M3U8 = $link")
            list.add(GoodsEntity("高清", link))
            return list
        }
        return arrayListOf()
    }

}



