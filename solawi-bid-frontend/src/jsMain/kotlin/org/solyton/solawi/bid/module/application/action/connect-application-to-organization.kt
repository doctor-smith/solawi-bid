package org.solyton.solawi.bid.module.application.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.application.data.ApplicationOrganizationRelations
import org.solyton.solawi.bid.module.application.data.ConnectApplicationToOrganization
import org.solyton.solawi.bid.module.application.data.management.ApplicationManagement
import org.solyton.solawi.bid.module.application.data.management.applicationOrganizationRelations
import org.solyton.solawi.bid.module.application.data.toDomainType


const val CONNECT_APPLICATION_TO_ORGANIZATION = "ConnectApplicationToOrganization"

fun connectApplicationToOrganization(
    applicationId: String,
    organizationId: String,
    moduleIds: List<String>,
    nameSuffix: String = "",
): Action<ApplicationManagement, ConnectApplicationToOrganization, ApplicationOrganizationRelations> =
    Action<ApplicationManagement, ConnectApplicationToOrganization, ApplicationOrganizationRelations>(
        name = CONNECT_APPLICATION_TO_ORGANIZATION+nameSuffix,
        reader = { ConnectApplicationToOrganization(applicationId, organizationId, moduleIds) },
        endPoint = ConnectApplicationToOrganization::class,
        writer = applicationOrganizationRelations.set contraMap {
            relations: ApplicationOrganizationRelations -> relations.toDomainType()
        }
    )
