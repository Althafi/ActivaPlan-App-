package com.activaplan.app.presentation

import com.activaplan.app.domain.i18n.Language
import com.activaplan.app.domain.i18n.Strings
import com.activaplan.app.presentation.navigation.TopLevelDestination
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TopLevelDestinationTest {
    @Test
    fun hasFiveTabsInFigmaOrder() {
        val labels = TopLevelDestination.entries.map { Strings(Language.ID)[it.label] }
        assertEquals(listOf("Beranda", "Jelajah", "Rutinitas", "Jadwal", "Setelan"), labels)
    }

    @Test
    fun routesAreUniqueAndResolvable() {
        val routes = TopLevelDestination.entries.map { it.route }
        assertEquals(routes.size, routes.toSet().size)
        TopLevelDestination.entries.forEach { assertEquals(it, TopLevelDestination.fromRoute(it.route)) }
        assertNull(TopLevelDestination.fromRoute("tidak-ada"))
        assertNull(TopLevelDestination.fromRoute(null))
    }

    @Test
    fun startsAtHome() = assertEquals(TopLevelDestination.HOME, TopLevelDestination.start)
}
