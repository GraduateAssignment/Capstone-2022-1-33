package com.pnu.smartwalkingstickapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
    private val REQUEST_ALL_PERMISSION = 2
    private val REQUEST_CALL = 4
    private val REQUEST_CAMERA = 5

    //private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navHostFragment: NavHostFragment
    val mapViewModel: MapViewModel by viewModels()
    private val bluetoothViewModel: BluetoothViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        invalidateOptionsMenu()

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment

        navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.nav_camera_x_fragment -> binding.bottomNav.visibility = View.GONE
                R.id.showDirectionFragment -> binding.bottomNav.visibility = View.GONE
                else -> binding.bottomNav.visibility = View.VISIBLE
            }
        }

        supportFragmentManager.addOnBackStackChangedListener(this)

        bluetoothViewModel.onReceiveRunEmergencyCall.observe(this) {
            if (it) {
                runEmergencyCall()
            }
        }
        bluetoothViewModel.onReceiveRunCamera.observe(this) {
            if (it != "") {
                runCamera(it)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.v("juyong: 2-", requestCode.toString())
        when (requestCode) {
            REQUEST_CALL -> {
                if (getSuccessRequest(grantResults)) {
                    bluetoothViewModel.setRequestCallPermissionsResult(true)
                }
            }
            REQUEST_CAMERA -> {
                if (getSuccessRequest(grantResults)) {
                    bluetoothViewModel.setRequestCameraPermissionsResult(true)
                }
            }
            REQUEST_ALL_PERMISSION -> {
                if (getSuccessRequest(grantResults)) {
                    bluetoothViewModel.setRequestBluetoothPermissionsResult(true)
                }
            }
        }
    }

    fun getSuccessRequest(grantResults: IntArray): Boolean {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
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