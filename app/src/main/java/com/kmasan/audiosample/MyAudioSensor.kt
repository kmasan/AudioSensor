package com.kmasan.audiosample

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kmasan.audiosensor.*
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.FileWriter
import java.io.IOException
import java.util.stream.IntStream.range
import kotlin.collections.ArrayDeque
import kotlin.math.*


class MyAudioSensor(context: Context): AudioSensor.AudioSensorListener {
    companion object {
        const val LOG_NAME: String = "MyAudioSensor"

        data class AudioData(
            val time: Long,
            val buffer: ShortArray,
            val fft: DoubleArray,
            val fft2: DoubleArray,
            val envelope: DoubleArray
        ) {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as AudioData

                if (!buffer.contentEquals(other.buffer)) return false

                return true
            }

            override fun hashCode(): Int {
                return buffer.contentHashCode()
            }
        }
    }

    private val audioSensor = AudioSensor(context, this)
    private val audioAnalysis = AudioAnalysis()

    var queue: ArrayDeque<AudioData> = ArrayDeque(listOf())
    private set

    var csvRun = false

    var volume = 0 // 現在の音量
        private set
    private var _volumeLiveData = MutableLiveData(volume)
    val volumeLiveData: LiveData<Int> = _volumeLiveData
    var voice = 0 // 現在の音の高さレベル
        private set

    fun start(period: Int) = audioSensor.start(period)
    fun stop() = audioSensor.stop()

    override fun onAudioSensorChanged(data: ShortArray) {
        val fft = audioAnalysis.fft(data)
        val fft2 = audioAnalysis.fft(audioAnalysis.toLogSpectrum(fft))
        val envelope = audioAnalysis.toSpectrumEnvelope(fft, 2.0, audioSensor.sampleRate)
        if(csvRun)
            queue.add(AudioData(System.currentTimeMillis(), data.clone(), fft.clone(), fft2.clone(), envelope.clone()))
        volume = audioAnalysis.toDB(data)
        Log.d(LOG_NAME, "$volume")
        _volumeLiveData.postValue(volume)

        // 最大振幅の周波数
        val maxFrequency: Int = audioAnalysis.toMaxFrequency(fft, audioSensor.sampleRate)

        // 最大振幅の周波数をレベルに変換（0~)
        val levelStage = 5 // レベルの段階数
        val minVoice = 500 // レベル1とする最低周波数
        val maxVoice = 2500 // レベル最大とする最大周波数
        val levelInterval = (maxVoice - minVoice)/(levelStage-1) // レベルの間隔
        val voiceLevel: Int = (maxFrequency - minVoice + levelInterval)/ levelInterval // レベルに変換
        voice = when{
            voiceLevel < 0 -> 0
            voiceLevel > levelStage -> levelStage
            else -> voiceLevel
        }
    }

    fun cosineSimilarity(dataA: DoubleArray, dataB: DoubleArray): Double {
        // コサイン類似度を計算
        var dotProduct = 0.0
        var normA = 0.0
        var normB = 0.0
        for ( i in range(0, dataA.size)) {
            dotProduct += dataA[i] * dataB[i];
            normA += dataA[i].pow(2.0)
            normB += dataB[i].pow(2.0)
        }
        return dotProduct / (sqrt(normA) * sqrt(normB))
    }

    fun csvWriterStart(path: String, fileName: String): Boolean {
        //CSVファイルの定期的な書き出し
        try{
            //書込み先指定
            val writer = FileWriter("${path}/${fileName}-sound.csv")

            //書き込み準備
            val csvPrinter = CSVPrinter(
                writer, CSVFormat.DEFAULT
                    .withHeader(
                        "time",
                        "buffer",
                        "fft",
                        "fft2",
                        "envelope"
                    )
            )
            val hnd = Handler(Looper.getMainLooper())
            queue.clear()
            csvRun = true
            // rnbが何回も呼ばれる
            val rnb = object : Runnable {
                override fun run() {
//                    val queueClone = queue
                    //書き込み開始
                    for(data in queue){
                        //データ保存
                        csvPrinter.printRecord(
                            data.time,
                            data.buffer.toList(),
                            data.fft.toList(),
                            data.fft2.toList(),
                            data.envelope.toList()
                        )
                    }
                    queue.clear()

                    // stop用のフラグ
                    when(csvRun) {
                        true -> {
                            // 指定時間後に自分自身を呼ぶ
                            hnd.postDelayed(this, 1000)
                        }
                        false -> {
                            //データ保存の終了処理
                            csvPrinter.flush()
                            csvPrinter.close()
                        }
                    }
                }
            }
            // 初回の呼び出し
            hnd.post(rnb)
            return true
        }catch (e: IOException){
            //エラー処理d
            Log.d("csvWrite", "${e}:${e.message!!}")
            return false
        }
    }
}