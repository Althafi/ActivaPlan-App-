package com.activaplan.app.info

import android.content.Context
import com.activaplan.app.domain.info.AppInfo

class AndroidAppInfo(private val context: Context) : AppInfo {
    override val versionName: String by lazy {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
        } catch (_: Exception) {
            ""
        }
    }
}
