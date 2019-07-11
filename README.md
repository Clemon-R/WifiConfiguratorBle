## WifiConfigurator

Using Kotlin Jvm for Android, and my own BLE librairy (app/src/main/java/fr.rtone.demowificonfigurator/ble)

##

### Needed

You just need to run the app, but you a need a gatt server compatible.

##
### How to use ?

You can use only the librairy, by copying the `ble` folder, and creating a class with extends of `BleHandler`, and you can choose all the 'IBleX' interface.
The BleHandler only need a BleAdapter, that you can get it with  `BleAdapter.instance()` if already created or `BleAdapter.newInstance(activity)`.
When you are done with you event handler, you can destroy it like this `X.destroy()`, its simple and basic.

##

### Purpose

Changing the wifi ssid/password of a product with BLE.

##
### Authors
 * [Raphael-G](https://github.com/Clemon-R)
