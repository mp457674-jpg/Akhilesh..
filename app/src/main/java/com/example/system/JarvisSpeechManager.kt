package com.example.system

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class JarvisSpeechManager(private val context: Context) {

    private val TAG = "JarvisSpeech"
    private var textToSpeech: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val _liveTranscript = MutableStateFlow("")
    val liveTranscript: StateFlow<String> = _liveTranscript.asStateFlow()

    private var isTtsInitialized = false
    private var isMuted = false

    init {
        initializeTts()
    }

    private fun initializeTts() {
        try {
            textToSpeech = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isTtsInitialized = true
                    val hiLocale = Locale("hi", "IN")
                    val result = textToSpeech?.setLanguage(hiLocale)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        textToSpeech?.setLanguage(Locale.US)
                    }
                    textToSpeech?.setPitch(0.95f)
                    textToSpeech?.setSpeechRate(1.05f)

                    textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                        }

                        override fun onDone(utteranceId: String?) {
                            _isSpeaking.value = false
                        }

                        override fun onError(utteranceId: String?) {
                            _isSpeaking.value = false
                        }
                    })
                } else {
                    Log.e(TAG, "TTS initialization failed: $status")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating TextToSpeech", e)
        }
    }

    fun speak(text: String, onDone: () -> Unit = {}) {
        if (isMuted || !isTtsInitialized) {
            onDone()
            return
        }
        try {
            val utteranceId = "JARVIS_${System.currentTimeMillis()}"
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.e(TAG, "TTS speak failed", e)
            _isSpeaking.value = false
            onDone()
        }
    }

    fun stopSpeaking() {
        try {
            textToSpeech?.stop()
            _isSpeaking.value = false
        } catch (e: Exception) {
            Log.e(TAG, "Failed stopping TTS", e)
        }
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
        if (muted) stopSpeaking()
    }

    fun startListening(
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition service unavailable on device")
            return
        }

        try {
            stopSpeaking()
            _liveTranscript.value = ""
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {
                        _isListening.value = true
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        _audioAmplitude.value = (rmsdB / 10f).coerceIn(0f, 1f)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        _audioAmplitude.value = 0f
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        _audioAmplitude.value = 0f
                        val currentText = _liveTranscript.value.trim()
                        if (currentText.isNotBlank()) {
                            // If we already have live transcribed text before timeout, execute it!
                            onResult(currentText)
                            _liveTranscript.value = ""
                            return
                        }
                        val msg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out"
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Audio permission required"
                            else -> "Recognition error ($error)"
                        }
                        onError(msg)
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _audioAmplitude.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val query = matches?.firstOrNull().orEmpty()
                        val finalResult = if (query.isNotBlank()) query else _liveTranscript.value.trim()
                        _liveTranscript.value = ""
                        if (finalResult.isNotBlank()) {
                            onResult(finalResult)
                        } else {
                            onError("No speech detected")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partials = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = partials?.firstOrNull().orEmpty()
                        if (text.isNotBlank()) {
                            _liveTranscript.value = text
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            Log.e(TAG, "Error starting speech recognition", e)
            onError(e.localizedMessage ?: "Failed to start listening")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            _isListening.value = false
            _audioAmplitude.value = 0f
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping listener", e)
        }
    }

    fun shutdown() {
        try {
            speechRecognizer?.destroy()
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Shutdown error", e)
        }
    }
}
