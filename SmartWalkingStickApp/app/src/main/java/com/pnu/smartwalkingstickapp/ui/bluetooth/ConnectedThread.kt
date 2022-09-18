package com.pnu.smartwalkingstickapp.ui.bluetooth

import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {
    private val TAG = "jiwoo"
    private val mmInStream: InputStream = mmSocket.inputStream
    private val mmOutStream: OutputStream = mmSocket.outputStream
    private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

    override fun run() {
        var numBytes: Int // bytes returned from read()
        Log.d(TAG, "run: ")
        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            // Read from the InputStream.
            numBytes = try {
                val value = mmInStream.read(mmBuffer)
                Log.d(TAG, "run: $value ")
                value
            } catch (e: IOException) {
                Log.d(TAG, "Input stream was disconnected", e)
                break
            }

        }
    }

    // Call this from the main activity to send data to the remote device.
    fun write(bytes: ByteArray) {
        try {
            mmOutStream.write(bytes)
        } catch (e: IOException) {
            Log.e(TAG, "Error occurred when sending data", e)

        }

        // Share the sent message with the UI activity.

    }

    // Call this method from the main activity to shut down the connection.
    fun cancel() {
        try {
            mmSocket.close()
        } catch (e: IOException) {
            Log.e(TAG, "Could not close the connect socket", e)
        }
    }
}