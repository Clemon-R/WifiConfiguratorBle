package fr.rtone.demowificonfigurator.ble.handler.interfaces

import fr.rtone.demowificonfigurator.ble.handler.BleParam

interface IBleOn{
    fun onBleOn(param: BleParam)
}