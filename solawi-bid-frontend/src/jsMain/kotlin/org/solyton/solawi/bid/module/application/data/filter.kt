package org.solyton.solawi.bid.module.application.data

import org.evoleq.math.Reader
import org.solyton.solawi.bid.module.application.data.management.ApplicationManagement

@Suppress("FunctionName")
fun PersonalOrganizationApplicationContext (applicationId: String, organizationId: String): Reader<ApplicationManagement, String> = Reader {
    app  -> app.applicationOrganizationRelations.first {
        it.organizationId == organizationId && it.applicationId == applicationId
    }.contextId
}
