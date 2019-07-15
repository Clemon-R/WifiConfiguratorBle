package fr.rtone.demowificonfigurator.ble

import android.Manifest
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import fr.rtone.demowificonfigurator.ble.handler.BleHandler
import fr.rtone.demowificonfigurator.ble.handler.BleHandlerType
import fr.rtone.demowificonfigurator.ble.handler.BleParam

class BleAdapter(val activity: AppCompatActivity){
    companion object{
        private val TAG = "BleAdapter"
        private val REQUEST_ENABLE_BT = 0
        private val REQUEST_PERMISSION_LOCATION = 1
        private val SCAN_TIMEOUT = 15 //Seconds

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

    val bluetoothAdapter: BluetoothAdapter
    val bluetoothManager: BluetoothManager
    val handlers: MutableMap<Int, BleHandler> = mutableMapOf()

    init {
        bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null)
            throw Exception("Missing a bluetoothAdapter")
        instance = this
        this.init()
    }

    private fun isBluetoothEnabled() : Boolean
    {
        activity.packageManager.takeIf { !it.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {
            Toast.makeText(activity, "BLE not supported", Toast.LENGTH_SHORT).show()
            activity.finish()
        }
        return bluetoothAdapter.isEnabled
    }

    private fun isLocationPermission() : Boolean
    {
        return activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun enableBluetooth()
    {
        Log.d(TAG, "Ask to enable bluetooth")
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    private fun enablePermissionLocation()
    {
        Log.d(TAG, "Ask permission for location")
        activity.requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), REQUEST_PERMISSION_LOCATION
        )
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
        if (scanning){
            Log.w(TAG, "Already not scanning")
            return
        }
        if (!isBluetoothEnabled()) {
            enableBluetooth()
            return
        } else if (!isLocationPermission()) {
            enablePermissionLocation()
        }
        Log.d(TAG, "Starting scan...")
        timer.postDelayed({
            stopScan()
        }, 1000L * SCAN_TIMEOUT)
        bluetoothAdapter.startDiscovery()
        Toast.makeText(activity, "Searching devices...", Toast.LENGTH_LONG).show()
    }

    fun stopScan()
    {
        if (!scanning){
            Log.w(TAG, "Already not scanning")
            return
        }
        Log.d(TAG, "Stopping scan...")
        bluetoothAdapter.cancelDiscovery()
        this@BleAdapter.applyAction(BleHandlerType.SCANNED)
    }

    private val bluetoothHandler = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (val action = intent.action){
                BluetoothDevice.ACTION_FOUND -> { //Device find
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                    val client = BleClient(device, this@BleAdapter)
                    devices += (client.device.address to client)
                    this@BleAdapter.applyAction(BleHandlerType.DEVICE_FOUND, client)
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    this@BleAdapter.applyAction(BleHandlerType.SCANNING)
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> { //Discovery end
                    this@BleAdapter.applyAction(BleHandlerType.SCANNED)
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    if (state == BluetoothAdapter.STATE_ON)
                        this@BleAdapter.applyAction(BleHandlerType.BT_ON)
                    else if (state == BluetoothAdapter.STATE_OFF)
                        this@BleAdapter.applyAction(BleHandlerType.BT_OFF)
                }
                else -> {
                    Log.w(TAG, "Action $action catched")
                }
            }
        }
    }

    fun applyAction(
        action: BleHandlerType,
        client: BleClient? = null,
        gatt: BluetoothGatt? = null,
        char: BluetoothGattCharacteristic? = null,
        permitted: Boolean? = null
    ){
        for (entry in handlers){
            if (entry.key and action.uuid > 0){
                val param = BleParam(client, gatt, char, permitted)
                action.execute(entry.value, param)
            }
        }
    }
}