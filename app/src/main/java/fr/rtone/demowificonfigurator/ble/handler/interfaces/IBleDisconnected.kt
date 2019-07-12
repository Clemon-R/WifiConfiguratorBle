package fr.rtone.demowificonfigurator.ble.handler.interfaces

import fr.rtone.demowificonfigurator.ble.BleClient
import fr.rtone.demowificonfigurator.ble.handler.BleParam

interface IBleDisconnected {
    fun onBleDisconnected(param: BleParam)
}