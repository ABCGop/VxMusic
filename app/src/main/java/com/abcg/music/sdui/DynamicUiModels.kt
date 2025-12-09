package com.abcg.music.sdui

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DynamicScreen(
    val id: String,
    val sections: List<DynamicSection> = emptyList()
)

@Serializable
data class DynamicSection(
    val id: String,
    val type: SectionType,
    val content: DynamicContent,
    val action: DynamicAction? = null,
    val style: DynamicStyle? = null
)

@Serializable
enum class SectionType {
    @SerialName("banner") BANNER,
    @SerialName("card_list") CARD_LIST,
    @SerialName("text_block") TEXT_BLOCK,
    @SerialName("button") BUTTON,
    @SerialName("spacer") SPACER
}

@Serializable
data class DynamicContent(
    val title: String? = null,
    val subtitle: String? = null,
    val imageUrl: String? = null,
    val items: List<DynamicSection>? = null // For nested lists like horizontal scrolls
)

@Serializable
data class DynamicAction(
    val type: ActionType,
    val value: String
)

@Serializable
enum class ActionType {
    @SerialName("url") OPEN_URL,
    @SerialName("navigate") NAVIGATE,
    @SerialName("toast") SHOW_TOAST,
    @SerialName("dialog") SHOW_DIALOG
}

@Serializable
data class DynamicStyle(
    val backgroundColor: String? = null,
    val textColor: String? = null,
    val padding: Int? = null,
    val cornerRadius: Int? = null
)
