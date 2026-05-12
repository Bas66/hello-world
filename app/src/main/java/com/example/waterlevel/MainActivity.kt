package com.example.waterlevel

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.waterlevel.databinding.ActivityMainBinding
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    // Low-pass filter alpha voor vloeiende beweging
    private val alpha = 0.15f
    private var filteredX = 0f
    private var filteredY = 0f
    private var filteredZ = 9.81f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Scherm altijd aan houden
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometer == null) {
            showNoSensorMessage()
        }

        binding.btnCalibrate.setOnClickListener {
            calibrate()
        }
    }

    private var calibrationOffsetX = 0f
    private var calibrationOffsetY = 0f

    private fun calibrate() {
        calibrationOffsetX = filteredX
        calibrationOffsetY = filteredY
        binding.tvStatus.text = getString(R.string.status_calibrated)
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        // Low-pass filter voor vloeiende weergave
        filteredX = alpha * event.values[0] + (1 - alpha) * filteredX
        filteredY = alpha * event.values[1] + (1 - alpha) * filteredY
        filteredZ = alpha * event.values[2] + (1 - alpha) * filteredZ

        // Bereken tilthoeken in graden
        val adjustedX = filteredX - calibrationOffsetX
        val adjustedY = filteredY - calibrationOffsetY

        val gravity = sqrt(adjustedX * adjustedX + adjustedY * adjustedY + filteredZ * filteredZ)

        val tiltX = Math.toDegrees(atan2(adjustedX.toDouble(), filteredZ.toDouble())).toFloat()
        val tiltY = Math.toDegrees(atan2(adjustedY.toDouble(), filteredZ.toDouble())).toFloat()

        binding.bubbleLevel.tiltX = tiltX
        binding.bubbleLevel.tiltY = tiltY

        updateAngleDisplay(tiltX, tiltY)
        updateLevelStatus(tiltX, tiltY)
    }

    private fun updateAngleDisplay(tiltX: Float, tiltY: Float) {
        binding.tvAngleX.text = getString(R.string.angle_x, String.format("%.1f", abs(tiltX)))
        binding.tvAngleY.text = getString(R.string.angle_y, String.format("%.1f", abs(tiltY)))
    }

    private fun updateLevelStatus(tiltX: Float, tiltY: Float) {
        val isLevel = binding.bubbleLevel.isSurfaceLevel()
        if (isLevel) {
            binding.tvStatus.text = getString(R.string.status_level)
            binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.level_green))
            binding.statusIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.level_green))
        } else {
            binding.tvStatus.text = getString(R.string.status_not_level)
            binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.level_red))
            binding.statusIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.level_red))
        }
    }

    private fun showNoSensorMessage() {
        binding.tvStatus.text = getString(R.string.no_sensor)
        binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.level_red))
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Niet nodig voor deze app
    }
}
