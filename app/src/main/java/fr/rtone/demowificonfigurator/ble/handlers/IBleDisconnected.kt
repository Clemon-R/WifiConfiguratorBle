package fr.rtone.demowificonfigurator.ble.handlers

import fr.rtone.demowificonfigurator.ble.BleClient

interface IBleDisconnected {
    fun onBleDisconnected(client: BleClient)
}