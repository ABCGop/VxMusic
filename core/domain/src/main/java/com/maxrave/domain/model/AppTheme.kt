package com.maxrave.domain.model

data class AppTheme(
    val id: String,
    val name: String,
    val isPro: Boolean,
    val primaryColor: Long,
    val secondaryColor: Long,
    val backgroundColor: Long,
    val accentColor: Long,
    val surfaceColor: Long,
    val onPrimary: Long,
    val onSecondary: Long,
    val onBackground: Long
)
