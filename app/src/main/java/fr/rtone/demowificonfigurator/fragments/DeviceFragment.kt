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
import fr.rtone.demowificonfigurator.ble.BleHandler
import fr.rtone.demowificonfigurator.ble.handlers.IBleDeviceFound
import fr.rtone.demowificonfigurator.ble.handlers.IBleOff
import fr.rtone.demowificonfigurator.ble.handlers.IBleScanned
import fr.rtone.demowificonfigurator.ble.handlers.IBleScanning


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
    private var recycler: RecyclerView? = null

    private var btnScan: Button? = null
    private var pbScan: ProgressBar? = null

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

        btnScan = view.findViewById<Button>(R.id.btnScan)
        pbScan = view.findViewById<ProgressBar>(R.id.pbScan)
        recycler = view.findViewById<RecyclerView>(R.id.listDevices)
        if (btnScan != null) {
            btnScan!!.setOnClickListener {
                BleAdapter.instance()!!.startScan()
            }
        }

        if (pbScan != null)
            pbScan!!.visibility = View.GONE

        // Set the adapter
        if (recycler != null) {
            with(recycler!!) {
                layoutManager = LinearLayoutManager(context)
                adapter = BleDeviceRecyclerViewAdapter(
                    items,
                    this@DeviceFragment
                )
            }
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

    override fun onDetach() {
        super.onDetach()
        listener.destroy()
    }


    override fun onBleSelected(client: BleClient) {
        if (BleAdapter.instance()!!.scanning)
            BleAdapter.instance()!!.stopScan()
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        //transaction.add(R.id.content, ConnectedFragment.newInstance(client.device.address))
        transaction.replace(R.id.content, ConnectedFragment.newInstance(client.device.address))
        transaction.addToBackStack(null)
        //transaction.remove(this)

        transaction.commit()
    }

    inner class BleListener(private val adapter: BleAdapter) : BleHandler(adapter), IBleScanning, IBleScanned, IBleDeviceFound, IBleOff {
        override fun onBleOff() {
            if (adapter.scanning)
                adapter.stopScan()
            else
                onScanned()
        }

        override fun onDeviceFound(client: BleClient) {
            Log.d(TAG, "Device found")
            items += client
            recycler?.adapter?.notifyDataSetChanged()
        }

        override fun onScanning() {
            Log.d(TAG, "Start scan")
            items.clear()
            recycler?.adapter?.notifyDataSetChanged()
            if (btnScan != null)
                btnScan!!.isEnabled = false
            if (pbScan != null)
                pbScan!!.visibility = View.VISIBLE
        }

        override fun onScanned() {
            Log.d(TAG, "End scan")
            if (btnScan != null)
                btnScan!!.isEnabled = true
            if (pbScan != null)
                pbScan!!.visibility = View.GONE
        }

    }
}
