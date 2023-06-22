# AudioSensor
このライブラリはマイク等のaudioからbufferデータを取得するクラスとそのbufferをdb変換やfftなどの解析をするクラスが含まれます  
This library includes classes for acquiring buffer data from audio sources such as microphones, and classes for analyzing the buffer data, such as db conversion and fft.

## `AudioSensor()`
マイク等のaudioからbufferデータを取得するクラス  
Class to get buffer data from audio such as microphone.

### 初期引数(Initial argument)
- `private val context: Context`  
    Activity等を指定  
    `android.permission.RECORD_AUDIO`が許可されているかの確認に使用  
    
    Specify Activity etc.  
    Used to check if `android.permission.RECORD_AUDIO` is allowed.

- `private val listener: AudioSensorListener?`  
    bufferの値が変わった際のデータ送り先  
    随時処理が不要ならnullでおｋ  
    
    Destination of data when buffer value is changed.
    If you don't need to process at any time, use null.

- `val audioSource: Int = MediaRecorder.AudioSource.MIC`  
    audioの音源元を指定  
    デフォルトではマイクにしてる  
    
    Specify the source of the audio.
    Default to mike.

- `val sampleRate: Int = 44100`  
    １データのサンプルレート  
    デフォルトはおすすめの値  
    
    Sample rate per data.
    Default is the recommended value.

- `val audioChannel: Int = AudioFormat.CHANNEL_IN_MONO`  
    データのチャンネル数  
    デフォルトはモノラル（１チャンネル）  
    
    Number of data channels.
    Default is mono (1 channel).

- `val audioEncoding: Int = AudioFormat.ENCODING_PCM_16BIT`  
    audioのエンコーディング方法  
    
    audio encoding methods
    
### グローバル変数(Global variable)
- `val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate, audioChannel,
        audioEncoding
    )`  
    初期引数からbufferサイズを設定  
    
    Set buffer size from initial argument.

- `lateinit var audioRecord: AudioRecord`  
audioを取得するクラスを用意  

Prepare a class to get audio.
        
- `var buffer:ShortArray = ShortArray(bufferSize)`  
bufferの保存先  
必要なタイミングで使いたい場合に参照するとよい  

destination to save to buffer.  
You can refer to it when you want to use it when necessary.

- `var isRecoding: Boolean = false`  
audioを取得しているか否か  

Whether or not you have acquired audio.

### `fun start(period: Int)`  
- `period: Int`  
audioの取得間隔[ms]  
Audio acquisition interval[ms].

audioの取得を開始する際に呼び出す  
`android.permission.RECORD_AUDIO`が許可されていない場合はエラーが返ります  
`private fun recoding(period: Int)`を呼び出してaudio取得のループを始めます  

Called when starting to retrieve audio.  
If `android.permission.RECORD_AUDIO` is not allowed, an error will be returned.  
Call `private fun recoding(period: Int)` to start the loop of audio acquisition.

### `fun stop()`  
audioの取得を停止する際に呼び出す  

Called when stopping the acquisition of audio.

### `private fun recoding(period: Int)`  
audio取得のループ設定用  
`Handler`と`Runnable`を使って定期処理してる  
結果を`buffer`に保存し`listener?.onAudioSensorChanged()`で適宜送信  

For loop setting of audio acquisition.  
Using `Handler` and `Runnable` for periodic processing.  
Save the result in `buffer` and send it with `listener?.onAudioSensorChanged()` as appropriate.

### `interface AudioSensorListener`  
適宜送信用のlistener  
bufferを`fun onAudioSensorChanged(data: ShortArray)`に送信  

Listener for sending as appropriate.  
Send buffer to `fun onAudioSensorChanged(data: ShortArray)`.

## `AudioAnalisys()`
bufferをdb変換やfftなどの解析をするクラス  

Class to Analisys buffer for db conversion, fft, etc.

### `toDB(buffer: ShortArray): Int`
db変換する関数  

Function to convert db

### `toMaxAmplitude(buffer: ShortArray or DoubleArray): Double`
最大振幅を返す関数
`buffer: DoubleArray`版もある  

Function to return the maximum amplitude.  
`buffer: DoubleArray` version also available.

### `fft(buffer: ShortArray or DoubleArray): DoubleArray`
FFTする関数  

Functions for FFT.

### `ifft(fftBuffer: DoubleArray): DoubleArray`
逆FFTする関数

### `toMaxFrequency(buffer: ShortArray or DoubleArray, sampleRate: Int): Int`
fft結果から最大振幅の周波数を返す関数  
sampleRateはAudioSensorから取得するとよい  
buffer: ShortArray版もある  

Function to return the frequency of maximum amplitude from the fft result.  
SampleRate obtained from AudioSensor.  
`buffer: ShortArray` version also available.

### `searchFrequency(buffer: ShortArray or DoubleArray, targetFrequency: Int, sampleRate: Int): Double`
fft結果から特定の周波数の振幅を返す関数  
sampleRateはAudioSensorから取得するとよい  
buffer: ShortArray版もある  

Function to return the amplitude of a specific frequency from the fft result.  
SampleRate obtained from AudioSensor.  
`buffer: ShortArray` version also available.

### `toLogSpectrum(fftBuffer: DoubleArray): DoubleArray`
対数スペクトルに変換する関数

### `toPowerSpectrum(fftBuffer: DoubleArray): DoubleArray`
パワースペクトルに変換する関数

### `toSpectrumEnvelope(fftBuffer: DoubleArray, quefrencyTh: Double, sampleRate: Int): DoubleArray`
fftした結果からスペクトル包絡を求める関数
`quefrencyTh`を閾値としたローパスフィルタを用いている
パワースペクトル→対数スペクトル→ローパスフィルタ

