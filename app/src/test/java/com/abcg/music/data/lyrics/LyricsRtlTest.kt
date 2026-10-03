package com.abcg.music.data.lyrics

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LyricsRtlTest {

    @Test
    fun isRtlText_arabicPhrases_returnTrue() {
        assertThat(isRtlText("حبيبي يا نور العين")).isTrue()
        assertThat(isRtlText("يا مسافر وحدك")).isTrue()
        assertThat(isRtlText("انت عمري")).isTrue()
    }

    @Test
    fun isRtlText_hebrewAndFarsi_returnTrue() {
        assertThat(isRtlText("שלום עליכם")).isTrue()
        assertThat(isRtlText("ای ایران ای مرز پرگهر")).isTrue()
    }

    @Test
    fun isRtlText_latinText_returnsFalse() {
        assertThat(isRtlText("Never gonna give you up")).isFalse()
        assertThat(isRtlText("Habibi ya nour el ain")).isFalse()
        assertThat(isRtlText("Hello World 123")).isFalse()
    }

    @Test
    fun isRtlText_arabicWithLeadingSymbolsAndPunctuation_returnsTrue() {
        assertThat(isRtlText("♪ حبيبي ♪")).isTrue()
        assertThat(isRtlText("(حبيبي يا نور العين)")).isTrue()
        assertThat(isRtlText("1. حبيبي")).isTrue()
        assertThat(isRtlText("2024 سنة سعيدة")).isTrue()
    }

    @Test
    fun isRtlText_mixedEnglishAndArabic_dominantArabicReturnsTrue() {
        assertThat(isRtlText("DJ Snake حبيبي يا نور العين")).isTrue()
    }

    @Test
    fun isRtlText_blankOrNullOrPunctuationOnly_returnsFalse() {
        assertThat(isRtlText(null)).isFalse()
        assertThat(isRtlText("")).isFalse()
        assertThat(isRtlText("   ")).isFalse()
        assertThat(isRtlText("♪♪♪")).isFalse()
        assertThat(isRtlText("12345")).isFalse()
        assertThat(isRtlText("... !?")).isFalse()
    }

    @Test
    fun lyricLine_isRtl_respectsTextAndSyllables() {
        val arabicLine = LyricLine(
            timeMs = 1000L,
            text = "حبيبي يا نور العين",
            syllables = listOf(
                LyricSyllable(timeMs = 1000L, durationMs = 500L, text = "حبيبي "),
                LyricSyllable(timeMs = 1500L, durationMs = 500L, text = "يا "),
                LyricSyllable(timeMs = 2000L, durationMs = 500L, text = "نور "),
                LyricSyllable(timeMs = 2500L, durationMs = 500L, text = "العين"),
            ),
        )
        assertThat(arabicLine.isRtl).isTrue()

        val englishLine = LyricLine(
            timeMs = 1000L,
            text = "Never gonna give you up",
            syllables = listOf(
                LyricSyllable(timeMs = 1000L, durationMs = 500L, text = "Never "),
                LyricSyllable(timeMs = 1500L, durationMs = 500L, text = "gonna "),
            ),
        )
        assertThat(englishLine.isRtl).isFalse()

        val emptyTextWithArabicSyllable = LyricLine(
            timeMs = 1000L,
            text = "",
            syllables = listOf(
                LyricSyllable(timeMs = 1000L, durationMs = 500L, text = "حبيبي"),
            ),
        )
        assertThat(emptyTextWithArabicSyllable.isRtl).isTrue()
    }
}
