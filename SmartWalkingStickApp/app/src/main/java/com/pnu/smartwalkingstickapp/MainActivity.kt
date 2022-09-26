package com.pnu.smartwalkingstickapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.pnu.smartwalkingstickapp.databinding.ActivityMainBinding
import com.pnu.smartwalkingstickapp.ui.bluetooth.BluetoothViewModel
import com.pnu.smartwalkingstickapp.ui.map_task.MapViewModel
import com.pnu.smartwalkingstickapp.ui.ocr_task.CameraXFragment

class MainActivity : AppCompatActivity(), FragmentManager.OnBackStackChangedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    //private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navHostFragment: NavHostFragment
    val mapViewModel: MapViewModel by viewModels()
    val bluetoothViewModel: BluetoothViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        invalidateOptionsMenu()

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment

        //appBarConfiguration = AppBarConfiguration(navController.graph)
        navController = navHostFragment.navController
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.nav_camera_x_fragment -> binding.bottomNav.visibility = View.GONE
                R.id.showDirectionFragment -> binding.bottomNav.visibility = View.GONE
                else -> binding.bottomNav.visibility = View.VISIBLE
            }
        }

        when (val phonePermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)) {
            PackageManager.PERMISSION_GRANTED -> null
            else -> requestCallPhonePermission()
        }
        supportFragmentManager.addOnBackStackChangedListener(this)

        bluetoothViewModel.onReceiveRunEmergencyCall.observe(this) {
            if (it) {
                runEmergencyCall()
            }
        }
//        bluetoothViewModel.onReceiveRunCamera.observe(this) {
//            runCamera(it)
//        }
    }

    private fun requestCallPhonePermission() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CALL_PHONE), 99)
    }

    private fun runEmergencyCall() {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        val defaultValue = ""
        val savedPhoneNumber = sharedPref.getString("number", defaultValue)
        var intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$savedPhoneNumber")
        if (intent.resolveActivity(this.packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun runCamera(feature: String) {
        supportFragmentManager.commit {
            val bundle = bundleOf("feature" to feature)
            replace<CameraXFragment>(R.id.nav_host_fragment_container, args = bundle)
            setReorderingAllowed(true)
            addToBackStack(null)
            hideComponent()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_app_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_detect -> {
                runCamera("detect")
                super.onOptionsItemSelected(item)
            }
            R.id.menu_text -> {
                runCamera("text")
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun hideComponent() {
        binding.bottomNav.visibility = View.GONE
        binding.toolbar.visibility = View.GONE
    }

    private fun showComponent() {
        binding.bottomNav.visibility = View.VISIBLE
        binding.toolbar.visibility = View.VISIBLE
    }

    override fun onBackStackChanged() {
        when (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container)) {
            is CameraXFragment -> {
                hideComponent()
            }
            else -> {
                showComponent()
            }
        }
    }

}