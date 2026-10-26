package com.activaplan.app.domain.i18n

import java.util.Locale

actual fun systemLanguageTag(): String = Locale.getDefault().toLanguageTag()
