package io.github.kaiseiyokoyama.joykonkt

import io.github.kaiseiyokoyama.joykonkt.controller.Controller
import io.github.kaiseiyokoyama.joykonkt.controller.ProController
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import org.hid4java.*
import org.hid4java.event.HidServicesEvent

object Manager : HidServicesListener {
    val attachedChannel: Channel<Controller> = Channel(50)
    val detachedChannel: Channel<HidDevice> = Channel()
    val service: HidServices by lazy {
        val config = HidServicesSpecification()
        config.isAutoStart = true
        HidManager.getHidServices(config)
    }

    init {
        service.addHidServicesListener(this)
        service.start()
    }

    override fun hidDeviceAttached(event: HidServicesEvent?) {
        event?.let { it ->
//            println("Attached: ${it.hidDevice.product}")
            ProController.tryNew(it.hidDevice)?.let { pc ->
                attachedChannel.trySendBlocking(pc)
            }
        }
    }

    override fun hidDeviceDetached(event: HidServicesEvent?) {
        event?.let {
            detachedChannel.trySendBlocking(it.hidDevice)
        }
    }

    override fun hidFailure(event: HidServicesEvent?) {
//        TODO("Not yet implemented")
    }
}