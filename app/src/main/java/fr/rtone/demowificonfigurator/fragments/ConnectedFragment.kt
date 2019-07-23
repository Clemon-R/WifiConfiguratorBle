package fr.rtone.demowificonfigurator.fragments

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import fr.rtone.demowificonfigurator.BleConstant.Companion.ACTION_CHAR
import fr.rtone.demowificonfigurator.BleConstant.Companion.PASSWORD_CHAR
import fr.rtone.demowificonfigurator.BleConstant.Companion.SSID_CHAR
import fr.rtone.demowificonfigurator.BleConstant.Companion.WIFI_SERVICE
import fr.rtone.demowificonfigurator.MainActivity

import fr.rtone.demowificonfigurator.R
import fr.rtone.demowificonfigurator.ble.BleAdapter
import fr.rtone.demowificonfigurator.ble.BleClient
import fr.rtone.demowificonfigurator.ble.BleClientState
import fr.rtone.demowificonfigurator.ble.handler.BleHandler
import fr.rtone.demowificonfigurator.ble.handler.BleParam
import fr.rtone.demowificonfigurator.ble.handler.interfaces.*
import java.lang.Thread.sleep

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
    private lateinit var editSsid: EditText
    private lateinit var editPassword: EditText

    private lateinit var bleGatt: BluetoothGatt
    private lateinit var wifiService: BluetoothGattService

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
        editSsid = view.findViewById(R.id.editSsid)
        editPassword = view.findViewById(R.id.editPassword)

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
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        btnSave.setOnClickListener {
            if (editSsid.text.isEmpty() || editPassword.text.isEmpty()) {
                Toast.makeText(context, "You need an ssid", Toast.LENGTH_LONG)
            } else {
                val ssid = wifiService.characteristics.find { it.uuid.toString().substring(4, 8).toInt(16) == SSID_CHAR }
                val password = wifiService.characteristics.find { it.uuid.toString().substring(4, 8).toInt(16) == PASSWORD_CHAR }
                val action = wifiService.characteristics.find { it.uuid.toString().substring(4, 8).toInt(16) == ACTION_CHAR }
                if (ssid != null && password != null && action != null){
                    it.isEnabled = false
                    btnDisconnect.isEnabled = false
                    pbConnect.visibility = View.VISIBLE

                    ssid.setValue(editSsid.text.toString())
                    password.setValue(editPassword.text.toString())
                    action.value = byteArrayOf(0)

                    bleGatt.writeCharacteristic(ssid)
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

    private fun getFromBLEWifiData()
    {
        if (client.state != BleClientState.CONNECTED)
            return
        val ssid = wifiService.characteristics.find { it.uuid.toString().substring(4, 8).toInt(16) == SSID_CHAR }
        if (ssid != null){
            bleGatt.readCharacteristic(ssid)
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
                    getFromBLEWifiData()
                }.start()
            }
        }

        override fun onBleCharWrite(param: BleParam) {
            when (param.char?.uuid.toString().substring(4, 8).toInt(16)){
                SSID_CHAR -> {
                    val password = wifiService.characteristics.find { it.uuid.toString().substring(4, 8).toInt(16) == PASSWORD_CHAR }
                    if (password != null)
                        bleGatt.writeCharacteristic(password)
                }
                PASSWORD_CHAR -> {
                    val action = wifiService.characteristics.find { it.uuid.toString().substring(4, 8).toInt(16) == ACTION_CHAR }
                    if (action != null)
                        bleGatt.writeCharacteristic(action)
                }
                ACTION_CHAR -> {
                    context.runOnUiThread {
                        view?.findViewById<Button>(R.id.btnSave)?.isEnabled = true
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
                    val password = wifiService.characteristics.find { it.uuid.toString().substring(4, 8).toInt(16) == PASSWORD_CHAR }
                    if (password != null)
                        bleGatt.readCharacteristic(password)
                    else
                        context.runOnUiThread {
                            btnDisconnect.isEnabled = true
                            btnConnect.isEnabled = false
                            pbConnect.visibility = View.GONE
                            viewWifiConfigurator.visibility = View.VISIBLE
                        }
                }
                PASSWORD_CHAR -> {
                    context.runOnUiThread {
                        editPassword.text.replace(0, editPassword.text.length, param.char!!.getStringValue(0))
                        viewWifiConfigurator.visibility = View.VISIBLE
                        btnDisconnect.isEnabled = true
                        btnConnect.isEnabled = false
                        pbConnect.visibility = View.GONE
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
                                getFromBLEWifiData()
                            }.start()
                        }
                    }
                }
            }
            if (!wifiServiceFound) {
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
                lblError.visibility = View.GONE
                Toast.makeText(context, "You are now disconnected", Toast.LENGTH_LONG)
            }
        }
    }
}
