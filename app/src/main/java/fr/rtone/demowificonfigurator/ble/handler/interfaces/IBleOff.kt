package fr.rtone.demowificonfigurator.ble.handler.interfaces

import fr.rtone.demowificonfigurator.ble.handler.BleParam

interface IBleOff{
    fun onBleOff(param: BleParam)
}