package fr.rtone.demowificonfigurator.ble

import android.bluetooth.*
import android.bluetooth.BluetoothGatt.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import fr.rtone.demowificonfigurator.ble.handler.BleHandlerType


class BleClient(val device: BluetoothDevice, val adapter: BleAdapter)
{
    companion object{
        private val TAG = "BleClient"
    }
    var bluetoothGatt:BluetoothGatt ?= null

    var state: BleClientState = BleClientState.DISCONNECTED

    init {
        this.init()
    }

    private fun init()
    {
        Log.d(TAG, "Init...")
    }

    private fun deinit()
    {
        Log.d(TAG, "Deinit...")
    }

    public fun destroy()
    {
        if (this.state == BleClientState.CONNECTED)
            this.disconnect()
        this.deinit()
    }

    fun connect()
    {
        if (this.state != BleClientState.DISCONNECTED){
            Log.w(TAG, "You need to be disconnected")
            return
        }
        if (bluetoothGatt == null){
            Log.d(TAG, "Connecting...")
            bluetoothGatt = device.connectGatt(adapter.activity,false,mBleGattCallBack, BluetoothDevice.TRANSPORT_LE)
        } else if (adapter.bluetoothManager.getConnectionState(device, BluetoothProfile.GATT_SERVER) == BluetoothProfile.STATE_DISCONNECTED) {
            Log.d(TAG, "Connecting...")
            bluetoothGatt!!.connect()
        }
    }

    fun disconnect()
    {
        Log.d(TAG, "Disconnecting...")
        if (this.state != BleClientState.CONNECTED){
            Log.w(TAG, "You need to be connected")
            return
        }
        if (adapter.bluetoothManager.getConnectionState(device, BluetoothProfile.GATT_SERVER) == BluetoothProfile.STATE_CONNECTED){
            this.bluetoothGatt!!.disconnect()
        }
    }

    private val mBleGattCallBack: BluetoothGattCallback by lazy {
        object : BluetoothGattCallback(){

            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        adapter.applyAction(BleHandlerType.DEVICE_CONNECTED, this@BleClient, gatt)
                        bluetoothGatt!!.discoverServices()
                    }
                    BluetoothProfile.STATE_CONNECTING ->  adapter.applyAction(BleHandlerType.DEVICE_CONNECTING, this@BleClient, gatt)
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        try {
                            bluetoothGatt!!.close()
                        }catch (e: Exception){
                        }
                        bluetoothGatt = null
                        adapter.applyAction(BleHandlerType.DEVICE_DISCONNECTED, this@BleClient)
                    }
                    BluetoothProfile.STATE_DISCONNECTING -> adapter.applyAction(BleHandlerType.DEVICE_DISCONNECTING, this@BleClient, gatt)
                }
                state = BleClientState.fromBluetoothProfile(newState)
                when (status) {
                    BluetoothGatt.GATT_SUCCESS ->  Log.d(TAG, "Successfully created GATT")
                    BluetoothGatt.GATT_FAILURE -> Log.e(TAG, "Error in creating GATT")
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)
                adapter.applyAction(BleHandlerType.SERVICE_DISCOVERED, this@BleClient, gatt)
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                super.onCharacteristicRead(gatt, characteristic, status)
                adapter.applyAction(BleHandlerType.CHAR_READ, this@BleClient, gatt, characteristic, status == GATT_SUCCESS)
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                super.onCharacteristicWrite(gatt, characteristic, status)
                adapter.applyAction(BleHandlerType.CHAR_WRITE, this@BleClient, gatt, characteristic, status == GATT_SUCCESS)
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
                super.onCharacteristicChanged(gatt, characteristic)
                Log.d(TAG,"onCharacteristicChanged: reading into the characteristic ${characteristic?.uuid}")
            }
        }
    }

    fun triggerAction(id: String, intent: Intent)
    {
        if (this.state != BleClientState.CONNECTED)
            return
        when (id){
            BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)

                when (state){
                    BluetoothDevice.BOND_NONE -> {
                        adapter.applyAction(BleHandlerType.NOT_BONDED, this@BleClient, bluetoothGatt)
                    }
                    BluetoothDevice.BOND_BONDING -> {
                        adapter.applyAction(BleHandlerType.BONDING, this@BleClient, bluetoothGatt)
                    }
                    BluetoothDevice.BOND_BONDED -> {
                        adapter.applyAction(BleHandlerType.BONDED, this@BleClient, bluetoothGatt)
                    }
                }
            }
        }
    }
}