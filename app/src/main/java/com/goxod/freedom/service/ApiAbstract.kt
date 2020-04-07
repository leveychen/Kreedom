package com.goxod.freedom.service

import com.goxod.freedom.data.db.Db
import com.goxod.freedom.data.db.LocalVideo
import com.goxod.freedom.data.entity.CategoryEntity
import com.goxod.freedom.data.entity.PageEntity
import com.goxod.freedom.utils.AesUtil

abstract class ApiAbstract : ApiInterface {
    override var realHost: String = ""
    override fun fetchRealHost() {
        realHost = AesUtil.decrypt(api().key)
    }

    override fun originPage(): Int {
        return 1
    }

    override var categoryIndex: Int = 0


    private var collection: MutableList<LocalVideo> = arrayListOf()


    /**
     * 处理默认的收藏数据
     * */
    override fun refreshCollection(page: Int) {
        if (page == originPage()) {
            collection.clear()
            Db.collection(api())?.let { collection.addAll(it) }
        }
    }


    protected fun checkFavorite(item: PageEntity) {
        collection.map {
            if (it.url == item.url) {
                item.favorite = it.favoriteType
            }
        }
    }
}