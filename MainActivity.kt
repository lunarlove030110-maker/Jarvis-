
package com.jarvis.assistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.*
import android.graphics.Color
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var status: TextView
    private var recognizer: SpeechRecognizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(30, 40, 30, 40)
            setBackgroundColor(Color.rgb(4, 9, 24))
        }

        val title = TextView(this).apply {
            text = "J.A.R.V.I.S"
            textSize = 36f
            gravity = Gravity.CENTER
            setTextColor(Color.CYAN)
        }

        status = TextView(this).apply {
            text = "SYSTEM ONLINE"
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(10, 40, 10, 40)
        }

        val button = Button(this).apply {
            text = "ACTIVATE JARVIS"
            setOnClickListener { listen() }
        }

        layout.addView(title)
        layout.addView(status)
        layout.addView(button)

        setContentView(layout)
    }

    private fun listen() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO), 101
            )
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.text = "Speech recognition unavailable"
            return
        }

        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)

        recognizer?.setRecognitionListener(
            object : RecognitionListener {

                override fun onReadyForSpeech(params: Bundle?) {
                    status.text = "LISTENING..."
                }

                override fun onResults(results: Bundle?) {
                    val words = results?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    )
                    val command = words?.firstOrNull()
                        ?: "I didn't hear you"

                    status.text = "YOU: $command"
                    respond(command)
                }

                override fun onError(error: Int) {
                    status.text = "Please try again"
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(results: Bundle?) {}
                override fun onEvent(
                    eventType: Int,
                    params: Bundle?
                ) {}
            }
        )

        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        ).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "en-IN"
            )
        }

        recognizer?.startListening(intent)
    }

    private fun respond(command: String) {

        val answer = when {
            command.contains("hello", true) ->
                "Hello. How can I assist you?"

            command.contains("your name", true) ->
                "I am Jarvis, your personal assistant."

            command.contains("time", true) ->
                "The time is " +
                SimpleDateFormat(
                    "hh:mm a", Locale.getDefault()
                ).format(Date())

            else ->
                "Command received. My artificial intelligence module is under development."
        }

        status.text = "JARVIS: $answer"

        tts.speak(
            answer,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "jarvis_response"
        )
    }

    override fun onInit(result: Int) {
        if (result == TextToSpeech.SUCCESS) {
            tts.language = Locale.UK
        }
    }

    override fun onDestroy() {
        recognizer?.destroy()
        tts.shutdown()
        super.onDestroy()
    }
}
