package dev.eatgrapes.soarlauncher.i18n

import dev.eatgrapes.soarlauncher.i18n.TranslationManager

object i18n {
    fun text(key: String, default: String = key): String {
        return TranslationManager.getTranslation(key, default)
    }
    
    fun t(key: String, default: String = key): String = text(key, default)
}