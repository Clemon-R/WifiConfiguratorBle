package fr.rtone.demowificonfigurator.ble.handler.interfaces

import fr.rtone.demowificonfigurator.ble.BleClient
import fr.rtone.demowificonfigurator.ble.handler.BleParam

interface IBleDisconnecting {
    fun onBleDisconnecting(param: BleParam)
}