package io.github.kaiseiyokoyama.joykonkt

import org.hid4java.HidManager
import org.hid4java.HidServicesListener
import org.hid4java.HidServicesSpecification
import org.hid4java.event.HidServicesEvent

class Manager: HidServicesListener {
    init {
        val config = HidServicesSpecification()
        config.isAutoStart = false

        val service = HidManager.getHidServices(config)
        service.addHidServicesListener(this)

        service.start()
    }

    override fun hidDeviceAttached(event: HidServicesEvent?) {
        event?.let {
            println("Attached: ${it.hidDevice.product}")
        }
    }

    override fun hidDeviceDetached(event: HidServicesEvent?) {
        event?.let {
            println("Detached: ${it.hidDevice.product}")
        }
    }

    override fun hidFailure(event: HidServicesEvent?) {
//        TODO("Not yet implemented")
    }
}