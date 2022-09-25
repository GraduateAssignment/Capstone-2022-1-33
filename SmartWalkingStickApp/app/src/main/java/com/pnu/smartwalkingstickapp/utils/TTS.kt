package com.pnu.smartwalkingstickapp.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

class TTS (context: Context) {
    private lateinit var textToSpeech: TextToSpeech

    init {
        textToSpeech = TextToSpeech(context, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                // TODO: 언어 선택
                val result = textToSpeech.setLanguage(Locale.KOREAN)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS","해당언어는 지원되지 않습니다.")
                    return@OnInitListener
                }
            }
        })
    }

    fun play(msg: String) {
        if (!textToSpeech.isSpeaking) {
            textToSpeech.speak(msg, TextToSpeech.QUEUE_FLUSH, null, null)
            textToSpeech?.playSilentUtterance(750, TextToSpeech.QUEUE_ADD,null)
        }
    }
}