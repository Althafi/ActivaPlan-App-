package com.activaplan.app.presentation.navigation

import com.activaplan.app.designsystem.components.ActivaIconKind
import com.activaplan.app.domain.i18n.StringKey

/** Lima tab bottom nav, berurutan seperti di Figma. [route] unik dan dipakai NavHost. */
enum class TopLevelDestination(val route: String, val label: StringKey, val icon: ActivaIconKind) {
    HOME("home", StringKey.NAV_HOME, ActivaIconKind.HOME),
    EXPLORE("explore", StringKey.NAV_EXPLORE, ActivaIconKind.SEARCH),
    ROUTINES("routines", StringKey.NAV_ROUTINES, ActivaIconKind.LIST),
    SCHEDULE("schedule", StringKey.NAV_SCHEDULE, ActivaIconKind.CALENDAR),
    SETTINGS("settings", StringKey.NAV_SETTINGS, ActivaIconKind.SLIDERS),
    ;

    companion object {
        val start = HOME

        fun fromRoute(route: String?): TopLevelDestination? = entries.firstOrNull { it.route == route }
    }
}
