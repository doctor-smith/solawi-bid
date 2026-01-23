package org.solyton.solawi.bid.module.application.repository

import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.application.schema.UserBundleEntity
import org.solyton.solawi.bid.module.application.schema.UserBundlesTable
import java.util.*


fun Transaction.readUserBundlesOfUser(userId: UUID): List<UserBundleEntity> = UserBundleEntity.find {
    UserBundlesTable.userId eq userId
}.toList()

fun Transaction.readUserBundleSubscriptions(bundleId: UUID): List<UserBundleEntity> = UserBundleEntity.find {
    UserBundlesTable.bundleId eq bundleId
}.toList()
