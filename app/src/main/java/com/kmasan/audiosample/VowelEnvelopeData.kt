package com.kmasan.audiosample

import android.content.Context
import android.content.res.AssetManager
import org.apache.commons.csv.*
import java.io.IOException


class VowelEnvelopeData(context: Context) {
    private val assetManager: AssetManager = context.resources.assets
    lateinit var voiceA: DoubleArray
        private set
    lateinit var voiceI: DoubleArray
        private set
    lateinit var voiceU: DoubleArray
        private set
    lateinit var voiceE: DoubleArray
        private set
    lateinit var voiceO: DoubleArray
        private set

    init {
        csvRead()
    }

    private fun csvRead(): Boolean{
        //CSVファイルの定期的な書き出し
        try{
            //読み込み先指定
            val reader = assetManager.open("vowel-envelope.csv").reader()

            //読み込み
            val csvReader = CSVFormat.DEFAULT.withHeader().parse(reader)
            for (record in csvReader) {
                val vowel = record["vowel"]
                val envelope = record["envelope"]
                    .replace("\"","")
                    .replace("[","")
                    .replace("]","")
                    .split(",")
                    .map { it.toDouble() }.toDoubleArray()
                when(vowel){
                    "a" -> voiceA = envelope
                    "i" -> voiceI = envelope
                    "u" -> voiceU = envelope
                    "e" -> voiceE = envelope
                    "o" -> voiceO = envelope
                }
            }
//            Log.d("VowelEnvelopeData", "${voiceA.toList()}")
            return true
        }catch (e: IOException){
            //エラー処理
//            Log.d("csvWrite", "${e}:${e.message!!}")
            return false
        }
    }
}

