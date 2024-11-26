package com.example.projekt2

import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.Surface
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.projekt2.ui.theme.PROJEKT2Theme

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var rotationSensor: Sensor? = null

    private val pitch = mutableStateOf(0f)
    private val roll = mutableStateOf(0f)
    private val yaw = mutableStateOf(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize sensor manager and rotation sensor
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        setContent {
            PROJEKT2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height

                            drawLine(
                                start = Offset(x = 0f, y = canvasHeight / 2f),
                                end = Offset(x = canvasWidth, y = canvasHeight / 2f),
                                color = Color.Blue,
                                strokeWidth = 5f
                            )

                            val th = ((pitch.value * canvasHeight) / 180f)
                            val tw = ((yaw.value * canvasWidth) / 180f)

                            val start = Offset(x = 0f + tw, y = (canvasHeight / 2f) + th)
                            val end = Offset(x = canvasWidth + tw, y = (canvasHeight / 2f) + th)

                            rotate(degrees = roll.value) {
                                drawLine(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color.Magenta,
                                            Color.Red,
                                            Color.Yellow,
                                            Color.Green,
                                            Color.Cyan,
                                            Color.Blue,
                                            Color.Magenta
                                        ),
                                        start = start,
                                        end = end
                                    ),
                                    start = start,
                                    end = end,
                                    strokeWidth = 60f
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        rotationSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onStop() {
        super.onStop()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor == rotationSensor) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

            val adjustedRotationMatrix = FloatArray(9)
            val windowManager = getSystemService(Activity.WINDOW_SERVICE) as WindowManager
            val rotation = windowManager.defaultDisplay.rotation

            val worldAxisX = when (rotation) {
                Surface.ROTATION_0, Surface.ROTATION_90 -> SensorManager.AXIS_X
                Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X
                Surface.ROTATION_270 -> SensorManager.AXIS_X
                else -> SensorManager.AXIS_X
            }
            val worldAxisZ = when (rotation) {
                Surface.ROTATION_0, Surface.ROTATION_90 -> SensorManager.AXIS_Z
                Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_Z
                Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Z
                else -> SensorManager.AXIS_Z
            }

            SensorManager.remapCoordinateSystem(
                rotationMatrix, worldAxisX, worldAxisZ, adjustedRotationMatrix
            )

            val orientation = FloatArray(3)
            SensorManager.getOrientation(adjustedRotationMatrix, orientation)

            pitch.value = Math.toDegrees(orientation[1].toDouble()).toFloat()
            roll.value = Math.toDegrees(orientation[2].toDouble()).toFloat()
            yaw.value = Math.toDegrees(orientation[0].toDouble()).toFloat()
        }
    }
}
