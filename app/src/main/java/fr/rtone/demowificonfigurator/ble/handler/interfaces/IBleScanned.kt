package fr.rtone.demowificonfigurator.ble.handler.interfaces

import fr.rtone.demowificonfigurator.ble.handler.BleParam

interface IBleScanned {
    fun onScanned(param: BleParam)
}