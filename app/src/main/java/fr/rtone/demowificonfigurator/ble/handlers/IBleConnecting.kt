package fr.rtone.demowificonfigurator.ble.handlers

import fr.rtone.demowificonfigurator.ble.BleClient

interface IBleConnecting {
    fun onBleConnecting(client: BleClient)
}