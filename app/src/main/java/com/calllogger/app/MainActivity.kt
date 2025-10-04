package com.calllogger.app
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var statusText: TextView
    private lateinit var startButton: Button
    private lateinit var logCountText: TextView
    private val PERMISSION_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        statusText = findViewById(R.id.statusText)
        startButton = findViewById(R.id.startButton)
        logCountText = findViewById(R.id.logCountText)
        startButton.setOnClickListener {
            if (checkPermissions()) startLoggingService() else requestPermissions()
        }
        updateUI()
    }

    private fun checkPermissions(): Boolean {
        val permissions = mutableListOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startLoggingService()
            } else {
                Toast.makeText(this, "Permissions required", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startLoggingService() {
        val intent = Intent(this, CallLoggerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        updateUI()
        Toast.makeText(this, "Call logging started!", Toast.LENGTH_SHORT).show()
    }

    private fun updateUI() {
        statusText.text = "Status: Active ✓"
        statusText.setTextColor(getColor(android.R.color.holo_green_dark))
        val logFile = File(getExternalFilesDir(null), "CallHistory/call_logs.json")
        if (logFile.exists()) {
            val count = logFile.readLines().count { it.contains("phone_number") }
            logCountText.text = "Calls Logged: $count"
        } else {
            logCountText.text = "Calls Logged: 0"
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }
}
