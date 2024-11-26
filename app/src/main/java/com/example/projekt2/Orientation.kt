package com.example.projekt2

import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.Surface
import android.view.WindowManager

private const val TAG = "Orientation"
private const val SENSOR_DELAY_MICROS = 16 * 1000 

class Orientation(activity: Activity) : SensorEventListener {

    interface Listener {
        fun onOrientationChanged(pitch: Float, roll: Float, yaw: Float)
    }

    private val mWindowManager: WindowManager = activity.window.windowManager
    private val mSensorManager: SensorManager = activity.getSystemService(Activity.SENSOR_SERVICE) as SensorManager
    private val mRotationSensor: Sensor? = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private var mLastAccuracy: Int = SensorManager.SENSOR_STATUS_UNRELIABLE
    private var mListener: Listener? = null

    fun startListening(listener: Listener) {
        if (mListener == listener) {
            return
        }

        mListener = listener
        if (mRotationSensor == null) {
            Log.w(TAG, "Rotation vector sensor not available; will not provide orientation data.")
            return
        }
        mSensorManager.registerListener(this, mRotationSensor, SENSOR_DELAY_MICROS)
    }

    fun stopListening() {
        mSensorManager.unregisterListener(this)
        mListener = null
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        mLastAccuracy = accuracy
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (mListener == null) {
            return
        }
        if (mLastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return
        }

        if (event.sensor == mRotationSensor) {
            updateOrientation(event.values)
        }
    }

    private fun updateOrientation(rotationVector: FloatArray) {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector)

        val worldAxisForDeviceAxisX = when (mWindowManager.defaultDisplay.rotation) {
            Surface.ROTATION_0, Surface.ROTATION_90 -> Pair(SensorManager.AXIS_X, SensorManager.AXIS_Z)
            Surface.ROTATION_180 -> Pair(SensorManager.AXIS_MINUS_X, SensorManager.AXIS_Z)
            Surface.ROTATION_270 -> Pair(SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Z)
            else -> Pair(SensorManager.AXIS_X, SensorManager.AXIS_Z)
        }

        val adjustedRotationMatrix = FloatArray(9)
        SensorManager.remapCoordinateSystem(
            rotationMatrix,
            worldAxisForDeviceAxisX.first,
            worldAxisForDeviceAxisX.second,
            adjustedRotationMatrix
        )

        // Transform rotation matrix into azimuth/pitch/roll
        val orientation = FloatArray(3)
        SensorManager.getOrientation(adjustedRotationMatrix, orientation)

        // Convert radians to degrees
        val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
        val roll = Math.toDegrees(orientation[2].toDouble()).toFloat()
        val yaw = Math.toDegrees(orientation[0].toDouble()).toFloat()

        mListener?.onOrientationChanged(pitch, roll, yaw)
    }
}
