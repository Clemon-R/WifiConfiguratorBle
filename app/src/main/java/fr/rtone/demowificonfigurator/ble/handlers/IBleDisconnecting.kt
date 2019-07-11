package fr.rtone.demowificonfigurator.ble.handlers

import fr.rtone.demowificonfigurator.ble.BleClient

interface IBleDisconnecting {
    fun onBleDisconnecting(client: BleClient)
}