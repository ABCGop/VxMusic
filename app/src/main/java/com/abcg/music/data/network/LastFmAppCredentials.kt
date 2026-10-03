package com.abcg.music.data.network

/**
 * LastWave's own registered Last.fm API application, baked into the app
 * itself — per explicit request, so nobody signing in ever has to go find
 * or paste an API key/secret. This is the same pattern most published
 * Last.fm apps use: one app-level key identifies the APPLICATION to
 * Last.fm, not the individual person signing into it; each person's own
 * identity comes from the session key they get during their own sign-in
 * (see AuthRepository.beginWebAuth / completeWebAuth), not from this key.
 *
 * Registered at last.fm/api/account/create under the app name "LastWave".
 */
object LastFmAppCredentials {
    val API_KEY: String
        get() = com.abcg.music.data.lossless.LosslessMusicApi.decodeSecretBytes(
            com.abcg.music.BuildConfig.LASTFM_API_KEY_BYTES,
            com.abcg.music.BuildConfig.SECRET_MASK_BYTES
        )

    val API_SECRET: String
        get() = com.abcg.music.data.lossless.LosslessMusicApi.decodeSecretBytes(
            com.abcg.music.BuildConfig.LASTFM_API_SECRET_BYTES,
            com.abcg.music.BuildConfig.SECRET_MASK_BYTES
        )
}
