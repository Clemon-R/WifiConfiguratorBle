package fr.rtone.demowificonfigurator

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import fr.rtone.demowificonfigurator.ble.BleAdapter
import fr.rtone.demowificonfigurator.fragments.ConnectedFragment
import fr.rtone.demowificonfigurator.fragments.DeviceFragment

class MainActivity : AppCompatActivity() {
    private var adapter: BleAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        adapter = BleAdapter.newInstance(this)

        setContentView(R.layout.activity_main)

        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.content, DeviceFragment())

        transaction.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter?.destroy()
    }
}
