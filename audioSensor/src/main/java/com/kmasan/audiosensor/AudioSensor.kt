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
    val audioEncoding: Int = AudioFormat.ENCODING_PCM_16BIT
) {
    /*
    audioを随時取得するクラス

    "private val context: Context"
    Activity等を指定
    android.permission.RECORD_AUDIOが許可されているかの確認に使用

    "private val listener: AudioSensorListener?"
    bufferの値が変わった際のデータ送り先
    随時処理が不要ならnullでおｋ

    "val audioSource: Int = MediaRecorder.AudioSource.MIC"
    audioの音源元
    デフォルトでマイクにしてる

    "val sampleRate: Int = 44100"
    １データのサンプルレート
    デフォルトはおすすめの値

    "val audioChannel: Int = AudioFormat.CHANNEL_IN_MONO"
    データのチャンネル数
    デフォルトはモノラル（１チャンネル）

    "val audioEncoding: Int = AudioFormat.ENCODING_PCM_16BIT"
    audioのエンコーディング方法
    */
    val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate, audioChannel,
        audioEncoding
    )
    lateinit var audioRecord: AudioRecord
        private set
    var buffer:ShortArray = ShortArray(bufferSize)
        private set
    var isRecoding: Boolean = false
        private set

    // period[ms]: Audio processing interval
    fun start(period: Int) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            error("\"android.permission.RECORD_AUDIO\" is disable\n")
        }
        audioRecord = AudioRecord(
            audioSource,
            sampleRate,
            audioChannel,
            audioEncoding,
            bufferSize
        )
        audioRecord.startRecording()

        isRecoding = true
        // 指定period[ms]ごとに処理を実行
        recoding(period)
    }

    fun stop() {
        isRecoding = false
    }

    // 音声取得ループ
    private fun recoding(period: Int) {
        val hnd = Handler(Looper.getMainLooper())
        // こいつ(rnb0) が何回も呼ばれる
        val rnb = object : Runnable {
            override fun run() {
                // bufferにデータを入れる
                audioRecord.read(buffer,0,bufferSize)
                // 結果をlistenerで通知
                listener?.onAudioSensorChanged(buffer)

                // stop用のフラグ
                if (isRecoding) {
                    // 指定時間後に自分自身を呼ぶ
                    hnd.postDelayed(this, period.toLong())
                }
            }
        }
        // First call
        hnd.post(rnb)
    }

    interface AudioSensorListener{
        fun onAudioSensorChanged(data: ShortArray)
    }
}