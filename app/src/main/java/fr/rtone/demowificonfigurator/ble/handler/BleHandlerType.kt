package fr.rtone.demowificonfigurator.ble.handler

import fr.rtone.demowificonfigurator.ble.handler.interfaces.*
import kotlin.reflect.KCallable
import kotlin.reflect.KParameter
import kotlin.reflect.full.createType
import kotlin.reflect.typeOf

enum class BleHandlerType(private val id: Int, private val func: KCallable<Unit>){ //ATTENTION: Int = 32 bits so 31 id available
	SCANNING(0, IBleScanning::onScanning),
	SCANNED(1, IBleScanned::onScanned),
	DEVICE_FOUND(2, IBleDeviceFound::onDeviceFound),
	BT_ON(3, IBleOn::onBleOn),
	BT_OFF(4, IBleOff::onBleOff),
	DEVICE_CONNECTING(5, IBleConnecting::onBleConnecting),
	DEVICE_CONNECTED(6, IBleConnected::onBleConnected),
	DEVICE_DISCONNECTING(7, IBleDisconnecting::onBleDisconnecting),
	DEVICE_DISCONNECTED(8, IBleDisconnected::onBleDisconnected),
	SERVICE_DISCOVERED(9, IBleServiceDiscovered::onBleServiceDiscovered),
	CHAR_READ(10, IBleCharRead::onBleCharRead),
	CHAR_WRITE(11, IBleCharWrite::onBleCharWrite);

	val uuid = 1 shl id
	val instanceParam: KParameter?
		get() {
			if (func.parameters[0].kind != KParameter.Kind.INSTANCE) //Documentation say the first is the instance
				return null
			return func.parameters[0]
		}

	fun execute(instance: BleHandler, param: BleParam)
	{
		if (func.parameters[0].kind != KParameter.Kind.INSTANCE || //Documentation say the first is the instance
			func.parameters.size != 2 ||
			func.parameters[1].type != BleParam::class.createType())
			return
		func.call(instance, param)
	}
}