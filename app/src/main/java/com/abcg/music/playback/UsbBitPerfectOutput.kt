package com.abcg.music.playback

import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioMixerAttributes
import android.os.Build

/** Requests the platform USB mode only for the PCM format actually sent to AudioTrack. */
class UsbBitPerfectOutput(private val manager: AudioManager?) {
    private var device: AudioDeviceInfo? = null
    private var format: AudioFormat? = null
    private var enabled = false
    private var attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
    private var requestedDevice: AudioDeviceInfo? = null

    @Synchronized
    fun setDevice(value: AudioDeviceInfo?) {
        if (device?.id == value?.id) return
        clear()
        device = value
        apply()
    }

    @Synchronized
    fun setEnabled(value: Boolean) {
        if (enabled == value) return
        enabled = value
        apply()
    }

    @Synchronized
    fun setAttributes(value: AudioAttributes) {
        if (attributes == value) return
        clear()
        attributes = value
        apply()
    }

    @Synchronized
    fun setFormat(value: AudioFormat?) {
        if (format == value) return
        clear()
        format = value
        apply()
    }

    @Synchronized
    fun isConfigured(): Boolean {
        if (Build.VERSION.SDK_INT < 34 || !enabled || requestedDevice == null) return false
        return runCatching {
            val preferred = manager?.getPreferredMixerAttributes(attributes, requestedDevice!!)
            preferred?.mixerBehavior == AudioMixerAttributes.MIXER_BEHAVIOR_BIT_PERFECT &&
                preferred.format == format
        }.getOrDefault(false)
    }

    private fun apply() {
        if (Build.VERSION.SDK_INT < 34) return
        val target = device
        val pcm = format
        if (!enabled || target == null || pcm == null) {
            clear()
            return
        }
        if (target.type != AudioDeviceInfo.TYPE_USB_DEVICE && target.type != AudioDeviceInfo.TYPE_USB_HEADSET) return
        runCatching {
            val supported = manager?.getSupportedMixerAttributes(target)?.firstOrNull {
                it.mixerBehavior == AudioMixerAttributes.MIXER_BEHAVIOR_BIT_PERFECT && it.format == pcm
            } ?: return
            if (manager?.setPreferredMixerAttributes(attributes, target, supported) == true) requestedDevice = target
        }
    }

    private fun clear() {
        val previous = requestedDevice
        requestedDevice = null
        if (Build.VERSION.SDK_INT >= 34 && previous != null) {
            runCatching { manager?.clearPreferredMixerAttributes(attributes, previous) }
        }
    }
}
