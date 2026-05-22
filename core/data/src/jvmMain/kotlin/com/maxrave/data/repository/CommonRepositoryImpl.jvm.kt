package com.maxrave.data.repository

import com.maxrave.domain.data.model.cookie.CookieItem
import java.net.Authenticator
import java.net.PasswordAuthentication

actual fun setProxyAuthenticator(username: String, password: String) {
    Authenticator.setDefault(
        object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication? {
                if (requestorType == RequestorType.PROXY) {
                    return PasswordAuthentication(username, password.toCharArray())
                }
                return null
            }
        },
    )
}

actual fun clearProxyAuthenticator() {
    Authenticator.setDefault(null)
}

actual fun getCookies(url: String, packageName: String): CookieItem = CookieItem(url, emptyList())