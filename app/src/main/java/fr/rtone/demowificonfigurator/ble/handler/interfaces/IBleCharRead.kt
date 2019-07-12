package fr.rtone.demowificonfigurator.ble.handler.interfaces

import android.bluetooth.BluetoothGatt
import fr.rtone.demowificonfigurator.ble.BleClient
import fr.rtone.demowificonfigurator.ble.handler.BleParam

interface IBleCharRead
{
    fun onBleCharRead(param: BleParam)
}