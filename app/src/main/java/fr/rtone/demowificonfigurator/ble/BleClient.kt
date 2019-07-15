package fr.rtone.demowificonfigurator.ble

import android.bluetooth.*
import android.bluetooth.BluetoothGatt.*
import android.util.Log
import fr.rtone.demowificonfigurator.ble.handler.BleHandlerType


class BleClient(val device: BluetoothDevice, val adapter: BleAdapter)
{
    companion object{
        private val TAG = "BleClient"
    }
    var bluetoothGatt:BluetoothGatt ?= null

    var state: Int = BluetoothProfile.STATE_DISCONNECTED

    fun connect()
    {
        if (this.state != BluetoothProfile.STATE_DISCONNECTED){
            Log.w(TAG, "You need to be disconnected")
            return
        }
        Log.d(TAG, "Connecting...")
        if (bluetoothGatt == null){
            bluetoothGatt = device.connectGatt(adapter.activity,false,mBleGattCallBack)
        } else if (adapter.bluetoothManager.getConnectionState(device, BluetoothProfile.GATT_SERVER) == BluetoothProfile.STATE_DISCONNECTED) {
            bluetoothGatt!!.connect()
        }
    }

    fun disconnect()
    {
        Log.d(TAG, "Disconnecting...")
        if (this.state != BluetoothProfile.STATE_CONNECTED){
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
                        adapter.applyAction(BleHandlerType.DEVICE_CONNECTED, this@BleClient)
                        bluetoothGatt!!.discoverServices()
                    }
                    BluetoothProfile.STATE_CONNECTING ->  adapter.applyAction(BleHandlerType.DEVICE_CONNECTING, this@BleClient)
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        bluetoothGatt!!.close()
                        bluetoothGatt = null
                        adapter.applyAction(BleHandlerType.DEVICE_DISCONNECTED, this@BleClient)
                    }
                    BluetoothProfile.STATE_DISCONNECTING -> adapter.applyAction(BleHandlerType.DEVICE_DISCONNECTING, this@BleClient)
                }
                state = newState
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
                adapter.applyAction(BleHandlerType.CHAR_READ, this@BleClient, gatt, characteristic, status != GATT_READ_NOT_PERMITTED)
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                super.onCharacteristicWrite(gatt, characteristic, status)
                adapter.applyAction(BleHandlerType.CHAR_WRITE, this@BleClient, gatt, characteristic, status != GATT_WRITE_NOT_PERMITTED)
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
                super.onCharacteristicChanged(gatt, characteristic)
                Log.d(TAG,"onCharacteristicChanged: reading into the characteristic ${characteristic?.uuid}")
            }
        }
    }
}