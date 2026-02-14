package org.solyton.solawi.bid.module.banking.service

import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.user.exception.UserManagementException
import org.solyton.solawi.bid.module.user.schema.UserEntity
import java.util.*


fun Transaction.validateUserExists(userId: UUID) =
    UserEntity.findById(userId) ?: throw UserManagementException.UserDoesNotExist.Id(userId.toString())
