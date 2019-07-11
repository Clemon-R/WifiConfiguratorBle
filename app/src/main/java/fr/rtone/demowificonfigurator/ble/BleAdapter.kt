package fr.rtone.demowificonfigurator.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import fr.rtone.demowificonfigurator.ble.BleHandler.Companion.BLE_HANDLER_BLE_OFF
import fr.rtone.demowificonfigurator.ble.BleHandler.Companion.BLE_HANDLER_BLE_ON
import fr.rtone.demowificonfigurator.ble.BleHandler.Companion.BLE_HANDLER_DEVICE_FOUND
import fr.rtone.demowificonfigurator.ble.BleHandler.Companion.BLE_HANDLER_SCANNED
import fr.rtone.demowificonfigurator.ble.BleHandler.Companion.BLE_HANDLER_SCANNING

class BleAdapter(val activity: AppCompatActivity){
    companion object{
        private val TAG = "BleAdapter"
        private val REQUEST_ENABLE_BT = 0
        private val REQUEST_PERMISSION_LOCATION = 1

        private var instance: BleAdapter? = null

        fun newInstance(activity: AppCompatActivity): BleAdapter {
            BleAdapter(activity)
            return instance!!
        }

        fun instance(): BleAdapter? {
            return instance
        }
    }

    private val timer = Handler()
    val scanning: Boolean
        get() = bluetoothAdapter.isDiscovering
    val devices: MutableMap<String, BleClient> = mutableMapOf()

    private val bluetoothAdapter: BluetoothAdapter
    val handlers: MutableMap<Int, BleHandler> = mutableMapOf()

    init {
        val bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null)
            throw Exception("Missing a bluetoothAdapter")
        instance = this
    }

    private fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)
    private fun checkBluetoothEnabled() : Boolean
    {
        activity.packageManager.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {
            Toast.makeText(activity, "BLE not supported", Toast.LENGTH_SHORT).show()
            activity.finish()
        }
        return bluetoothAdapter.isEnabled
    }

    private fun checkLocationPermission() : Boolean
    {
        return activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun askPermissionLocation()
    {
        Log.d(TAG, "Ask permission for location")
        activity.requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), REQUEST_PERMISSION_LOCATION
        )
    }

    private fun askEnableBluetooth()
    {
        Log.d(TAG, "Ask to enable bluetooth")
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    private fun init()
    {
        Log.d(TAG, "Init...")
        activity.registerReceiver(bluetoothHandler, IntentFilter(BluetoothDevice.ACTION_FOUND))
        activity.registerReceiver(bluetoothHandler, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    }

    private fun deinit()
    {
        Log.d(TAG, "Deinit...")
        activity.unregisterReceiver(bluetoothHandler)
    }

    fun destroy()
    {
        if (scanning)
            stopScan()
        deinit()
    }

    fun  startScan()
    {
        this.init()
        if (scanning){
            Log.w(TAG, "Already not scanning")
            return
        }
        if (!checkBluetoothEnabled()) {
            askEnableBluetooth()
            return
        }
        else if (!checkLocationPermission())
            askPermissionLocation()
        Log.d(TAG, "Starting scan...")
        timer.postDelayed({
            stopScan()
        }, 15000)
        bluetoothAdapter.startDiscovery()
        this@BleAdapter.applyAction(BLE_HANDLER_SCANNING, null)
        Toast.makeText(activity, "Scanning devices", Toast.LENGTH_LONG).show()
    }

    fun stopScan()
    {
        if (!scanning){
            Log.w(TAG, "Already not scanning")
            return
        }
        Log.d(TAG, "Stopping scan...")
        bluetoothAdapter.cancelDiscovery()
        this.applyAction(BLE_HANDLER_SCANNED, null)
    }

    private val bluetoothHandler = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (val action = intent.action){
                BluetoothDevice.ACTION_FOUND -> { //Device find
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                    val client = BleClient(device, this@BleAdapter)
                    devices += (client.device.address to client)
                    this@BleAdapter.applyAction(BLE_HANDLER_DEVICE_FOUND, client)
                    Log.d(TAG, "New device found: ${device.address} ${device.name}")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> { //Discovery end
                    this@BleAdapter.applyAction(BLE_HANDLER_SCANNED, null)
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    if (state == BluetoothAdapter.STATE_ON)
                        this@BleAdapter.applyAction(BLE_HANDLER_BLE_ON, null)
                    else if (state == BluetoothAdapter.STATE_OFF)
                        this@BleAdapter.applyAction(BLE_HANDLER_BLE_OFF, null)
                }
                else -> {
                    Log.w(TAG, "Action X$action catched")
                }
            }
        }
    }

    private fun applyAction(action: Int, client: BleClient?){
        for (entry in handlers){
            if (entry.key and action > 0){
                entry.value.apply(action, client, null)
            }
        }
    }
}