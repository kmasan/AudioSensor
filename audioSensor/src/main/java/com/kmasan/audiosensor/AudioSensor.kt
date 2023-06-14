package com.kmasan.audiosensor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat


class AudioSensor(
    private val context: Context,
    private val listener: AudioSensorListener?,
    val audioSource: Int = MediaRecorder.AudioSource.MIC,
    val sampleRate: Int = 44100,
    val audioChannel: Int = AudioFormat.CHANNEL_IN_MONO,
    val audioPCM: Int = AudioFormat.ENCODING_PCM_16BIT
) {
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate, AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )
    lateinit var audioRecord: AudioRecord
    var buffer:ShortArray = ShortArray(bufferSize)
        private set
    var isRecoding: Boolean = false
        private set

    // period: オーディオ処理のインターバル, recordingMode: 処理の種類（定数として宣言済み）
    fun start(period: Int) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            error("permission is disable")
        }
        audioRecord = AudioRecord(
            audioSource,
            sampleRate,
            audioChannel,
            audioPCM,
            bufferSize
        )
        audioRecord.startRecording()

        isRecoding = true
        // 指定period[ms]ごとにrecordingModeで指定した処理を実行
        recoding(period)
    }

    fun stop() {
        isRecoding = false
    }

    //
    private fun recoding(period: Int) {
        val hnd0 = Handler(Looper.getMainLooper())
        // こいつ(rnb0) が何回も呼ばれる
        val rnb0 = object : Runnable {
            override fun run() {
                // bufferにデータを入れる
                audioRecord.read(buffer,0,bufferSize)
                // 結果をlistenerで通知
                listener?.onAudioSensorChanged(buffer)

                // stop用のフラグ
                if (isRecoding) {
                    // 指定時間後に自分自身を呼ぶ
                    hnd0.postDelayed(this, period.toLong())
                }
            }
        }
        // 初回の呼び出し
        hnd0.post(rnb0)
    }

    interface AudioSensorListener{
        fun onAudioSensorChanged(data: ShortArray)
    }
}