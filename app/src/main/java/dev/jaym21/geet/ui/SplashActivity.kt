package dev.jaym21.geet.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dev.jaym21.geet.utils.Constants

class SplashActivity: AppCompatActivity() {

    private var readPermissionGranted = false
    private var writePermissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isReadPermissionAvailable = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val isWritePermissionAvailable = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val minSDK29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        readPermissionGranted = isReadPermissionAvailable
        writePermissionGranted = isWritePermissionAvailable || minSDK29

        if (readPermissionGranted) {
            if (writePermissionGranted) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                ActivityCompat.requestPermissions(this,  arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), Constants.WRITE_EXTERNAL_STORAGE_REQUEST_CODE)
            }
        } else {
            ActivityCompat.requestPermissions(this,  arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), Constants.READ_EXTERNAL_STORAGE_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            Constants.WRITE_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    val intent = Intent(this, NoPermissionActivity::class.java)
                    startActivity(intent)
                    finish()
                }else {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            Constants.READ_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    val intent = Intent(this, NoPermissionActivity::class.java)
                    startActivity(intent)
                    finish()
                }else {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}