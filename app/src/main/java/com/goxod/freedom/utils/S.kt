package com.goxod.freedom.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.view.KeyEvent
import com.goxod.freedom.BuildConfig
import com.goxod.freedom.config.ApiConstants
import com.goxod.freedom.data.event.ErrorEvent
import es.dmoral.toasty.Toasty
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.*


object S {



    @SuppressLint("ConstantLocale")
    val localVideoSfd = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

    fun fake(): Boolean {
        if(!BuildConfig.DEBUG){
            return false
        }
        return ApiConstants.FAKE_RESOURCE
    }

    private const val TAG = "opop "

    fun log(s: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, s)
    }


    fun toast(context: Context, s: String) {
        Toasty.normal(context, s, Toasty.LENGTH_SHORT).show()
    }

    fun success(context: Context, s: String) {
        Toasty.success(context, s, Toasty.LENGTH_SHORT).show()
    }

    fun info(context: Context, s: String) {
        Toasty.info(context, s, Toasty.LENGTH_SHORT).show()
    }

    fun warning(context: Context, s: String) {
        Toasty.warning(context, s, Toasty.LENGTH_SHORT).show()
    }

    fun error(context: Context, s: String) {
        Toasty.error(context, s, Toasty.LENGTH_SHORT).show()
    }

    private var exitTime: Long = 0
    fun quitClick(activity: Activity, event: KeyEvent, s: String, exit: Boolean): Boolean {
        return if (event.action == KeyEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - exitTime > 2000) {
                toast(activity, s)
                exitTime = System.currentTimeMillis()
            } else {
                if (exit) {
                    val home = Intent(Intent.ACTION_MAIN)
                    home.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    home.addCategory(Intent.CATEGORY_HOME)
                    activity.startActivity(home)
                } else {
                    activity.finish()
                }
            }
            true
        } else {
            false
        }
    }

    fun postError(title:String,message:String){
        EventBus.getDefault().postSticky(
            ErrorEvent(
                title,
                message
            )
        )
    }

    fun networkError(){
        postError("神秘网络错误", "科学上网了解一下!!!???")
    }

    fun checkInternetAvailable(context: Context):Boolean{
        if(!isInternetAvailable(context)){
            postError("网络不可用", "都没联网你刷个啥...")
            return false
        }
        return true
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}