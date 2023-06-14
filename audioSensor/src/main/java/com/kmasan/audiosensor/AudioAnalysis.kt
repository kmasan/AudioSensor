package com.kmasan.audiosensor

import org.jtransforms.fft.DoubleFFT_1D
import java.util.stream.IntStream
import kotlin.math.log10
import kotlin.math.sqrt

class AudioAnalysis {
    fun toDB(buffer: ShortArray): Int {
        // 最大音量を解析
        val sum = buffer.sumOf { it.toDouble() * it.toDouble() }
        val amplitude = sqrt(sum / buffer.size)
        // デシベル変換
        return (20.0 * log10(amplitude)).toInt()
    }

    fun toMaxAmplitude(buffer: ShortArray): Double{
        //振幅が最大の周波数とその振幅値の解析
        var maxAmplitude = 0.0 // 最大振幅
        // 最大振幅が入っているリスト番号を走査
        for(index in IntStream.range(0, buffer.size - 1)){
            val tmp = sqrt((buffer[index] * buffer[index] + buffer[index + 1] * buffer[index + 1]).toDouble())
            if (maxAmplitude < tmp){
                maxAmplitude = tmp
            }
        }
        return maxAmplitude
    }

    fun toMaxAmplitude(buffer: DoubleArray): Double{
        //振幅が最大の周波数とその振幅値の解析
        var maxAmplitude = 0.0 // 最大振幅
        // 最大振幅が入っているリスト番号を走査
        for(index in IntStream.range(0, buffer.size - 1)){
            val tmp = sqrt((buffer[index] * buffer[index] + buffer[index + 1] * buffer[index + 1]))
            if (maxAmplitude < tmp){
                maxAmplitude = tmp
            }
        }
        return maxAmplitude
    }

    fun fft(buffer: ShortArray): DoubleArray{
        // FFT
        val fft = DoubleFFT_1D(buffer.size.toLong())
        val fftBuffer = DoubleArray(buffer.size * 2)
        val doubleBuffer: DoubleArray = buffer.map { it.toDouble() }.toDoubleArray()
        System.arraycopy(doubleBuffer, 0, fftBuffer, 0, buffer.size)
        fft.realForward(fftBuffer)

        return fftBuffer
    }

    fun toMaxFrequency(buffer: ShortArray, sampleRate: Int): Int{
        val fftBuffer = fft(buffer)
        return toMaxFrequency(fftBuffer, sampleRate)
    }

    fun toMaxFrequency(buffer: DoubleArray, sampleRate: Int): Int{
        //振幅が最大の周波数とその振幅値の解析
        var maxAmplitude = 0.0 // 最大振幅
        var maxIndex = 0 // 最大振幅が入っているリスト番号
        // 最大振幅が入っているリスト番号を走査
        for(index in IntStream.range(0, buffer.size - 1)){
            val tmp = sqrt((buffer[index] * buffer[index] + buffer[index + 1] * buffer[index + 1]))
            if (maxAmplitude < tmp){
                maxAmplitude = tmp
                maxIndex = index
            }
        }
        // 最大振幅の周波数
        return (maxIndex * sampleRate / buffer.size)
    }

    fun searchFrequency(buffer: DoubleArray, targetFrequency: Int, sampleRate: Int): Double{
        // 特定の周波数の振幅値の解析
        // val targetFrequency = 10000 // 特定の周波数（Hz）
        val index = (targetFrequency * buffer.size / sampleRate) // 特定の周波数が入っているリスト番号
        // 振幅値の解析
        return sqrt((buffer[index] * buffer[index] + buffer[index + 1] * buffer[index + 1]))
    }

    fun searchFrequency(buffer: ShortArray, targetFrequency: Int, sampleRate: Int): Double{
        val fftBuffer = fft(buffer)
        return searchFrequency(fftBuffer, targetFrequency, sampleRate)
    }
}