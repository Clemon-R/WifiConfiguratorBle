package fr.rtone.demowificonfigurator.ble

import android.bluetooth.*
import android.bluetooth.BluetoothGatt.GATT_READ_NOT_PERMITTED
import android.bluetooth.BluetoothGatt.GATT_WRITE_NOT_PERMITTED
import android.util.Log
import fr.rtone.demowificonfigurator.ble.BleHandler.Companion.BLE_HANDLER_CHAR_READ
import fr.rtone.demowificonfigurator.ble.BleHandler.Companion.BLE_HANDLER_CHAR_WRITE
import fr.rtone.demowificonfigurator.ble.BleHandler.Companion.BLE_HANDLER_DEVICE_CONNECTED
import fr.rtone.demowificonfigurator.ble.BleHandler.Companion.BLE_HANDLER_DEVICE_CONNECTING
import fr.rtone.demowificonfigurator.ble.BleHandler.Companion.BLE_HANDLER_DEVICE_DISCONNECTED
import fr.rtone.demowificonfigurator.ble.BleHandler.Companion.BLE_HANDLER_DEVICE_DISCONNECTING
import fr.rtone.demowificonfigurator.ble.BleHandler.Companion.BLE_HANDLER_DEVICE_SERVICE


class BleClient(val device: BluetoothDevice, val adapter: BleAdapter)
{
    companion object{
        private val TAG = "BleClient"
    }
    var bluetoothGatt:BluetoothGatt ?= null
    var connected: Boolean = false

    fun connect()
    {
        if (this.connected){
            Log.w(TAG, "Already connected")
            return
        }
        Log.d(TAG, "Connecting...")
        if (bluetoothGatt == null){
            bluetoothGatt = device.connectGatt(adapter.activity,false,mBleGattCallBack)
        } else {
            bluetoothGatt!!.connect()
        }
    }

    fun disconnect()
    {
        Log.d(TAG, "Disconnecting...")
        if (this.bluetoothGatt == null || !this.connected){
            Log.w(TAG, "Already disconnected")
            return
        }
        this.bluetoothGatt!!.disconnect()
    }

    private val mBleGattCallBack: BluetoothGattCallback by lazy {
        object : BluetoothGattCallback(){

            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        connected = true
                        applyAction(BLE_HANDLER_DEVICE_CONNECTED, this@BleClient)
                        bluetoothGatt!!.discoverServices()
                    }
                    BluetoothProfile.STATE_CONNECTING ->  applyAction(BLE_HANDLER_DEVICE_CONNECTING, this@BleClient)
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        connected = false
                        applyAction(BLE_HANDLER_DEVICE_DISCONNECTED, this@BleClient)
                    }
                    BluetoothProfile.STATE_DISCONNECTING -> applyAction(BLE_HANDLER_DEVICE_DISCONNECTING, this@BleClient)
                }
                when (status) {
                    BluetoothGatt.GATT_SUCCESS ->  Log.d(TAG, "Successfully created GATT")
                    BluetoothGatt.GATT_FAILURE -> Log.e(TAG, "Error in creating GATT")
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)
                applyAction(BLE_HANDLER_DEVICE_SERVICE, this@BleClient, gatt)
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                super.onCharacteristicRead(gatt, characteristic, status)
                if (status == GATT_READ_NOT_PERMITTED)
                    return
                applyAction(BLE_HANDLER_CHAR_READ, this@BleClient, gatt)
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                super.onCharacteristicWrite(gatt, characteristic, status)
                if (status == GATT_WRITE_NOT_PERMITTED)
                    return
                applyAction(BLE_HANDLER_CHAR_WRITE, this@BleClient, gatt)
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
                super.onCharacteristicChanged(gatt, characteristic)
                Log.d(TAG,"onCharacteristicChanged: reading into the characteristic ${characteristic?.uuid} the value ${characteristic?.getIntValue(
                    BluetoothGattCharacteristic.FORMAT_UINT32,0)}")

                val value :Int?= characteristic?.value!![0].toInt()
            }
        }
    }

    private fun applyAction(action: Int, client: BleClient?, gatt: BluetoothGatt? = null){
        for (entry in adapter.handlers){
            if (entry.key and action > 0){
                entry.value.apply(action, client, gatt)
            }
        }
    }
}