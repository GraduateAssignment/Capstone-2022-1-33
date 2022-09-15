package com.pnu.smartwalkingstickapp.ui.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.pnu.smartwalkingstickapp.MainActivity
import com.pnu.smartwalkingstickapp.databinding.FragmentBluetoothBinding
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.lang.reflect.Method
import java.util.*

class BluetoothFragment : Fragment() {

    private var binding: FragmentBluetoothBinding? = null

    private val TAG = MainActivity::class.java.simpleName

    private val BT_MODULE_UUID: UUID =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // "random" unique identifier


    // #defines for identifying shared types between calling functions
    private val REQUEST_ENABLE_BT = 1 // used to identify adding bluetooth names

    val MESSAGE_READ = 2 // used in bluetooth handler to identify message update

    private val CONNECTING_STATUS = 3 // used in bluetooth handler to identify message status

    private var mBTAdapter: BluetoothAdapter? = null
    private var mPairedDevices: Set<BluetoothDevice>? = null
    private var mBTArrayAdapter: ArrayAdapter<String>? = null

    private var mHandler // Our main handler that will receive callback notifications
            : Handler? = null
    private var mConnectedThread // bluetooth background worker thread to send and receive data
            : ConnectedThread? = null
    private var mBTSocket: BluetoothSocket? = null // bi-directional client-to-client data path

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBluetoothBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.devicesListView.adapter = mBTArrayAdapter // assign model to view
        binding!!.devicesListView.onItemClickListener = mDeviceClickListener

        // Ask for location permission if not already allowed
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        }
        mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                if (msg.what === MESSAGE_READ) {
                    var readMessage: String? = null
                    try {
                        readMessage = String((msg.obj as ByteArray), Charsets.UTF_8)
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                    }
                    binding!!.readBuffer.text = readMessage
                }
                if (msg.what === CONNECTING_STATUS) {
                    if (msg.arg1 === 1) binding!!.bluetoothStatus.text =
                        "Connected to Device: " + msg.obj else binding!!.bluetoothStatus.text =
                        "Connection Failed"
                }
            }
        }
        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            binding!!.bluetoothStatus.text = "Status: Bluetooth not found"
            Toast.makeText(
                requireContext(),
                "Bluetooth device not found!",
                Toast.LENGTH_SHORT
            ).show()
        } else {

            binding!!.scan.setOnClickListener {
                bluetoothOn()
            }
            binding!!.off.setOnClickListener { bluetoothOff() }

            binding!!.pairedBtn.setOnClickListener {
                listPairedDevices()
            }

            binding!!.discover.setOnClickListener {
                discover()
            }
        }
    }

    private fun bluetoothOn() {
        if (!mBTAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            binding!!.bluetoothStatus.text = "Bluetooth enabled"
            Toast.makeText(requireContext(), "Bluetooth turned on", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(requireContext(), "Bluetooth is already on", Toast.LENGTH_SHORT)
                .show()
        }
    }

    // Enter here after user selects "yes" or "no" to enabling radio
    override fun onActivityResult(requestCode: Int, resultCode: Int, Data: Intent?) {
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                binding!!.bluetoothStatus.text = "Enabled"
            } else binding!!.bluetoothStatus.text = "Disabled"
        }
    }

    @SuppressLint("MissingPermission")
    private fun bluetoothOff() {
        mBTAdapter!!.disable() // turn off
        binding!!.bluetoothStatus.text = "Bluetooth disabled"
        Toast.makeText(requireContext(), "Bluetooth turned Off", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingPermission")
    private fun discover() {
        // Check if the device is already discovering
        if (mBTAdapter!!.isDiscovering) {
            mBTAdapter!!.cancelDiscovery()
            Toast.makeText(requireContext(), "Discovery stopped", Toast.LENGTH_SHORT).show()
        } else {
            if (mBTAdapter!!.isEnabled) {
                mBTArrayAdapter!!.clear() // clear items
                mBTAdapter!!.startDiscovery()
                Toast.makeText(requireContext(), "Discovery started", Toast.LENGTH_SHORT)
                    .show()
                requireActivity().registerReceiver(blReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
            } else {
                Toast.makeText(requireContext(), "Bluetooth not on", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    val blReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                // add the name to the list
                mBTArrayAdapter!!.add(
                    """
                    ${device!!.name}
                    ${device.address}
                    """.trimIndent()
                )
                mBTArrayAdapter!!.notifyDataSetChanged()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun listPairedDevices() {
        mBTArrayAdapter!!.clear()
        mPairedDevices = mBTAdapter!!.bondedDevices
        if (mBTAdapter!!.isEnabled) {
            // put it's one to the adapter
            for (device in (mPairedDevices as MutableSet<BluetoothDevice>?)!!) mBTArrayAdapter!!.add(
                device.name + "\n" + device.address
            )
            Toast.makeText(requireContext(), "Show Paired Devices", Toast.LENGTH_SHORT)
                .show()
        } else Toast.makeText(requireContext(), "Bluetooth not on", Toast.LENGTH_SHORT)
            .show()
    }

    private val mDeviceClickListener =
        OnItemClickListener { parent, view, position, id ->
            if (!mBTAdapter!!.isEnabled) {
                Toast.makeText(requireContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show()
                return@OnItemClickListener
            }
            binding!!.bluetoothStatus.text = "Connecting..."
            // Get the device MAC address, which is the last 17 chars in the View
            val info = (view as TextView).text.toString()
            val address = info.substring(info.length - 17)
            val name = info.substring(0, info.length - 17)

            // Spawn a new thread to avoid blocking the GUI one
            object : Thread() {
                @SuppressLint("MissingPermission")
                override fun run() {
                    var fail = false
                    val device = mBTAdapter!!.getRemoteDevice(address)
                    try {
                        mBTSocket = createBluetoothSocket(device)
                    } catch (e: IOException) {
                        fail = true
                        Toast.makeText(
                            requireContext(),
                            "Socket creation failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        mBTSocket!!.connect()
                    } catch (e: IOException) {
                        try {
                            fail = true
                            mBTSocket!!.close()
                            mHandler!!.obtainMessage(CONNECTING_STATUS, -1, -1)
                                .sendToTarget()
                        } catch (e2: IOException) {
                            //insert code to deal with requireContext()
                            Toast.makeText(
                                requireContext(),
                                "Socket creation failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    if (!fail) {
                        mConnectedThread = ConnectedThread(mBTSocket!!, mHandler)
                        mConnectedThread!!.start()
                        mHandler!!.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                            .sendToTarget()
                    }
                }
            }.start()
        }

    @SuppressLint("MissingPermission")
    @Throws(IOException::class)
    private fun createBluetoothSocket(device: BluetoothDevice): BluetoothSocket? {
        try {
            val m: Method = device.javaClass.getMethod(
                "createInsecureRfcommSocketToServiceRecord",
                UUID::class.java
            )
            return m.invoke(device, BT_MODULE_UUID) as BluetoothSocket?
        } catch (e: Exception) {
            Log.e(TAG, "Could not create Insecure RFComm Connection", e)
        }
        return device.createRfcommSocketToServiceRecord(BT_MODULE_UUID)
    }
}