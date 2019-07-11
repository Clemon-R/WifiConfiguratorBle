package fr.rtone.demowificonfigurator.ble.handlers

import android.bluetooth.BluetoothGatt
import fr.rtone.demowificonfigurator.ble.BleClient

interface IBleCharWrite
{
    fun onBleCharWrite(client: BleClient, gatt: BluetoothGatt)
}