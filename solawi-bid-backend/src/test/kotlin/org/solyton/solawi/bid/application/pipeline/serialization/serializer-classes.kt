package org.solyton.solawi.bid.application.pipeline.serialization

import org.solyton.solawi.bid.module.user.data.api.organization.AddMember
import org.solyton.solawi.bid.module.user.data.api.organization.ImportMembers
import org.solyton.solawi.bid.module.user.data.api.organization.Member
import org.solyton.solawi.bid.module.user.data.api.organization.Organization
import org.solyton.solawi.bid.module.user.data.api.organization.Organizations
import org.solyton.solawi.bid.module.user.data.api.organization.RemoveMember
import org.solyton.solawi.bid.module.user.data.api.organization.UpdateMember
import kotlin.reflect.KClass


val organizationSerializers: List<KClass<*>> by lazy {
    listOf(
        Organization::class,
        Organizations::class,
        Member::class,
        AddMember::class,
        UpdateMember::class,
        RemoveMember::class,
        ImportMembers::class,
    )
}
