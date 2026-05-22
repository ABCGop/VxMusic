package com.maxrave.data.di.loader

import com.simpmusic.media_jvm.di.loadVlcModule

actual fun loadMediaService() {
    loadVlcModule()
}
