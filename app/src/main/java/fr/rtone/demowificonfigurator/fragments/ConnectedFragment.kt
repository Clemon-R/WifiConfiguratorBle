package fr.rtone.demowificonfigurator.fragments

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic.*
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import fr.rtone.demowificonfigurator.BleConstant.Companion.DELAI_CHAR
import fr.rtone.demowificonfigurator.BleConstant.Companion.MQTT_ACTION_CHAR
import fr.rtone.demowificonfigurator.BleConstant.Companion.MQTT_SERVICE
import fr.rtone.demowificonfigurator.BleConstant.Companion.WIFI_ACTION_CHAR
import fr.rtone.demowificonfigurator.BleConstant.Companion.PASSWORD_CHAR
import fr.rtone.demowificonfigurator.BleConstant.Companion.PORT_CHAR
import fr.rtone.demowificonfigurator.BleConstant.Companion.SENSORS_ENABLE_CHAR
import fr.rtone.demowificonfigurator.BleConstant.Companion.SENSORS_SERVICE
import fr.rtone.demowificonfigurator.BleConstant.Companion.SSID_CHAR
import fr.rtone.demowificonfigurator.BleConstant.Companion.URL_CHAR
import fr.rtone.demowificonfigurator.BleConstant.Companion.WIFI_SERVICE
import fr.rtone.demowificonfigurator.MainActivity

import fr.rtone.demowificonfigurator.R
import fr.rtone.demowificonfigurator.ble.BleAdapter
import fr.rtone.demowificonfigurator.ble.BleClient
import fr.rtone.demowificonfigurator.ble.BleClientState
import fr.rtone.demowificonfigurator.ble.handler.BleHandler
import fr.rtone.demowificonfigurator.ble.handler.BleParam
import fr.rtone.demowificonfigurator.ble.handler.interfaces.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_MAC = "bleMacAdress"

class ConnectedFragment : Fragment() {
    private lateinit var context: MainActivity
    private lateinit var client: BleClient
    private lateinit var listener: BleListener

    private lateinit var btnConnect: Button
    private lateinit var btnDisconnect: Button
    private lateinit var pbConnect: ProgressBar
    private lateinit var lblError: TextView
    private lateinit var viewWifiConfigurator: LinearLayout
    private lateinit var viewMqttConfigurator: LinearLayout
    private lateinit var viewSensorsConfigurator: LinearLayout
    private lateinit var editSsid: EditText
    private lateinit var editPassword: EditText
    private lateinit var editDelai: EditText
    private lateinit var sensorsState: Switch

    private lateinit var editUrl: EditText
    private lateinit var editPort: EditText

