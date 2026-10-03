package com.abcg.music.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.abcg.music.R
import com.abcg.music.playback.SignalPathReport
import java.util.Locale

/**
 * Detailed signal-path popup opened from the quality badge (FLAC 24/96…).
 * Every row is a measured check — the BIT-PERFECT verdict appears only when
 * all of them pass. Drift refreshes live while playing (1 Hz report ticks).
 */
@Composable
fun SignalPathDialog(
    report: SignalPathReport,
    needsUsbPermission: Boolean,
    onRequestUsbAccess: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.signal_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    VerdictPill(bitPerfect = report.bitPerfect)
                }
                Text(
                    verdictText(report),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (report.bitPerfect) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(top = 4.dp),
                )
                Spacer(Modifier.height(12.dp))

                report.checks.forEach { check ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 5.dp)
                                .size(8.dp)
                                .background(
                                    if (check.passed) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.error,
                                    CircleShape,
                                ),
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                stringResource(check.labelRes),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                stringResource(check.detailRes, *check.detailArgs.toTypedArray()),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.signal_health),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(4.dp))
                val measuring = stringResource(R.string.signal_measuring)
                HealthRow(
                    stringResource(R.string.signal_drift),
                    report.driftPpm?.let { String.format(Locale.ROOT, "%+.1f PPM", it) }
                        ?: if (report.isPlaying) measuring else "—",
                )
                val streamState = if (report.isPlaying) {
                    stringResource(R.string.signal_playing)
                } else {
                    stringResource(R.string.signal_idle)
                }
                val sharedTrack = stringResource(R.string.signal_shared_track)
                HealthRow(
                    stringResource(R.string.signal_stream),
                    buildString {
                        append(streamState)
                        if (report.appRateHz > 0) append(" • ${report.appRateHz} Hz")
                        append(" • $sharedTrack")
                    },
                )
                HealthRow(stringResource(R.string.signal_glitches), report.glitchCount.toString())

                if (needsUsbPermission) {
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onRequestUsbAccess,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.signal_grant_usb))
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_close)) }
                }
            }
        }
    }
}

/** Verdict line: confirmation when perfect, otherwise the first failing check. */
@Composable
private fun verdictText(report: SignalPathReport): String {
    if (report.bitPerfect) return stringResource(R.string.signal_verdict_ok)
    val failing = report.checks.firstOrNull { !it.passed } ?: return ""
    return stringResource(failing.labelRes) +
        ": " +
        stringResource(failing.detailRes, *failing.detailArgs.toTypedArray())
}

@Composable
private fun VerdictPill(bitPerfect: Boolean) {
    val bg = if (bitPerfect) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val fg = if (bitPerfect) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            if (bitPerfect) stringResource(R.string.signal_bit_perfect) else stringResource(R.string.signal_check_path),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = fg,
        )
    }
}

@Composable
private fun HealthRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
