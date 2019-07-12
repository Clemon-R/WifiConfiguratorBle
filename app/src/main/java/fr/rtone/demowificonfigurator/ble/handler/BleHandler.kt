package fr.rtone.demowificonfigurator.ble.handler

import fr.rtone.demowificonfigurator.ble.BleAdapter
import kotlin.reflect.*


abstract class BleHandler(private val adapter: BleAdapter)
{
    companion object{
        private val TAG = "BleHandler"
    }

    init {
        adapter.handlers += (getTypes() to this)
    }

    fun destroy() {
        val types = getTypes()
        adapter.handlers -= types
    }

    fun getTypes(): Int {
	    var result = 0

	    for (type in BleHandlerType.values())
	    {
		    if (type.instanceParam?.kind != KParameter.Kind.INSTANCE) //Documentation say the first is the instance
			    continue
		    for (stype in this::class.supertypes){ //Checking all the extended class
			    if (type.instanceParam!!.type == stype) {
				    result += type.uuid
				    break
			    }
		    }
	    }
        return result
    }
}