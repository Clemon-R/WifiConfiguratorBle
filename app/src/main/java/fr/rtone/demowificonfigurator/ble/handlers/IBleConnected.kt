package fr.rtone.demowificonfigurator.ble.handlers

import fr.rtone.demowificonfigurator.ble.BleClient

interface IBleConnected {
    fun onBleConnected(client: BleClient)
}