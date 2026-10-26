package com.activaplan.app.domain.i18n

import com.activaplan.app.domain.model.AppLanguage

/** Bahasa yang benar-benar tersedia; [code] dipakai sebagai kunci di kamus. */
enum class Language(val code: String) {
    ID("id"),
    EN("en"),
}

/** Tag bahasa perangkat saat ini, mis. "id", "id-ID", "en_US". */
expect fun systemLanguageTag(): String

/** Bahasa selain Indonesia memakai Inggris. "in" adalah kode lama Android untuk Indonesia. */
fun languageFromTag(tag: String): Language {
    val primary = tag.lowercase().substringBefore('-').substringBefore('_')
    return if (primary == "id" || primary == "in") Language.ID else Language.EN
}

fun AppLanguage.resolve(systemTag: String = systemLanguageTag()): Language = when (this) {
    AppLanguage.ID -> Language.ID
    AppLanguage.EN -> Language.EN
    AppLanguage.SYSTEM -> languageFromTag(systemTag)
}
