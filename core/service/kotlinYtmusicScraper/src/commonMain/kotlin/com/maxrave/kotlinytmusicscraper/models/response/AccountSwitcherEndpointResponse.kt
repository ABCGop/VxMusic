package com.maxrave.kotlinytmusicscraper.models.response

import com.maxrave.kotlinytmusicscraper.models.AccountInfo
import com.maxrave.kotlinytmusicscraper.models.Run
import com.maxrave.kotlinytmusicscraper.models.Thumbnail
import kotlinx.serialization.Serializable

@Serializable
data class AccountSwitcherEndpointResponse(
    val code: String?,
    val data: AccountSwitcherData?,
)

@Serializable
data class AccountSwitcherData(
    val actions: List<AccountSwitcherAction?>?,
    val contents: List<AccountSwitcherSection>?,
    val responseContext: AccountSwitcherResponseContext?,
    val selectText: AccountSwitcherSelectText?,
)

@Serializable
data class AccountSwitcherAction(
    val getMultiPageMenuAction: AccountSwitcherGetMultiPageMenuAction?,
)

@Serializable
data class AccountSwitcherGetMultiPageMenuAction(
    val menu: AccountSwitcherMenu?,
)

@Serializable
data class AccountSwitcherMenu(
    val multiPageMenuRenderer: AccountSwitcherMultiPageMenuRenderer?,
)

@Serializable
data class AccountSwitcherMultiPageMenuRenderer(
    val footer: AccountSwitcherFooter?,
    val header: AccountSwitcherMenuHeader?,
    val sections: List<AccountSwitcherSection?>?,
    val style: String?,
)

@Serializable
data class AccountSwitcherFooter(
    val multiPageMenuSectionRenderer: AccountSwitcherMultiPageMenuSectionRenderer?,
)

@Serializable
data class AccountSwitcherMultiPageMenuSectionRenderer(
    val items: List<AccountSwitcherMenuItem?>?,
)

@Serializable
data class AccountSwitcherMenuItem(
    val compactLinkRenderer: AccountSwitcherCompactLinkRenderer?,
)

@Serializable
data class AccountSwitcherCompactLinkRenderer(
    val icon: AccountSwitcherCompactLinkIcon?,
    val navigationEndpoint: AccountSwitcherCompactLinkNavigationEndpoint?,
    val style: String?,
    val title: AccountSwitcherCompactLinkTitle?,
)

@Serializable
data class AccountSwitcherCompactLinkIcon(
    val iconType: String?,
)

@Serializable
data class AccountSwitcherCompactLinkNavigationEndpoint(
    val signOutEndpoint: AccountSwitcherSignOutEndpoint?,
    val urlEndpoint: AccountSwitcherUrlEndpoint?,
)

@Serializable
data class AccountSwitcherSignOutEndpoint(
    val hack: Boolean?,
)

@Serializable
data class AccountSwitcherUrlEndpoint(
    val url: String?,
)

@Serializable
data class AccountSwitcherCompactLinkTitle(
    val runs: List<Run?>?,
)

@Serializable
data class AccountSwitcherMenuHeader(
    val simpleMenuHeaderRenderer: AccountSwitcherSimpleMenuHeaderRenderer?,
)

@Serializable
data class AccountSwitcherSimpleMenuHeaderRenderer(
    val backButton: AccountSwitcherBackButton?,
    val title: AccountSwitcherSimpleMenuTitle?,
)

@Serializable
data class AccountSwitcherBackButton(
    val buttonRenderer: AccountSwitcherButtonRenderer?,
)

@Serializable
data class AccountSwitcherButtonRenderer(
    val accessibility: AccountSwitcherButtonAccessibility?,
    val accessibilityData: AccountSwitcherButtonAccessibilityDataWrapper?,
    val icon: AccountSwitcherButtonIcon?,
    val isDisabled: Boolean?,
    val size: String?,
    val style: String?,
)

@Serializable
data class AccountSwitcherButtonAccessibility(
    val label: String?,
)

@Serializable
data class AccountSwitcherButtonAccessibilityDataWrapper(
    val accessibilityData: AccountSwitcherButtonAccessibilityData?,
)

@Serializable
data class AccountSwitcherButtonAccessibilityData(
    val label: String?,
)

@Serializable
data class AccountSwitcherButtonIcon(
    val iconType: String?,
)

@Serializable
data class AccountSwitcherSimpleMenuTitle(
    val runs: List<Run?>?,
)

@Serializable
data class AccountSwitcherSection(
    val accountSectionListRenderer: AccountSectionListRenderer?,
)

@Serializable
data class AccountSectionListRenderer(
    val contents: List<AccountSectionContent?>?,
    val header: AccountSectionHeader?,
)

@Serializable
data class AccountSectionContent(
    val accountItemSectionRenderer: AccountItemSectionRenderer?,
)

@Serializable
data class AccountItemSectionRenderer(
    val contents: List<AccountItemContent?>?,
    val header: AccountItemSectionHeaderWrapper?,
)

@Serializable
data class AccountItemContent(
    val accountItem: AccountItem?,
)

