package com.goxod.freedom.utils

import android.content.res.AssetManager
import org.mozilla.javascript.Context
import org.mozilla.javascript.Function
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.ScriptableObject
import java.io.InputStream
import java.lang.Exception
import java.util.*

class JsEngine(private val mContext: android.content.Context, private val jsFile:String) {
    private val clz: Class<*>
    private var rhino: Context? = null
    private var scope: Scriptable? = null
    private var jsCode = ""

    init {
        clz = JsEngine::class.java
        initRhinoEngine()
    }

    private fun initRhinoEngine() {
        jsCode = "var ScriptAPI = java.lang.Class.forName(\"" + clz.name + "\", true, javaLoader);\n" + loadJsString(mContext,jsFile)
        rhino = Context.enter()
        rhino?.optimizationLevel = -1
        try {
            scope = rhino?.initStandardObjects()
            ScriptableObject.putProperty(scope, "javaContext", Context.javaToJS(this, scope))
            ScriptableObject.putProperty(scope, "javaLoader", Context.javaToJS(clz.classLoader, scope))
            rhino?.evaluateString(scope, jsCode, clz.simpleName, 1, null)
        } catch (e:Exception) {
            Context.exit()
        }
    }

    fun request(method:String,args:List<String>):Any? {
        if(scope == null){
            return null
        }
        return callFunction(scope!!,method,args)
    }

    private fun callFunction(
        parent:Scriptable,
        method: String,
        args: List<String>
    ):Any?{
        val function = parent.get(method,scope) as Function
        return function.call(rhino, scope, parent, args.toTypedArray())
    }


    @Throws(java.lang.Exception::class)
    private fun loadJsString(context: android.content.Context, fileName: String): String? {
        return readJsFileFromAssets(context,fileName)
    }

    @Throws(java.lang.Exception::class)
    private fun readJsFileFromAssets(context: android.content.Context, fileName: String?): String? {
        val am: AssetManager = context.assets
        val inputStream: InputStream = am.open(fileName!!)
        val scanner = Scanner(inputStream, "UTF-8")
        return scanner.useDelimiter("\\A").next()
    }

    companion object{
        fun unRegister(){
            Context.exit()
        }
    }
}