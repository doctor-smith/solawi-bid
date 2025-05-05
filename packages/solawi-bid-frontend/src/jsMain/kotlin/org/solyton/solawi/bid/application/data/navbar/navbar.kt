package org.solyton.solawi.bid.application.data.navbar

import org.evoleq.optics.lens.Lens
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.module.navbar.data.NavBar

val navBar = Lens<Application, NavBar>(
    {whole -> NavBar(
        whole.i18N,
        whole.environment
    ) },
    {part -> {whole -> whole.copy(i18N = part.i18n)}} // Rest is read only
)