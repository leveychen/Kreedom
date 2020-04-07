package com.goxod.freedom.utils

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Moshi 的简单封装
 * @link https://github.com/square/moshi
 * */
object Mo {

    private fun <T> obj(clz: Class<T>): JsonAdapter<T> {
        return Moshi
            .Builder()
            .add(KotlinJsonAdapterFactory())
            .build().adapter(clz)
    }

    private fun <T> list(clz: Class<T>): JsonAdapter<List<T>> {
        return Moshi
            .Builder()
            .add(KotlinJsonAdapterFactory())
            .build().adapter(Types.newParameterizedType(MutableList::class.java, clz))
    }

    fun <T> obj(clz: Class<T>, jsonStr: String): T? {
        return obj(clz).fromJson(jsonStr)
    }

    fun <T> list(clz: Class<T>, jsonStr: String): List<T>? {
        return list(clz).fromJson(jsonStr)
    }

    fun <T> string(clz: Class<T>, jsonObj: T?): String? {
        return obj(clz).toJson(jsonObj)
    }

    fun <T> string(clz: Class<T>, jsonList: List<T>?): String? {
        return list(clz).toJson(jsonList)
    }

}