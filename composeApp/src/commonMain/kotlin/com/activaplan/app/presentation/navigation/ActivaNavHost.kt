package com.activaplan.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.activaplan.app.presentation.explore.ExploreScreen
import com.activaplan.app.presentation.home.HomeScreen
import com.activaplan.app.presentation.routines.RoutinesScreen
import com.activaplan.app.presentation.schedule.ScheduleScreen
import com.activaplan.app.presentation.settings.SettingsScreen

@Composable
fun ActivaNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController, startDestination = TopLevelDestination.start.route, modifier = modifier) {
        composable(TopLevelDestination.HOME.route) { HomeScreen() }
        composable(TopLevelDestination.EXPLORE.route) { ExploreScreen() }
        composable(TopLevelDestination.ROUTINES.route) { RoutinesScreen() }
        composable(TopLevelDestination.SCHEDULE.route) { ScheduleScreen() }
        composable(TopLevelDestination.SETTINGS.route) { SettingsScreen() }
    }
}

/** Pindah tab: satu salinan per tab, state tab tersimpan, dan Back dari tab mana pun kembali ke Beranda. */
fun NavHostController.navigateToTab(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
