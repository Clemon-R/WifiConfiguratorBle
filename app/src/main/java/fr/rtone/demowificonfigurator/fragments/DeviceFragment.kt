package fr.rtone.demowificonfigurator.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import fr.rtone.demowificonfigurator.recyclerAdapters.BleDeviceRecyclerViewAdapter
import fr.rtone.demowificonfigurator.MainActivity
import fr.rtone.demowificonfigurator.R
import fr.rtone.demowificonfigurator.ble.BleAdapter
import fr.rtone.demowificonfigurator.ble.BleClient
import fr.rtone.demowificonfigurator.ble.handler.BleHandler
import fr.rtone.demowificonfigurator.ble.handler.BleParam
import fr.rtone.demowificonfigurator.ble.handler.interfaces.IBleDeviceFound
import fr.rtone.demowificonfigurator.ble.handler.interfaces.IBleOff
import fr.rtone.demowificonfigurator.ble.handler.interfaces.IBleScanned
import fr.rtone.demowificonfigurator.ble.handler.interfaces.IBleScanning


interface OnBleSelected {
    // TODO: Update argument type and name
    fun onBleSelected(client: BleClient)
}

class DeviceFragment: Fragment(), OnBleSelected {
    companion object{
        private val TAG = "DeviceFragment"
    }

    private lateinit var context: MainActivity
    private lateinit var listener: BleListener
    private val items: MutableList<BleClient> = mutableListOf()

    private lateinit var recycler: RecyclerView

    private lateinit var btnStartScan: Button
    private lateinit var btnStopScan: Button
    private lateinit var pbScan: ProgressBar

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (BleAdapter.instance() == null)
            throw RuntimeException("$context must contains an adapter")
        this.listener = BleListener(BleAdapter.instance()!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_device_list, container, false)

        btnStartScan = view.findViewById(R.id.btnScanStart)
        btnStopScan = view.findViewById(R.id.btnScanStop)
        pbScan = view.findViewById(R.id.pbScan)
        recycler = view.findViewById<RecyclerView>(R.id.listDevices)

        btnStartScan.setOnClickListener {
            BleAdapter.instance()!!.startScan()
        }
        btnStopScan.setOnClickListener {
            BleAdapter.instance()!!.stopScan()
        }

        pbScan.visibility = View.GONE

        // Set the adapter
        with(recycler) {
            layoutManager = LinearLayoutManager(context)
            adapter = BleDeviceRecyclerViewAdapter(
                items,
                this@DeviceFragment
            )
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            this.context = context
        } else {
            throw RuntimeException("$context must be an Acitivity")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listener.destroy()
    }


    override fun onBleSelected(client: BleClient) {
        if (BleAdapter.instance()!!.scanning)
            BleAdapter.instance()!!.stopScan()
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.replace(R.id.content, ConnectedFragment.newInstance(client.device.address))
        transaction.addToBackStack(null)

        transaction.commit()
    }

    inner class BleListener(private val adapter: BleAdapter) : BleHandler(adapter),
        IBleScanning,
        IBleScanned,
        IBleDeviceFound,
        IBleOff {

        override fun onBleOff(param: BleParam) {
            if (adapter.scanning)
                adapter.stopScan()
            else
              onScanned(param)
        }

        override fun onDeviceFound(param: BleParam) {
            if (items.contains(param.client))
                return
            Log.d(TAG, "Device found")
            items += param.client!!
            recycler.adapter?.notifyDataSetChanged()
        }

        override fun onScanning(param: BleParam) {
            Log.d(TAG, "Start scan")
            items.clear()
            recycler.adapter?.notifyDataSetChanged()
            btnStartScan.isEnabled = false
            btnStopScan.isEnabled = true
            pbScan.visibility = View.VISIBLE
        }

        override fun onScanned(param: BleParam) {
            Log.d(TAG, "End scan")
            btnStartScan.isEnabled = true
            btnStopScan.isEnabled = false
            pbScan.visibility = View.GONE
        }
    }
}
