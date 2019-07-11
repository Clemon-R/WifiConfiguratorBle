package fr.rtone.demowificonfigurator.ble.handlers

import android.bluetooth.BluetoothGatt
import fr.rtone.demowificonfigurator.ble.BleClient

interface IBleServiceDiscovered {
    fun onBleServiceDiscovered(client: BleClient, gatt: BluetoothGatt)
}