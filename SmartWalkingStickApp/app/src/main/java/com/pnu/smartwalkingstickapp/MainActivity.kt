package com.pnu.smartwalkingstickapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.pnu.smartwalkingstickapp.databinding.ActivityMainBinding
import com.pnu.smartwalkingstickapp.ui.bluetooth.BluetoothViewModel
import com.pnu.smartwalkingstickapp.ui.map_task.MapViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navHostFragment: NavHostFragment
    val mapViewModel : MapViewModel by viewModels()
    val bluetoothViewModel: BluetoothViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(navController.graph)
        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener{ controller, destination, arguments ->
            when (destination.id) {
                R.id.nav_camera_x_fragment -> binding.bottomNav.visibility = View.GONE
                else -> binding.bottomNav.visibility = View.VISIBLE
            }
        }

        when(val phonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)){
            PackageManager.PERMISSION_GRANTED ->  null
            else -> requestCallPhonePermission()
        }

        bluetoothViewModel.onReceiveRunEmergencyCall.observe(this) {
            if (it) {
                runEmergencyCall()
            }
        }
    }

    private fun requestCallPhonePermission() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CALL_PHONE), 99)
    }

    private fun runEmergencyCall() {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        val defaultValue = ""
        val highScore = sharedPref.getString("number", defaultValue)
        var intent = Intent(Intent.ACTION_CALL)
        print(highScore)
        intent.data = Uri.parse("tel:$highScore")
        if(intent.resolveActivity(this.packageManager) != null){
            startActivity(intent)
        }
    }

    fun getNavHostFragment(): NavHostFragment {
        return navHostFragment
    }

}