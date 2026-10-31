package org.solyton.solawi.bid.application.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.BuildInfo
import org.solyton.solawi.bid.application.data.ReadBackendBuildInfo
import org.solyton.solawi.bid.application.data.version.VersionInfo
import org.solyton.solawi.bid.application.data.version.full.backend
import org.solyton.solawi.bid.application.data.versionInfo

const val READ_BACKEND_VERSION_INFO = "READ_BACKEND_VERSION_INFO"

fun readBackendVersionInfo(): Action<Application, ReadBackendBuildInfo, BuildInfo> = Action(
    name = READ_BACKEND_VERSION_INFO,
    reader = { ReadBackendBuildInfo },
    endPoint = ReadBackendBuildInfo::class,
    writer = versionInfo * backend.set contraMap { info ->
        VersionInfo(
            commit = info.commit,
            version = info.version
        )
    }
)
