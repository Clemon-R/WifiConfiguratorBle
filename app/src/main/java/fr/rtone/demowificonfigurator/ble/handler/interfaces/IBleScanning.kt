package fr.rtone.demowificonfigurator.ble.handler.interfaces

import fr.rtone.demowificonfigurator.ble.handler.BleParam

interface IBleScanning
{
    fun onScanning(param: BleParam)
}