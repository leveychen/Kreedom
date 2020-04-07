package com.goxod.freedom.service

import android.content.Context
import com.goxod.freedom.config.type.ApiItem
import com.goxod.freedom.data.entity.CategoryEntity
import com.goxod.freedom.data.entity.PageEntity
import com.goxod.freedom.data.entity.GoodsEntity

interface ApiInterface {

    var realHost:String

    fun fetchRealHost()

    fun api(): ApiItem

    fun pageSize():Int

    /**
     * 默认起始页码
     * */
    fun originPage(): Int

    /**
     * 分类指针，默认0
     * */
    var categoryIndex: Int


    /**
     * 某个板块的分类列表
     * */
    var categories: List<CategoryEntity>

    /**
     * 获取某个页面的数据
     * */
    suspend fun loadPageItems(page: Int): MutableList<PageEntity>

    /**
     * 获取某个条目的视频列表
     * */
    suspend fun loadGoodsItems(context: Context, page: PageEntity): MutableList<GoodsEntity>


    /**
     * 刷新收藏数据，方便匹配已收藏的内容
     * */
    fun refreshCollection(page: Int)

}