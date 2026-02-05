package org.solyton.solawi.bid.module.distribution.data.management

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite
import org.evoleq.compose.modal.Modals
import org.evoleq.device.data.Device
import org.evoleq.optics.ReadOnly
import org.evoleq.optics.storage.ActionDispatcher
import org.solyton.solawi.bid.module.context.data.Context
import org.solyton.solawi.bid.module.distribution.data.distributionpoint.DistributionPoint
import org.solyton.solawi.bid.module.i18n.data.Environment
import org.solyton.solawi.bid.module.i18n.data.I18N

@Lensify
data class DistributionManagement(
    @ReadWrite val context: Context = Context(),
    @ReadOnly val actions: ActionDispatcher<DistributionManagement> = ActionDispatcher {  },
    @ReadOnly val environment: Environment,
    @ReadWrite val deviceData: Device,
    @ReadWrite val modals: Modals<Int>,
    @ReadWrite val i18n: I18N,
    @ReadWrite val distributionPoints: List<DistributionPoint>
)
