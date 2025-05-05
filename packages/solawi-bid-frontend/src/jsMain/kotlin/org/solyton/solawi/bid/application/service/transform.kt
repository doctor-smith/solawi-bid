package org.solyton.solawi.bid.application.service

import org.solyton.solawi.bid.application.data.env.Environment
import org.solyton.solawi.bid.module.i18n.data.I18nResources
import org.solyton.solawi.bid.module.i18n.data.Environment as I18nEnv

fun Environment.useI18nTransform(): I18nEnv = I18nEnv(
    i18nResources = I18nResources(
        url = "$frontendUrl/i18n",
        port = frontendPort
    )
)