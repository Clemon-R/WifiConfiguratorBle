package fr.rtone.demowificonfigurator.ble.handler.interfaces

import fr.rtone.demowificonfigurator.ble.handler.BleParam

interface IBleBonding
{
	fun onBleBonding(param: BleParam)
}