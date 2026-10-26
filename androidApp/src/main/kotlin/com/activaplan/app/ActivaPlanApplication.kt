package com.activaplan.app

import android.app.Application
import com.activaplan.app.di.initKoin
import org.koin.android.ext.koin.androidContext

class ActivaPlanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@ActivaPlanApplication) }
    }
}
