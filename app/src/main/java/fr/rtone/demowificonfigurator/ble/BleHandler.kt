package fr.rtone.demowificonfigurator.ble

import android.bluetooth.BluetoothGatt
import android.util.Log
import fr.rtone.demowificonfigurator.ble.handlers.*


abstract class BleHandler(private val adapter: BleAdapter)
{
    companion object{
        private val TAG = "BleHandler"
        val BLE_HANDLER_SCANNING = 1 shl 0
        val BLE_HANDLER_SCANNED = 1 shl 1
        val BLE_HANDLER_DEVICE_FOUND = 1 shl 2
        val BLE_HANDLER_BLE_ON = 1 shl 3
        val BLE_HANDLER_BLE_OFF = 1 shl 4
        val BLE_HANDLER_DEVICE_CONNECTING = 1 shl 5
        val BLE_HANDLER_DEVICE_CONNECTED = 1 shl 6
        val BLE_HANDLER_DEVICE_DISCONNECTING = 1 shl 7
        val BLE_HANDLER_DEVICE_DISCONNECTED = 1 shl 8
        val BLE_HANDLER_DEVICE_SERVICE = 1 shl 9
        val BLE_HANDLER_CHAR_READ = 1 shl 10
        val BLE_HANDLER_CHAR_WRITE = 1 shl 11
    }

    init {
        val types = getTypes()

        adapter.handlers += (types to this)
    }

    fun destroy() {
        val types = getTypes()
        adapter.handlers -= types
    }

    fun getTypes(): Int {
        var result = 0
        if (this is IBleScanning)
            result += BLE_HANDLER_SCANNING
        if (this is IBleScanned)
            result += BLE_HANDLER_SCANNED
        if (this is IBleDeviceFound)
            result += BLE_HANDLER_DEVICE_FOUND
        if (this is IBleOn)
            result += BLE_HANDLER_BLE_ON
        if (this is IBleOff)
            result += BLE_HANDLER_BLE_OFF
        if (this is IBleConnecting)
            result += BLE_HANDLER_DEVICE_CONNECTING
        if (this is IBleConnected)
            result += BLE_HANDLER_DEVICE_CONNECTED
        if (this is IBleDisconnecting)
            result += BLE_HANDLER_DEVICE_DISCONNECTING
        if (this is IBleDisconnected)
            result += BLE_HANDLER_DEVICE_DISCONNECTED
        if (this is IBleServiceDiscovered)
            result += BLE_HANDLER_DEVICE_SERVICE
        if (this is IBleCharRead)
            result += BLE_HANDLER_CHAR_READ
        if (this is IBleCharWrite)
            result += BLE_HANDLER_CHAR_WRITE
        return result
    }

    fun apply(type: Int, client: BleClient?, gatt: BluetoothGatt?){
        when (type) {
            BLE_HANDLER_SCANNING -> {
                if (this is IBleScanning)
                    (this as IBleScanning).onScanning()
            }
            BLE_HANDLER_SCANNED -> {
                if (this is IBleScanned)
                    (this as IBleScanned).onScanned()
            }
            BLE_HANDLER_DEVICE_FOUND -> {
                if (this is IBleDeviceFound)
                    (this as IBleDeviceFound).onDeviceFound(client!!)
            }
            BLE_HANDLER_BLE_ON -> {
                if (this is IBleOn)
                    (this as IBleOn).onBleOn()
            }
            BLE_HANDLER_BLE_OFF -> {
                if (this is IBleOff)
                    (this as IBleOff).onBleOff()
            }
            BLE_HANDLER_DEVICE_CONNECTING -> {
                if (this is IBleConnecting)
                    (this as IBleConnecting).onBleConnecting(client!!)
            }
            BLE_HANDLER_DEVICE_CONNECTED -> {
                if (this is IBleConnected)
                    (this as IBleConnected).onBleConnected(client!!)
            }
            BLE_HANDLER_DEVICE_DISCONNECTING -> {
                if (this is IBleDisconnecting)
                    (this as IBleDisconnecting).onBleDisconnecting(client!!)
            }
            BLE_HANDLER_DEVICE_DISCONNECTED -> {
                if (this is IBleDisconnected)
                    (this as IBleDisconnected).onBleDisconnected(client!!)
            }
            BLE_HANDLER_DEVICE_SERVICE -> {
                if (this is IBleServiceDiscovered)
                    (this as IBleServiceDiscovered).onBleServiceDiscovered(client!!, gatt!!)
            }
            BLE_HANDLER_CHAR_READ -> {
                if (this is IBleCharRead)
                    (this as IBleCharRead).onBleCharRead(client!!, gatt!!)
            }
            BLE_HANDLER_CHAR_WRITE -> {
                if (this is IBleCharWrite)
                    (this as IBleCharWrite).onBleCharWrite(client!!, gatt!!)
            }
        }
    }
}