    private lateinit var bleGatt: BluetoothGatt
    private lateinit var wifiService: BluetoothGattService
    private lateinit var mqttService: BluetoothGattService
    private lateinit var sensorsService: BluetoothGattService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BleAdapter.instance() == null)
            throw RuntimeException("Missing adapter for BLE")
        arguments?.let {
            val mac = it.getString(ARG_MAC, null) ?: throw java.lang.RuntimeException("Missing device mac")
            client = BleAdapter.instance()!!.devices[mac]!!
        }
        listener = BleListener(BleAdapter.instance()!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_connected, container, false)

        btnConnect = view.findViewById(R.id.btnConnect)
        btnDisconnect = view.findViewById(R.id.btnDisconnect)
        pbConnect = view.findViewById(R.id.pbConnect)

        lblError = view.findViewById(R.id.lblError)

        viewWifiConfigurator = view.findViewById(R.id.viewWifiConfigurator)
        viewMqttConfigurator = view.findViewById(R.id.viewMqttConfigurator)
        viewSensorsConfigurator = view.findViewById(R.id.viewSensorsConfigurator)

        editSsid = view.findViewById(R.id.editSsid)
        editPassword = view.findViewById(R.id.editPassword)

        editUrl = view.findViewById(R.id.editUrl)
        editPort = view.findViewById(R.id.editPort)

        editDelai = view.findViewById(R.id.editDelai)
        sensorsState = view.findViewById(R.id.sensorsState)

        btnConnect.setOnClickListener {
            client.connect()
            btnConnect.isEnabled = false
            pbConnect.visibility = View.VISIBLE
            lblError.visibility = View.GONE
        }
        btnDisconnect.setOnClickListener {
            client.disconnect()
            btnDisconnect.isEnabled = false
            pbConnect.visibility = View.VISIBLE
        }
        pbConnect.visibility = View.GONE


        lblError.visibility = View.GONE
        lblError.text = ""

        viewWifiConfigurator.visibility = View.GONE
        viewMqttConfigurator.visibility = View.GONE
        viewSensorsConfigurator.visibility = View.GONE

        val lblName = view.findViewById<TextView>(R.id.lblName)
        if (lblName != null){
            if (client.device.name != null)
                lblName.text = client.device.name
            else
                lblName.text = ""
        }
        val lblMac = view.findViewById<TextView>(R.id.lblMac)
        if (lblMac != null){
            if (client.device.address != null)
                lblMac.text = client.device.address
            else
                lblMac.text = ""
        }

        val btnSaveWifi = view.findViewById<Button>(R.id.btnSaveWifi)
        btnSaveWifi.setOnClickListener {
            if (editSsid.text.isEmpty() || editPassword.text.isEmpty()) {
                Toast.makeText(context, "You need an ssid and password", Toast.LENGTH_LONG)
            } else {
                if (client.sendString(WIFI_SERVICE, SSID_CHAR, editSsid.text.toString())){
                    client.sendString(WIFI_SERVICE, PASSWORD_CHAR, editPassword.text.toString())
                    client.sendBytes(WIFI_SERVICE, WIFI_ACTION_CHAR, byteArrayOf(0))
                    it.isEnabled = false
                    btnDisconnect.isEnabled = false
                    pbConnect.visibility = View.VISIBLE
                } else {
                    Toast.makeText(context, "Error while sending data", Toast.LENGTH_LONG)
                }
            }
        }

        val btnSaveMqtt = view.findViewById<Button>(R.id.btnSaveMqtt)
        btnSaveMqtt.setOnClickListener {
            if (editUrl.text.isEmpty() || editPort.text.isEmpty()) {
                Toast.makeText(context, "You need an url and port", Toast.LENGTH_LONG)
            } else {
                if (client.sendString(MQTT_SERVICE, URL_CHAR, editUrl.text.toString())){
                    client.sendInt(MQTT_SERVICE, PORT_CHAR, editPort.text.toString().toInt(), FORMAT_UINT16)
                    client.sendBytes(MQTT_SERVICE, MQTT_ACTION_CHAR, byteArrayOf(0))
                    it.isEnabled = false
                    btnDisconnect.isEnabled = false
                    pbConnect.visibility = View.VISIBLE
                } else {
                    Toast.makeText(context, "Error while sending data", Toast.LENGTH_LONG)
                }
            }
        }

        val btnSaveSensors = view.findViewById<Button>(R.id.btnSaveSensors)
        btnSaveSensors.setOnClickListener {
            if (editDelai.text.isEmpty()) {
                Toast.makeText(context, "You need a delai", Toast.LENGTH_LONG)
            } else {
                if (client.sendInt(SENSORS_SERVICE, SENSORS_ENABLE_CHAR, (if (sensorsState.isChecked) 1 else 0), FORMAT_UINT8)){
                    client.sendInt(SENSORS_SERVICE, DELAI_CHAR, editDelai.text.toString().toInt(), FORMAT_UINT32)
                } else {
                    Toast.makeText(context, "Error while sending data", Toast.LENGTH_LONG)
                }
            }
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            this.context = context
        } else {
            throw RuntimeException("$context must be MainActivity")
        }
    }

    override fun onDetach() {
        super.onDetach()
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.remove(this)

        transaction.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        client.disconnect()
    }

    companion object {
        private val TAG = "ConnectedFragment"
        fun newInstance(mac: String) =
            ConnectedFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_MAC, mac)
                }
            }
    }

    inner class BleListener(adapter: BleAdapter) : BleHandler(adapter),
	    IBleDisconnected,
	    IBleServiceDiscovered,
	    IBleCharRead,
	    IBleCharWrite,
        IBleNotBonded,
        IBleBonding,
        IBleBonded{

        var needBond = false
        var bondAvailable = false

        var wifiServiceFound = false
        var mqttServiceFound = false
        var sensorsServiceFound = false

        override fun onBleNotBonded(param: BleParam) {
            bondAvailable = false
        }

        override fun onBleBonding(param: BleParam) {
            needBond = true
        }

        override fun onBleBonded(param: BleParam) {
            bondAvailable = true
            if (wifiServiceFound){
                Thread {
                    client.read(WIFI_SERVICE, SSID_CHAR)
                    client.read(WIFI_SERVICE, PASSWORD_CHAR)
                }.start()
            }
            if (mqttServiceFound){
                Thread {
                    client.read(MQTT_SERVICE, URL_CHAR)
                    client.read(MQTT_SERVICE, PORT_CHAR)
                }.start()
            }
            if (sensorsServiceFound){
                Thread {
                    client.read(SENSORS_SERVICE, SENSORS_ENABLE_CHAR)
                    client.read(SENSORS_SERVICE, DELAI_CHAR)
                }.start()
            }
        }

        override fun onBleCharWrite(param: BleParam) {
            when (param.char?.uuid.toString().substring(4, 8).toInt(16)){
                WIFI_ACTION_CHAR -> {
                    context.runOnUiThread {
                        view?.findViewById<Button>(R.id.btnSaveWifi)?.isEnabled = true
                        btnDisconnect.isEnabled = true
                        pbConnect.visibility = View.GONE
                    }
                }
                MQTT_ACTION_CHAR -> {
                    context.runOnUiThread {
                        view?.findViewById<Button>(R.id.btnSaveMqtt)?.isEnabled = true
                        btnDisconnect.isEnabled = true
                        pbConnect.visibility = View.GONE
                    }
                }
            }
        }

        override fun onBleCharRead(param: BleParam) {
            when (param.char?.uuid.toString().substring(4, 8).toInt(16)){
                SSID_CHAR -> {
                    context.runOnUiThread {
                        editSsid.text.replace(0, editSsid.text.length, param.char!!.getStringValue(0))
                    }
                }
                PASSWORD_CHAR -> {
                    context.runOnUiThread {
                        editPassword.text.replace(0, editPassword.text.length, param.char!!.getStringValue(0))
                    }
                }
                URL_CHAR -> {
                    context.runOnUiThread {
                        editUrl.text.replace(0, editUrl.text.length, param.char!!.getStringValue(0))
                    }
                }
                PORT_CHAR -> {
                    context.runOnUiThread {
                        editPort.text.replace(0, editPort.text.length, "${param.char!!.getIntValue(FORMAT_UINT16, 0)}")
                    }
                }
                SENSORS_ENABLE_CHAR -> {
                    context.runOnUiThread {
                        sensorsState.isChecked = param.char!!.getIntValue(FORMAT_UINT8, 0) == 1
                    }
                }
                DELAI_CHAR -> {
                    context.runOnUiThread {
                        editDelai.text.replace(0, editDelai.text.length, "${param.char!!.getIntValue(FORMAT_UINT32, 0)}")
                    }
                }
            }
        }

        override fun onBleServiceDiscovered(param: BleParam) {
            Log.d(TAG, "New services found, checking uuid on 16 bits")
            bleGatt = param.gatt!!
            for (service in bleGatt.services){
                when (service.uuid.toString().substring(4, 8).toInt(16)){
                    WIFI_SERVICE -> {
                        Log.d(TAG, "Wifi service found !")
                        wifiServiceFound = true
                        wifiService = service
                        context.runOnUiThread { viewWifiConfigurator.visibility = View.VISIBLE}
                        if (!needBond || bondAvailable) {
                            Thread {
                                client.read(WIFI_SERVICE, SSID_CHAR)
                                client.read(WIFI_SERVICE, PASSWORD_CHAR)
                            }.start()
                        }
                    }
                    MQTT_SERVICE -> {
                        Log.d(TAG, "Mqtt service found !")
                        mqttService = service
                        mqttServiceFound = true
                        context.runOnUiThread { viewMqttConfigurator.visibility = View.VISIBLE}
                        if (!needBond || bondAvailable) {
                            Thread {
                                client.read(MQTT_SERVICE, URL_CHAR)
                                client.read(MQTT_SERVICE, PORT_CHAR)
                            }.start()
                        }
                    }
                    SENSORS_SERVICE -> {
                        Log.d(TAG, "Sensors service found !")
                        sensorsService = service
                        sensorsServiceFound = true
                        context.runOnUiThread { viewSensorsConfigurator.visibility = View.VISIBLE}
                        if (!needBond || bondAvailable) {
                            Thread {
                                client.read(SENSORS_SERVICE, SENSORS_ENABLE_CHAR)
                                client.read(SENSORS_SERVICE, DELAI_CHAR)
                            }.start()
                        }
                    }
                }
            }
            if (!wifiServiceFound && !mqttServiceFound && !sensorsServiceFound) {
                context.runOnUiThread {
                    lblError.visibility = View.VISIBLE
                    lblError.text = "Unhandle device"
                }
            }
            context.runOnUiThread {
                btnDisconnect.isEnabled = true
                btnConnect.isEnabled = false
                pbConnect.visibility = View.GONE
            }
        }

        override fun onBleDisconnected(param: BleParam) {
            context.runOnUiThread {
                btnConnect.isEnabled = true
                btnDisconnect.isEnabled = false
                pbConnect.visibility = View.GONE
                viewWifiConfigurator.visibility = View.GONE
                viewMqttConfigurator.visibility = View.GONE
                viewSensorsConfigurator.visibility = View.GONE
                lblError.visibility = View.GONE
                Toast.makeText(context, "You are now disconnected", Toast.LENGTH_LONG)
            }
        }
    }
}
