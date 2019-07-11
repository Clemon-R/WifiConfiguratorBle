package fr.rtone.demowificonfigurator.ble.handlers

import fr.rtone.demowificonfigurator.ble.BleClient

interface IBleDeviceFound {
    fun onDeviceFound(client: BleClient)
}