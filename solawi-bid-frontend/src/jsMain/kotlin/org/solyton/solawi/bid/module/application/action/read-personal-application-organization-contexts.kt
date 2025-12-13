package org.solyton.solawi.bid.module.application.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.application.data.ApplicationOrganizationRelations
import org.solyton.solawi.bid.module.application.data.ReadApplicationOrganizationContextRelations
import org.solyton.solawi.bid.module.application.data.management.ApplicationManagement
import org.solyton.solawi.bid.module.application.data.management.applicationOrganizationRelations
import org.solyton.solawi.bid.module.application.data.toDomainType


const val READ_PERSONAL_APPLICATION_ORGANIZATION_CONTEXT_RELATIONS = "ReadPersonalApplicationOrganizationContextRelations"

fun readPersonalApplicationOrganizationContextRelations(
    nameSuffix: String = "",
): Action<ApplicationManagement, ReadApplicationOrganizationContextRelations, ApplicationOrganizationRelations> =
    Action<ApplicationManagement, ReadApplicationOrganizationContextRelations, ApplicationOrganizationRelations>(
        name = READ_PERSONAL_APPLICATION_ORGANIZATION_CONTEXT_RELATIONS+nameSuffix,
        reader = { ReadApplicationOrganizationContextRelations },
        endPoint = ReadApplicationOrganizationContextRelations::class,
        writer = applicationOrganizationRelations.set contraMap {
                relations: ApplicationOrganizationRelations -> relations.toDomainType()
        }
    )
