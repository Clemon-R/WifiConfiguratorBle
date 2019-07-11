package fr.rtone.demowificonfigurator.fragments

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
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
import fr.rtone.demowificonfigurator.ble.BleHandler
import fr.rtone.demowificonfigurator.ble.handlers.*
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

    private var responseState = false

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
                    Thread{
                        ssid.setValue(editSsid.text.toString())
                        password.setValue(editPassword.text.toString())
                        action.value = byteArrayOf(0)

                        responseState = false
                        bleGatt.writeCharacteristic(ssid)
                        while (!responseState)
                            sleep(10)
                        responseState = false
                        bleGatt.writeCharacteristic(password)
                        while (!responseState)
                            sleep(10)
                        responseState = false
                        bleGatt.writeCharacteristic(action)
                        while (!responseState)
                            sleep(10)
                        context.runOnUiThread {
                            it.isEnabled = true
                            btnDisconnect.isEnabled = true
                            pbConnect.visibility = View.GONE
                        }
                    }.start()
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
        if (!client.connected)
            return
        val ssid = wifiService.characteristics.find { it.uuid.toString().substring(4, 8).toInt(16) == SSID_CHAR }
        val password = wifiService.characteristics.find { it.uuid.toString().substring(4, 8).toInt(16) == PASSWORD_CHAR }

        if (ssid != null && password != null){
            responseState = false
            bleGatt.readCharacteristic(ssid)
            while (!responseState)
                sleep(10)
            responseState = false
            bleGatt.readCharacteristic(password)
            while (!responseState)
                sleep(10)
            context.runOnUiThread {
                editSsid.text.clear()
                editSsid.text.append(ssid.getStringValue(0))
                editPassword.text.clear()
                editPassword.text.append(password.getStringValue(0))
            }
        }
    }

    inner class BleListener(adapter: BleAdapter) : BleHandler(adapter), IBleDisconnected, IBleServiceDiscovered, IBleCharRead, IBleCharWrite {
        override fun onBleCharWrite(client: BleClient, gatt: BluetoothGatt) {
            responseState = true
        }

        override fun onBleCharRead(client: BleClient, gatt: BluetoothGatt) {
            responseState = true
        }

        override fun onBleServiceDiscovered(client: BleClient, gatt: BluetoothGatt) {
            Log.d(TAG, "New services found, checking uuid on 16 bits")
            var serviceWifiFound = false
            bleGatt = gatt
            for (service in gatt.services){
                when (service.uuid.toString().substring(4, 8).toInt(16)){
                    WIFI_SERVICE -> {
                        Log.d(TAG, "Wifi service found !")
                        serviceWifiFound = true
                        wifiService = service
                        val job = Thread{
                            getFromBLEWifiData()
                        }
                        job.start()
                        context.runOnUiThread {
                            viewWifiConfigurator.visibility = View.VISIBLE
                        }
                    }
                }
            }
            context.runOnUiThread {
                btnDisconnect.isEnabled = true
                btnConnect.isEnabled = false
                pbConnect.visibility = View.GONE
            }
            if (!serviceWifiFound){
                context.runOnUiThread {
                    lblError.visibility = View.VISIBLE
                    lblError.text = "Unhandle device"
                }
            }
        }

        override fun onBleDisconnected(client: BleClient) {
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
