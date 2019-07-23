package fr.rtone.demowificonfigurator.ble.handler.interfaces

import fr.rtone.demowificonfigurator.ble.handler.BleParam

interface IBleNotBonded
{
	fun onBleNotBonded(param: BleParam)
}