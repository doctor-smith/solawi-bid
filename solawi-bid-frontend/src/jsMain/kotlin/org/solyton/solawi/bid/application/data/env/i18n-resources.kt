package org.solyton.solawi.bid.application.data.env

import org.evoleq.math.Reader
import org.evoleq.optics.lens.Lens
import org.solyton.solawi.bid.module.i18n.data.I18nResources
import org.solyton.solawi.bid.module.i18n.data.Environment as I18nEnv

val i18nEnvironment: Reader<Environment, I18nEnv> by lazy {
    Reader { environment -> I18nEnv(I18nResources(
        url = environment.frontendUrl + "/i18n",
        port = environment.frontendPort
    ))}
}

val i18nEnvironmentLens: Lens<Environment, I18nEnv> by lazy {
    Lens(
        get = { environment -> I18nEnv(I18nResources(
            url = environment.frontendUrl + "/i18n",
            port = environment.frontendPort
        ))},
        set = {{environment -> environment}}
    )
}
