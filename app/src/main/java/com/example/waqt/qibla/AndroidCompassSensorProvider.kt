package com.example.waqt.qibla

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.roundToInt

class AndroidCompassSensorProvider(
    context: Context
) : CompassSensorProvider {
    private val sensorManager =
        context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)

    override val status: CompassSensorStatus = when {
        rotationSensor == null -> CompassSensorStatus.Unavailable
        else -> CompassSensorStatus.Available
    }

    override fun azimuthDegrees(): Flow<Float> = callbackFlow {
        val sensor = rotationSensor
        if (sensor == null) {
            close()
            return@callbackFlow
        }

        val rotationMatrix = FloatArray(9)
        val remappedMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)
        var lastSentAzimuth: Int? = null

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                // Portrait: map device X/Z so azimuth matches the top edge of the screen.
                val remapped = SensorManager.remapCoordinateSystem(
                    rotationMatrix,
                    SensorManager.AXIS_X,
                    SensorManager.AXIS_Z,
                    remappedMatrix
                )
                val matrixForOrientation = if (remapped) remappedMatrix else rotationMatrix
                SensorManager.getOrientation(matrixForOrientation, orientationAngles)
                val azimuth = QiblaCalculator.normalizeDegrees(
                    Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                )
                val rounded = azimuth.roundToInt()
                if (lastSentAzimuth != rounded) {
                    lastSentAzimuth = rounded
                    trySend(azimuth)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }

        sensorManager.registerListener(
            listener,
            sensor,
            SensorManager.SENSOR_DELAY_UI
        )
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
