package fr.rtone.demowificonfigurator.ble.handlers

import android.bluetooth.BluetoothGatt
import fr.rtone.demowificonfigurator.ble.BleClient

interface IBleCharRead
{
    fun onBleCharRead(client: BleClient, gatt: BluetoothGatt)
}