package com.kmasan.audiosensor

import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kmasan.audiosensor.ui.theme.AudioSensorTheme

class MainActivity : ComponentActivity() {
    lateinit var audioSensor: MyAudioSensor
    lateinit var externalFilePath: String

    var sensorRun = false
    var csvRun = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createSetContent()

        externalFilePath = this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString()

        audioSensor = MyAudioSensor(this)
    }

    fun sensorStart(){
        audioSensor.start(10)

        sensorRun = true
    }

    fun sensorStop(){
        sensorRun = false
        audioSensor.stop()
    }

    fun csvStart(fileName: String){
        audioSensor.csvWriterStart(externalFilePath,fileName)

        csvRun = true
    }

    fun csvStop(){
        csvRun = false
        audioSensor.csvRun = false
    }

    private fun createSetContent(){
        setContent {
            var sensorButtonText by remember { mutableStateOf(" sensor off") }
            var csvButtonText by remember { mutableStateOf("csv start") }
            val dbText by audioSensor.volumeLiveData.observeAsState()

            AudioSensorTheme{
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        Greeting("Android")
                        OnClickButton(text = sensorButtonText) {
                            sensorButtonText = when(sensorRun){
                                true->{
                                    sensorStop()
                                    "sensor off"
                                }
                                false->{
                                    sensorStart()
                                    "sensor on"
                                }
                            }
                        }
                        Button(onClick = {
                            csvButtonText = when(csvRun){
                                true->{
                                    csvStop()
                                    "csv start"
                                }
                                false->{
                                    csvStart("${System.currentTimeMillis()}")
                                    "csw writing"
                                }
                            }
                        }) {
                            Text(text = csvButtonText)
                        }
                        Text(text = "audio: db")
                        Text(text = dbText.toString())
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun OnClickButton(text: String, onClick: () -> Unit){
    Button(onClick = onClick
    ) {
        Text(text = text)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AudioSensorTheme {
        Greeting("Android")
    }
}