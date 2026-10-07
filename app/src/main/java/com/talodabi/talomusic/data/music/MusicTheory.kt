package com.talodabi.talomusic.data.music

enum class PitchClass(val semitone: Int, val sharpName: String, val flatName: String) {
    C(0, "C", "C"),
    C_SHARP(1, "C#", "Db"),
    D(2, "D", "D"),
    D_SHARP(3, "D#", "Eb"),
    E(4, "E", "E"),
    F(5, "F", "F"),
    F_SHARP(6, "F#", "Gb"),
    G(7, "G", "G"),
    G_SHARP(8, "G#", "Ab"),
    A(9, "A", "A"),
    A_SHARP(10, "A#", "Bb"),
    B(11, "B", "B");

    fun transpose(semitones: Int): PitchClass {
        val next = Math.floorMod(this.semitone + semitones, 12)
        return fromSemitone(next)
    }

    companion object {
        fun fromSemitone(semi: Int): PitchClass {
            val normalized = Math.floorMod(semi, 12)
            return entries.first { it.semitone == normalized }
        }

        fun fromString(str: String): PitchClass {
            val clean = str.trim()
            return entries.firstOrNull {
                it.sharpName.equals(clean, ignoreCase = true) || it.flatName.equals(clean, ignoreCase = true)
            } ?: C
        }
    }
}

enum class ChordQuality(val symbol: String, val isMinor: Boolean) {
    MAJOR("", false),
    MINOR("m", true),
    DOMINANT_7("7", false),
    MAJOR_7("maj7", false),
    MINOR_7("m7", true),
    SUS_4("sus4", false),
    SUS_2("sus2", false),
    DIMINISHED("dim", true),
    AUGMENTED("aug", false),
    ADD_9("add9", false);

    companion object {
        fun fromString(str: String): ChordQuality {
            return when (str.trim().lowercase()) {
                "m", "min", "minor" -> MINOR
                "7", "dom7" -> DOMINANT_7
                "maj7", "major7", "m7+" -> MAJOR_7
                "m7", "min7" -> MINOR_7
                "sus4", "sus" -> SUS_4
                "sus2" -> SUS_2
                "dim", "o" -> DIMINISHED
                "aug", "+" -> AUGMENTED
                "add9" -> ADD_9
                else -> MAJOR
            }
        }
    }
}

enum class KeyMode {
    MAJOR, MINOR
}

data class MusicChord(
    val rootPitchClass: Int, // 0 = C, 1 = C#, ..., 11 = B
    val quality: ChordQuality = ChordQuality.MAJOR,
    val bassPitchClass: Int? = null
) {
    fun transpose(semitones: Int): MusicChord {
        val newRoot = Math.floorMod(rootPitchClass + semitones, 12)
        val newBass = bassPitchClass?.let { Math.floorMod(it + semitones, 12) }
        return copy(rootPitchClass = newRoot, bassPitchClass = newBass)
    }

    fun toDisplayString(useFlats: Boolean = false): String {
        val root = PitchClass.fromSemitone(rootPitchClass)
        val rootName = if (useFlats) root.flatName else root.sharpName
        val bassPart = if (bassPitchClass != null) {
            val bass = PitchClass.fromSemitone(bassPitchClass)
            "/" + (if (useFlats) bass.flatName else bass.sharpName)
        } else ""
        return "$rootName${quality.symbol}$bassPart"
    }

    /**
     * Converts the chord to Roman Numeral representation relative to song key.
     * E.g. in C Major: C -> I, Am -> vi, F -> IV, G -> V
     */
    fun toRomanNumeral(keyTonic: Int, keyMode: KeyMode = KeyMode.MAJOR): String {
        val interval = Math.floorMod(rootPitchClass - keyTonic, 12)
        val (numeral, isMajorDeg) = when (interval) {
            0 -> "I" to true
            1 -> "bII" to false
            2 -> "II" to false
            3 -> "bIII" to true
            4 -> "III" to false
            5 -> "IV" to true
            6 -> "bV" to false
            7 -> "V" to true
            8 -> "bVI" to true
            9 -> "VI" to false
            10 -> "bVII" to true
            11 -> "VII" to false
            else -> "?" to true
        }

        val baseNumeral = if (quality.isMinor) numeral.lowercase() else numeral
        val suffix = when (quality) {
            ChordQuality.DOMINANT_7 -> "7"
            ChordQuality.MAJOR_7 -> "maj7"
            ChordQuality.MINOR_7 -> "7"
            ChordQuality.SUS_4 -> "sus4"
            ChordQuality.DIMINISHED -> "°"
            ChordQuality.AUGMENTED -> "+"
            ChordQuality.ADD_9 -> "add9"
            else -> ""
        }
        return "$baseNumeral$suffix"
    }

    /**
     * Converts to Nashville Number notation relative to song key.
     * E.g. in C Major: C -> 1, Am -> 6m, F -> 4, G -> 5
     */
    fun toNashvilleNumber(keyTonic: Int): String {
        val interval = Math.floorMod(rootPitchClass - keyTonic, 12)
        val (number, accidental) = when (interval) {
            0 -> "1" to ""
            1 -> "2" to "b"
            2 -> "2" to ""
            3 -> "3" to "b"
            4 -> "3" to ""
            5 -> "4" to ""
            6 -> "5" to "b"
            7 -> "5" to ""
            8 -> "6" to "b"
            9 -> "6" to ""
            10 -> "7" to "b"
            11 -> "7" to ""
            else -> "?" to ""
        }

        val suffix = when (quality) {
            ChordQuality.MINOR -> "m"
            ChordQuality.DOMINANT_7 -> "7"
            ChordQuality.MAJOR_7 -> "maj7"
            ChordQuality.MINOR_7 -> "m7"
            ChordQuality.SUS_4 -> "sus4"
            ChordQuality.DIMINISHED -> "dim"
            ChordQuality.AUGMENTED -> "aug"
            else -> ""
        }
        return "$accidental$number$suffix"
    }

    companion object {
        fun parse(chordStr: String): MusicChord {
            val trimmed = chordStr.trim()
            if (trimmed.isEmpty()) return MusicChord(0)

            val parts = trimmed.split("/")
            val mainPart = parts[0]
            val bassPart = if (parts.size > 1) parts[1] else null

            // Detect root
            val rootStr: String
            val qualityStr: String
            if (mainPart.length > 1 && (mainPart[1] == '#' || mainPart[1] == 'b')) {
                rootStr = mainPart.substring(0, 2)
                qualityStr = mainPart.substring(2)
            } else {
                rootStr = mainPart.substring(0, 1)
                qualityStr = mainPart.substring(1)
            }

            val root = PitchClass.fromString(rootStr)
            val quality = ChordQuality.fromString(qualityStr)
            val bass = bassPart?.let { PitchClass.fromString(it).semitone }

            return MusicChord(root.semitone, quality, bass)
        }
    }
}
