package fr.rtone.demowificonfigurator.ble

import android.bluetooth.BluetoothProfile

enum class BleClientState(private val id: Int) {
	UNKNOW(-1),
	CONNECTING(BluetoothProfile.STATE_CONNECTING),
	CONNECTED(BluetoothProfile.STATE_CONNECTED),
	DISCONNECTING(BluetoothProfile.STATE_DISCONNECTING),
	DISCONNECTED(BluetoothProfile.STATE_DISCONNECTED);

	companion object{
		fun fromBluetoothProfile(id: Int) : BleClientState {
			for (type in BleClientState.values()){
				if (type.id == id)
					return type
			}
			return UNKNOW
		}
	}

	fun toBluetoothProfile() : Int
	{
		return id
	}
}