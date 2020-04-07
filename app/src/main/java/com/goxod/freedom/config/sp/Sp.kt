package com.goxod.freedom.config.sp

import android.content.Context
import com.goxod.freedom.utils.Mo
import com.goxod.freedom.utils.S


object Sp {

    private const val KEY: String = "freedom"

    var conf: SpEntity =
        SpEntity(
            false
        )

    fun save(context: Context) {
        val json = Mo.string(SpEntity::class.java, conf)!!
        S.log("SAVE SP = $json")
        context.saveValue(KEY, json)
    }

    fun load(context: Context) {
        val s = context.getValue<String>(KEY)
        S.log("LOAD SP = $s")
        if(s != "") conf = Mo.obj(SpEntity::class.java,s)!!
    }
}