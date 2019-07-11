package fr.rtone.demowificonfigurator.recyclerAdapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import fr.rtone.demowificonfigurator.R


import fr.rtone.demowificonfigurator.ble.BleClient
import fr.rtone.demowificonfigurator.fragments.OnBleSelected

import kotlinx.android.synthetic.main.fragment_device.view.*

class BleDeviceRecyclerViewAdapter(
    private val items: MutableList<BleClient>,
    private val mListener: OnBleSelected
) : RecyclerView.Adapter<BleDeviceRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as BleClient
            mListener.onBleSelected(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_device, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.macView.text = item.device.address
        holder.nameView.text = item.device.name

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val macView: TextView = mView.mac
        val nameView: TextView = mView.name

        override fun toString(): String {
            return super.toString() + " '" + macView.text + "'"
        }
    }
}
