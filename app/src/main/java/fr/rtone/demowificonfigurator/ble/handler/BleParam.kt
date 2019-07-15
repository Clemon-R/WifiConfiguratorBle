package fr.rtone.demowificonfigurator.ble.handler

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import fr.rtone.demowificonfigurator.ble.BleClient

data class BleParam(
	val client: BleClient?,
	val gatt: BluetoothGatt?,
	val char: BluetoothGattCharacteristic?,
	val permitted: Boolean?
)