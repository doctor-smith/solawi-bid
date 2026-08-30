package org.solyton.solawi.bid.application.effect

import androidx.compose.runtime.Composable
import kotlinx.coroutines.launch
import org.evoleq.compose.effect.LaunchedEffectOnSource
import org.evoleq.optics.storage.Read
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.context
import org.solyton.solawi.bid.application.service.dispatchContextOf
import org.solyton.solawi.bid.module.application.data.ApplicationName
import org.solyton.solawi.bid.module.context.data.current
import org.solyton.solawi.bid.module.user.data.api.OrganizationId

@Composable
fun Storage<Application>.ForceContext(
    applicationName: ApplicationName,
    organizationId: OrganizationId
) {
    LaunchedEffectOnSource(Read(this * context * current)) {
        launch {
            dispatchContextOf(
                applicationName,
                organizationId
            )
        }
    }
}
