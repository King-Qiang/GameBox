package me.wangqiang.gamebox.app

import android.app.Application
import android.content.Context
import com.alibaba.android.arouter.launcher.ARouter

class MainApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        if (BuildConfig.DEBUG) {
            ARouter.openLog();     // 打印日志
            ARouter.openDebug();
        }
        ARouter.init(this)
    }
}