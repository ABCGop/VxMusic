package com.maxrave.data.repository

import com.maxrave.domain.data.model.cookie.CookieItem

actual fun setProxyAuthenticator(username: String, password: String) {
    // iOS does not support java.net.Authenticator; SOCKS proxy auth is not available
}

actual fun clearProxyAuthenticator() {
    // No-op on iOS
}

actual fun getCookies(url: String, packageName: String): CookieItem = CookieItem(url, emptyList())