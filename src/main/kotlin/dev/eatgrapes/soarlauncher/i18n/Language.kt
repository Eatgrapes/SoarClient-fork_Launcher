package dev.eatgrapes.soarlauncher.i18n

object Language {
    const val ENGLISH = "en_US"
    const val CHINESE_SIMPLIFIED = "zh_CN"
    
    val DEFAULT = ENGLISH
    
    val ALL = listOf(
        ENGLISH,
        CHINESE_SIMPLIFIED
    )
    
    fun getDisplayName(code: String): String = when (code) {
        ENGLISH -> "English (US)"
        CHINESE_SIMPLIFIED -> "中文 (简体)"
        else -> code
    }
}