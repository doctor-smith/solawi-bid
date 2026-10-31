package org.solyton.solawi.bid.application.data.version.full

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.application.data.version.VersionInfo


@Lensify
data class FullVersionInfo(
    @ReadWrite val backend: VersionInfo = VersionInfo(),
    @ReadWrite val frontend: VersionInfo = VersionInfo()
)
