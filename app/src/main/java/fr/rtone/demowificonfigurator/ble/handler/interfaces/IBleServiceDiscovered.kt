package fr.rtone.demowificonfigurator.ble.handler.interfaces

import android.bluetooth.BluetoothGatt
import fr.rtone.demowificonfigurator.ble.BleClient
import fr.rtone.demowificonfigurator.ble.handler.BleParam

interface IBleServiceDiscovered {
    fun onBleServiceDiscovered(param: BleParam)
}