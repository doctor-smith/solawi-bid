package org.solyton.solawi.bid.module.db

import org.solyton.solawi.bid.module.db.migrations.Config
import org.jetbrains.exposed.sql.Database

fun connectToDB(): Database = with(Config.DB) {
    Database.connect(
        url = url,
        driver = driver,
        user = user,
        password = password
    )
}