@Serializable
data class AccountItem(
    val onBehalfOfParameter: String?,
    val accountByline: AccountByline?,
    val accountLogDirectiveInts: List<Int?>?,
    val accountName: AccountName?,
    val accountPhoto: AccountPhoto?,
    val channelHandle: ChannelHandle?,
    val hasChannel: Boolean?,
    val isDisabled: Boolean?,
    val isSelected: Boolean?,
    val mobileBanner: MobileBanner?,
    val serviceEndpoint: AccountItemServiceEndpoint?,
    val unlimitedStatus: List<AccountItemUnlimitedStatus?>?,
) {
    fun toAccountInfo(email: String): AccountInfo? {
        return AccountInfo(
            name = accountName?.simpleText ?: return null,
            email = email,
            pageId =
                onBehalfOfParameter
                    ?: serviceEndpoint
                        ?.selectActiveIdentityEndpoint
                        ?.supportedTokens
                        ?.firstOrNull { it?.pageIdToken != null }
                        ?.pageIdToken
                        ?.pageId,
            thumbnails = accountPhoto?.thumbnails?.filterNotNull() ?: emptyList(),
        )
    }
}

@Serializable
data class AccountByline(
    val simpleText: String,
)

@Serializable
data class AccountName(
    val simpleText: String,
)

@Serializable
data class AccountPhoto(
    val thumbnails: List<Thumbnail?>?,
)

@Serializable
data class ChannelHandle(
    val simpleText: String,
)

@Serializable
data class MobileBanner(
    val thumbnails: List<Thumbnail?>?,
)

@Serializable
data class AccountItemServiceEndpoint(
    val selectActiveIdentityEndpoint: SelectActiveIdentityEndpoint?,
)

@Serializable
data class AccountItemUnlimitedStatus(
    val runs: List<Run?>?,
)

@Serializable
data class AccountItemSectionHeaderWrapper(
    val accountItemSectionHeaderRenderer: AccountItemSectionHeaderRenderer?,
)

@Serializable
data class AccountItemSectionHeaderRenderer(
    val title: AccountItemSectionHeaderTitle?,
)

@Serializable
data class AccountItemSectionHeaderTitle(
    val runs: List<Run?>?,
)

@Serializable
data class AccountSectionHeader(
    val accountsDialogHeaderRenderer: AccountsDialogHeaderRenderer?,
    val googleAccountHeaderRenderer: GoogleAccountHeaderRenderer?,
)

@Serializable
data class AccountsDialogHeaderRenderer(
    val text: AccountsDialogHeaderText?,
)

@Serializable
data class AccountsDialogHeaderText(
    val runs: List<Run?>?,
)

@Serializable
data class GoogleAccountHeaderRenderer(
    val email: GoogleAccountEmail?,
    val name: GoogleAccountName?,
)

@Serializable
data class GoogleAccountEmail(
    val runs: List<Run?>?,
)

@Serializable
data class GoogleAccountName(
    val runs: List<Run?>?,
)

@Serializable
data class AccountSwitcherResponseContext(
    val serviceTrackingParams: List<AccountSwitcherServiceTrackingParam?>?,
)

@Serializable
data class AccountSwitcherServiceTrackingParam(
    val params: List<AccountSwitcherTrackingParam?>?,
    val service: String?,
)

@Serializable
data class AccountSwitcherTrackingParam(
    val key: String?,
    val value: String?,
)

@Serializable
data class AccountSwitcherSelectText(
    val runs: List<Run?>?,
)

@Serializable
data class SelectActiveIdentityEndpoint(
    val supportedTokens: List<SupportedToken?>?,
)

@Serializable
data class SupportedToken(
    val accountSigninToken: AccountSigninToken?,
    val accountStateToken: AccountStateToken?,
    val datasyncIdToken: DatasyncIdToken?,
    val offlineCacheKeyToken: OfflineCacheKeyToken?,
    val pageIdToken: PageIdToken?,
)

@Serializable
data class AccountSigninToken(
    val signinUrl: String?,
)

@Serializable
data class AccountStateToken(
    val hasChannel: Boolean?,
    val isMerged: Boolean?,
    val obfuscatedGaiaId: String?,
)

@Serializable
data class DatasyncIdToken(
    val datasyncIdToken: String?,
)

@Serializable
data class OfflineCacheKeyToken(
    val clientCacheKey: String?,
)

@Serializable
data class PageIdToken(
    val pageId: String?,
)

fun AccountSwitcherEndpointResponse.toListAccountInfo(): List<AccountInfo> {
    if (this.code == "SUCCESS" && this.data != null) {
        val list = mutableListOf<AccountInfo>()
        this.data.contents
            ?.firstOrNull()
            ?.accountSectionListRenderer
            ?.contents
            ?.firstOrNull()
            ?.accountItemSectionRenderer
            ?.contents
            ?.forEach { content ->
                content?.accountItem?.let { accountItem ->
                    accountItem
                        .toAccountInfo(
                            email =
                                accountItem.channelHandle
                                    ?.simpleText ?: "",
                        )?.let {
                            list.add(it)
                        }
                }
            }
        return list
    } else {
        return emptyList()
    }
}
