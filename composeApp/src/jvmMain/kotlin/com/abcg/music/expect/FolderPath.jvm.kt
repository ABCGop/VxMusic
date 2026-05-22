package com.abcg.music.expect

actual fun getDownloadFolderPath(): String = System.getProperty("user.home") + "/Downloads"