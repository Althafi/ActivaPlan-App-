package com.activaplan.app.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import com.activaplan.app.domain.model.ThemeMode
import platform.UIKit.UIApplication
import platform.UIKit.UIUserInterfaceStyleDark
import platform.UIKit.UIUserInterfaceStyleLight
import platform.UIKit.UIUserInterfaceStyleUnspecified
import platform.UIKit.UIWindow

@Composable
actual fun ApplySystemAppearance(theme: ThemeMode, darkTheme: Boolean) {
    SideEffect {
        // Unspecified menyerahkan keputusan ke perangkat, jadi mode "Ikuti sistem" tetap ikut berubah.
        val style = when (theme) {
            ThemeMode.LIGHT -> UIUserInterfaceStyleLight
            ThemeMode.DARK -> UIUserInterfaceStyleDark
            ThemeMode.SYSTEM -> UIUserInterfaceStyleUnspecified
        }
        UIApplication.sharedApplication.windows.forEach { (it as? UIWindow)?.overrideUserInterfaceStyle = style }
    }
}
