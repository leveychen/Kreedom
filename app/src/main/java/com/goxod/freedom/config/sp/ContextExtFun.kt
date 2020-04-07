package com.goxod.freedom.config.sp

import android.content.Context

fun Context.saveValue(key: String, value: String) =
    SpFunction.saveValue(
        context = this,
        key = key,
        value = value
    )

inline fun <reified T> Context.getValue(key: String, def: T? = null) =
    SpFunction.getValue(
        context = this,
        key = key,
        def = def
    )

fun Context.clear() {
    SpFunction.clear(context = this)
}

fun Context.remove(key: String) {
    SpFunction.remove(context = this, key = key)
}
