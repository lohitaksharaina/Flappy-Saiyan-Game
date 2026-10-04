package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.model.CharacterType
import com.example.model.SaiyanForm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class SaiyanAudioEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private val scope = CoroutineScope(Dispatchers.Default)

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val _isAudioPlaying = MutableStateFlow(false)
    val isAudioPlaying: StateFlow<Boolean> = _isAudioPlaying.asStateFlow()

    private val _currentVoiceLine = MutableStateFlow<String?>(null)
    val currentVoiceLine: StateFlow<String?> = _currentVoiceLine.asStateFlow()

    // Pre-rendered PCM buffers for zero latency
    private var flapPcm: ByteArray? = null
    private var scorePcm: ByteArray? = null
    private var hitPcm: ByteArray? = null
    private var kamehamehaPcm: ByteArray? = null
    private var finalFlashPcm: ByteArray? = null
    private val auraPcmCache = mutableMapOf<SaiyanForm, ByteArray>()

    private var activeTrack: AudioTrack? = null
    private var playbackJob: Job? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
        scope.launch {
            generateAudioBuffers()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.JAPANESE)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                val result2 = tts?.setLanguage(Locale.JAPAN)
                if (result2 == TextToSpeech.LANG_MISSING_DATA || result2 == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale.getDefault())
                }
            }
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isAudioPlaying.value = true
                }
                override fun onDone(utteranceId: String?) {
                    _isAudioPlaying.value = false
                    _currentVoiceLine.value = null
                }
                override fun onError(utteranceId: String?) {
                    _isAudioPlaying.value = false
                    _currentVoiceLine.value = null
                }
            })
            isTtsReady = true
        } else {
            Log.e("SaiyanAudioEngine", "TTS Init failed with status: $status")
        }
    }

    private fun generateAudioBuffers() {
        val sampleRate = 44100
        flapPcm = synthesizeFlap(sampleRate)
        scorePcm = synthesizeScore(sampleRate)
        hitPcm = synthesizeHit(sampleRate)
        kamehamehaPcm = synthesizeKamehameha(sampleRate, 5.0)
        finalFlashPcm = synthesizeFinalFlash(sampleRate, 5.0)

        SaiyanForm.entries.forEach { form ->
            auraPcmCache[form] = synthesizeAuraRoar(sampleRate, 4.0, form)
        }
    }

    /**
     * Plays the 5-second Kamehameha / Galick Gun audio track on game start
     * Spoken in authentic Japanese with Masako Nozawa and Ryō Horikawa voice styles
     */
    fun playStartKamehameha(character: CharacterType) {
        stopCurrentAudio()
        vibrate(200)

        val fullQuote = if (character == CharacterType.GOKU) {
            "か…め…は…め…波ーー！ (Ka... me... ha... me... HAAA!)"
        } else {
            "ギャリック砲ーー！波ーー！ (Galick Hō... HAAA!)"
        }
        _currentVoiceLine.value = fullQuote

        val japaneseSpeech = fullQuote.substringBefore(" (")

        // Masako Nozawa (Goku): pitch 1.35f (high, spirited anime hero)
        // Ryō Horikawa (Vegeta): pitch 0.85f (proud, regal, commanding)
        val pitch = if (character == CharacterType.GOKU) 1.35f else 0.85f
        speakVoice(
            text = japaneseSpeech,
            pitch = pitch,
            rate = 0.95f
        )

        // Play 5.0 second energy charge and blast
        val pcm = if (character == CharacterType.GOKU) kamehamehaPcm else finalFlashPcm
        pcm?.let { playPcmBuffer(it, 44100) }

        // Progressive haptics
        scope.launch {
            delay(1000)
            vibrate(100)
            delay(1200)
            vibrate(150)
            delay(1000)
            vibrate(500)
        }
    }

    /**
     * Plays 4-second audio track of Japanese voice actor transformation shouts
     * layered with custom Japanese battle theme soundtrack and roaring Ki aura
     */
    fun playTransformationAudio(character: CharacterType, form: SaiyanForm) {
        stopCurrentAudio()
        vibrate(350)

        val fullQuote = if (character == CharacterType.GOKU) {
            form.voiceQuoteGoku
        } else {
            form.voiceQuoteVegeta
        }

        if (fullQuote.isNotBlank()) {
            _currentVoiceLine.value = fullQuote
            val japaneseSpeech = fullQuote.substringBefore(" (")

            // Masako Nozawa voice modulation (spirited, energetic, high battle roars)
            // Ryō Horikawa voice modulation (deep, proud, regal Saiyan prince roars)
            val pitch = if (character == CharacterType.GOKU) {
                when {
                    form.isOozaru -> 0.70f // Deep beast roar
                    form == SaiyanForm.SUPER_SAIYAN_3 -> 1.45f
                    form == SaiyanForm.SUPER_SAIYAN_4 -> 1.25f
                    form == SaiyanForm.ULTRA_XENO -> 1.20f
                    form == SaiyanForm.ULTRA_INSTINCT || form == SaiyanForm.MASTERED_ULTRA_INSTINCT -> 1.15f
                    else -> 1.35f
                }
            } else {
                when {
                    form.isOozaru -> 0.65f // Deep beast roar
                    form == SaiyanForm.ULTRA_EGO || form == SaiyanForm.MASTERED_ULTRA_EGO -> 0.78f
                    form == SaiyanForm.SUPER_SAIYAN_4 -> 0.88f
                    form == SaiyanForm.ULTRA_XENO -> 0.82f
                    else -> 0.85f
                }
            }

            speakVoice(japaneseSpeech, pitch = pitch, rate = 1.0f)
        }

        // Japanese soundtrack & roaring aura
        val auraPcm = auraPcmCache[form] ?: synthesizeAuraRoar(44100, 4.0, form)
        playPcmBuffer(auraPcm, 44100)

        scope.launch {
            repeat(4) {
                delay(800)
                vibrate(if (form.isOozaru) 120 else 80)
            }
        }
    }

    fun playFlap() {
        flapPcm?.let { playShortPcm(it, 44100) }
    }

    fun playScore() {
        scorePcm?.let { playShortPcm(it, 44100) }
        vibrate(30)
    }

    fun playHit() {
        stopCurrentAudio()
        hitPcm?.let { playShortPcm(it, 44100) }
        vibrate(400)
    }

    private fun speakVoice(text: String, pitch: Float, rate: Float) {
        if (!isTtsReady) return
        tts?.setPitch(pitch)
        tts?.setSpeechRate(rate)
        val params = android.os.Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "saiyan_speech_${System.currentTimeMillis()}")
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "saiyan_speech")
    }

    private fun playPcmBuffer(pcm: ByteArray, sampleRate: Int) {
        playbackJob?.cancel()
        playbackJob = scope.launch {
            try {
                stopTrack()
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = maxOf(minBufferSize, pcm.size)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(pcm, 0, pcm.size)
                track.play()
                activeTrack = track

                _isAudioPlaying.value = true
                val durationMs = (pcm.size * 1000L) / (sampleRate * 2)
                delay(durationMs)
            } catch (e: Exception) {
                Log.e("SaiyanAudioEngine", "Error playing PCM audio", e)
            } finally {
                _isAudioPlaying.value = false
            }
        }
    }

    private fun playShortPcm(pcm: ByteArray, sampleRate: Int) {
        scope.launch {
            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(pcm.size)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(pcm, 0, pcm.size)
                track.play()
                val durationMs = (pcm.size * 1000L) / (sampleRate * 2)
                delay(durationMs + 50)
                track.stop()
                track.release()
            } catch (e: Exception) {
                // Ignore transient audio error
            }
        }
    }

    fun stopCurrentAudio() {
        playbackJob?.cancel()
        playbackJob = null
        stopTrack()
        tts?.stop()
        _isAudioPlaying.value = false
        _currentVoiceLine.value = null
    }

    private fun stopTrack() {
        try {
            activeTrack?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.stop()
                }
                it.release()
            }
            activeTrack = null
        } catch (e: Exception) {
            // Ignore track release errors
        }
    }

    fun release() {
        stopCurrentAudio()
        tts?.shutdown()
        tts = null
    }

    private fun vibrate(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (e: Exception) {
            // Ignore vibration errors
        }
    }

    /**
     * Synthesize 5.0s Kamehameha charge & beam blast
     */
    private fun synthesizeKamehameha(sampleRate: Int, durationSec: Double): ByteArray {
        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ByteArray(totalSamples * 2)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val sample = if (t < 3.2) {
                val progress = t / 3.2
                val freq = 160.0 + 520.0 * (progress * progress)
                val tremolo = 1.0 + 0.35 * sin(2.0 * PI * (8.0 + 20.0 * progress) * t)
                val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.12 * progress
                (sin(2.0 * PI * freq * t) * tremolo * 0.45 * progress + noise)
            } else {
                val blastT = t - 3.2
                val blastProgress = blastT / 1.8
                val decay = exp(-blastProgress * 2.6)
                val subBass = sin(2.0 * PI * 65.0 * t) * 0.6
                val highBeam = sin(2.0 * PI * 420.0 * t) * 0.35
                val shockNoise = (Random.nextDouble() * 2.0 - 1.0) * 0.4
                ((subBass + highBeam + shockNoise) * decay * 0.85)
            }
            val sampleVal = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
            buffer[i * 2] = (sampleVal.toInt() and 0xFF).toByte()
            buffer[i * 2 + 1] = ((sampleVal.toInt() shr 8) and 0xFF).toByte()
        }
        return buffer
    }

    /**
     * Synthesize 5.0s Final Flash charge & blast
     */
    private fun synthesizeFinalFlash(sampleRate: Int, durationSec: Double): ByteArray {
        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ByteArray(totalSamples * 2)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val sample = if (t < 3.2) {
                val progress = t / 3.2
                val freq = 220.0 + 640.0 * (progress * progress)
                val lightning = if (Random.nextDouble() > 0.98) (Random.nextDouble() * 2.0 - 1.0) * 0.5 else 0.0
                (sin(2.0 * PI * freq * t) * 0.42 * progress + lightning)
            } else {
                val blastT = t - 3.2
                val blastProgress = blastT / 1.8
                val decay = exp(-blastProgress * 2.4)
                val rumble = sin(2.0 * PI * 55.0 * t) * 0.7
                val beam = sin(2.0 * PI * 550.0 * t) * 0.3
                val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.45
                ((rumble + beam + noise) * decay * 0.9)
            }
            val sampleVal = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
            buffer[i * 2] = (sampleVal.toInt() and 0xFF).toByte()
            buffer[i * 2 + 1] = ((sampleVal.toInt() shr 8) and 0xFF).toByte()
        }
        return buffer
    }

    /**
     * Synthesizes 4.0-second Japanese Transformation Soundtrack tailored to each form:
     * - Japanese pentatonic / battle melody motifs
     * - Oozaru: Deep seismic beast roar, low taiko drum pulse, menacing monster brass
     * - SSJ4: Heroic triumphant Japanese battle synth lead + primal roar
     * - Ultra Xeno: Celestial space-time choir harmonies + crystal bells + hyper divine surge
     * - UI / MUI: Ka Ka Kachi Daze style battle synth melody + god Ki surge
     */
    private fun synthesizeAuraRoar(sampleRate: Int, durationSec: Double, form: SaiyanForm): ByteArray {
        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ByteArray(totalSamples * 2)

        val baseFreq = when {
            form.isOozaru -> 45.0 // Deep sub-bass beast rumble
            form == SaiyanForm.SUPER_SAIYAN_4 -> 85.0
            form == SaiyanForm.ULTRA_XENO -> 150.0
            form == SaiyanForm.SUPER_SAIYAN -> 95.0
            form == SaiyanForm.SUPER_SAIYAN_2 -> 110.0
            form == SaiyanForm.SUPER_SAIYAN_3 -> 80.0
            form == SaiyanForm.SUPER_SAIYAN_GOD -> 140.0
            form == SaiyanForm.SUPER_SAIYAN_BLUE -> 160.0
            form == SaiyanForm.ULTRA_INSTINCT || form == SaiyanForm.MASTERED_ULTRA_INSTINCT -> 130.0
            form == SaiyanForm.ULTRA_EGO || form == SaiyanForm.MASTERED_ULTRA_EGO -> 65.0
            else -> 100.0
        }

        // Japanese pentatonic melodic theme notes (A minor / Hirajōshi modes: A, B, C, E, F, A')
        val japaneseMelodyNotes = when (form) {
            SaiyanForm.BLACK_OOZARU, SaiyanForm.GOLDEN_OOZARU, SaiyanForm.WHITE_OOZARU ->
                doubleArrayOf(110.0, 116.5, 130.8, 110.0, 98.0, 110.0, 123.4, 110.0) // Menacing heavy beast march
            SaiyanForm.SUPER_SAIYAN_4 ->
                doubleArrayOf(220.0, 261.6, 293.7, 349.2, 329.6, 293.7, 440.0, 523.2) // Heroic triumphant DBGT theme
            SaiyanForm.ULTRA_XENO ->
                doubleArrayOf(440.0, 523.2, 587.3, 659.2, 783.9, 880.0, 987.7, 1046.5) // Transcendent celestial bells
            SaiyanForm.MASTERED_ULTRA_INSTINCT, SaiyanForm.ULTRA_INSTINCT ->
                doubleArrayOf(329.6, 392.0, 440.0, 523.2, 493.8, 440.0, 392.0, 523.2) // Ka Ka Kachi Daze melody
            SaiyanForm.MASTERED_ULTRA_EGO, SaiyanForm.ULTRA_EGO ->
                doubleArrayOf(130.8, 146.8, 164.8, 174.6, 164.8, 146.8, 130.8, 123.4) // Hakai destruction chords
            else ->
                doubleArrayOf(220.0, 261.6, 329.6, 392.0, 440.0, 392.0, 329.6, 440.0) // Classic Japanese battle theme
        }

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = when {
                t < 0.35 -> t / 0.35
                t > durationSec - 0.5 -> (durationSec - t) / 0.5
                else -> 1.0
            }

            // Japanese soundtrack melody note sequence
            val noteIndex = ((t / durationSec) * japaneseMelodyNotes.size).toInt().coerceIn(0, japaneseMelodyNotes.size - 1)
            val noteFreq = japaneseMelodyNotes[noteIndex]
            val melodyLead = sin(2.0 * PI * noteFreq * t) * 0.28

            // Power hum with resonant Ki pulse
            val kiTremolo = 1.0 + 0.25 * sin(2.0 * PI * (if (form.isOozaru) 5.0 else 8.0) * t)
            val subOsc = sin(2.0 * PI * baseFreq * t)
            val harmonic1 = 0.5 * sin(2.0 * PI * (baseFreq * 2.0) * t)
            val harmonic2 = 0.3 * sin(2.0 * PI * (baseFreq * 3.0) * t)

            // Taiko drum bass impact for Oozaru mode
            val taiko = if (form.isOozaru) {
                val taikoBeat = (t % 0.8)
                sin(2.0 * PI * 55.0 * t) * exp(-taikoBeat * 14.0) * 0.55
            } else 0.0

            // Lightning crackle for SSJ2, SSJ3, SSJ4, MUI, MUE, Ultra Xeno, Oozaru
            val lightning = if (form.hasLightning && Random.nextDouble() > 0.982) {
                (Random.nextDouble() * 2.0 - 1.0) * 0.55
            } else 0.0

            // Celestial chime shimmer for God forms & Ultra Xeno
            val celestial = if (form.isGodly) {
                0.22 * sin(2.0 * PI * (1200.0 + 250.0 * sin(2.0 * PI * 3.5 * t)) * t)
            } else 0.0

            // White noise Ki rushing wind
            val whiteNoise = (Random.nextDouble() * 2.0 - 1.0) * (if (form.isOozaru) 0.32 else 0.20)

            val mixed = (subOsc + harmonic1 + harmonic2 + melodyLead + taiko + lightning + celestial + whiteNoise) *
                    kiTremolo * envelope * 0.45

            val sampleVal = (mixed.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
            buffer[i * 2] = (sampleVal.toInt() and 0xFF).toByte()
            buffer[i * 2 + 1] = ((sampleVal.toInt() shr 8) and 0xFF).toByte()
        }
        return buffer
    }

    private fun synthesizeFlap(sampleRate: Int): ByteArray {
        val duration = 0.12
        val samples = (sampleRate * duration).toInt()
        val buffer = ByteArray(samples * 2)

        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val progress = t / duration
            val freq = 340.0 - 200.0 * progress
            val amp = (1.0 - progress) * 0.4
            val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.25 * (1.0 - progress)
            val sample = ((sin(2.0 * PI * freq * t) * amp + noise).coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
            buffer[i * 2] = (sample.toInt() and 0xFF).toByte()
            buffer[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
        }
        return buffer
    }

    private fun synthesizeScore(sampleRate: Int): ByteArray {
        val duration = 0.22
        val samples = (sampleRate * duration).toInt()
        val buffer = ByteArray(samples * 2)

        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 16.0)
            val tone1 = sin(2.0 * PI * 880.0 * t)
            val tone2 = 0.6 * sin(2.0 * PI * 1760.0 * t)
            val sample = (((tone1 + tone2) * decay * 0.4).coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
            buffer[i * 2] = (sample.toInt() and 0xFF).toByte()
            buffer[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
        }
        return buffer
    }

    private fun synthesizeHit(sampleRate: Int): ByteArray {
        val duration = 0.35
        val samples = (sampleRate * duration).toInt()
        val buffer = ByteArray(samples * 2)

        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 9.0)
            val lowImpact = sin(2.0 * PI * (120.0 - 80.0 * (t / duration)) * t) * 0.7
            val crunch = (Random.nextDouble() * 2.0 - 1.0) * 0.8
            val sample = (((lowImpact + crunch) * decay * 0.5).coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
            buffer[i * 2] = (sample.toInt() and 0xFF).toByte()
            buffer[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
        }
        return buffer
    }
}
