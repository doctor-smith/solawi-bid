package org.solyton.solawi.bid.module.db

import org.jetbrains.exposed.sql.Database
import org.solyton.solawi.bid.module.db.migrations.Config

fun connectToDB(): Database = with(Config.DB) {
    Database.connect(
        url = url,
        driver = driver,
        user = user,
        password = password
    )
}
