package fr.rtone.demowificonfigurator.ble.handler

import android.bluetooth.BluetoothGatt
import fr.rtone.demowificonfigurator.ble.BleClient

data class BleParam(val client: BleClient?, val gatt: BluetoothGatt?)