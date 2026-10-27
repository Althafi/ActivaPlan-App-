package com.activaplan.app.info

import com.activaplan.app.domain.info.AppInfo
import platform.Foundation.NSBundle

class IosAppInfo : AppInfo {
    override val versionName: String =
        NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: ""
}
