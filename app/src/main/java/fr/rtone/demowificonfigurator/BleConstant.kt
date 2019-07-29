package fr.rtone.demowificonfigurator

class BleConstant {
    companion object{
        val WIFI_SERVICE = 0x00ff
        val MQTT_SERVICE = 0x00fe
        val SENSORS_SERVICE = 0x00fd

        val SSID_CHAR       = 0xff01
        val PASSWORD_CHAR   = 0xff02
        val WIFI_ACTION_CHAR     = 0xff03

        val URL_CHAR     = 0xfe01
        val PORT_CHAR     = 0xfe02
        val MQTT_ACTION_CHAR     = 0xfe03

        val SENSORS_ENABLE_CHAR     = 0xfd01
        val DELAI_CHAR     = 0xfd02
        val TEMPERATURE_ENABLE_CHAR     = 0xfd03
        val HUMIDITY_ENABLE_CHAR     = 0xfd04
        val PRESSURE_ENABLE_CHAR     = 0xfd05
        val COLOR_ENABLE_CHAR     = 0xfd06
    }
}