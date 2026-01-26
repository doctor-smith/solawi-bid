package org.solyton.solawi.bid.module.system.repository

import SystemProcessException
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.deleteWhere
import org.solyton.solawi.bid.module.system.schema.SystemProcessEntity
import org.solyton.solawi.bid.module.system.schema.SystemProcessesTable
import java.util.UUID

fun Transaction.createSystemProcess(
    name: String,
    description: String
): SystemProcessEntity {
    validateProcessName(name)
    return SystemProcessEntity.new {
        this.name = name
        this.description = description
    }
}

fun Transaction.validateProcessName(name: String) {
    val exists = !SystemProcessEntity.find {
        SystemProcessesTable.name eq name
    }.empty()
    if(exists) throw SystemProcessException.DuplicateProcessName(name)
}

fun Transaction.validatedSystemProcess(name: String): SystemProcessEntity = SystemProcessEntity.find {
    SystemProcessesTable.name eq name
}.firstOrNull()?: throw SystemProcessException.NoSuchProcess(name)

fun Transaction.validatedSystemProcess(id: UUID): SystemProcessEntity = SystemProcessEntity.find {
    SystemProcessesTable.id eq id
}.firstOrNull()?: throw SystemProcessException.NoSuchProcess(id.toString())

fun Transaction.updateSystemProcess(
    id: UUID,
    name: String,
    description: String
): SystemProcessEntity {
    validateProcessName(name)
    val systemProcess = validatedSystemProcess(id)
    systemProcess.name = name
    systemProcess.description = description
    return systemProcess
}

fun Transaction.deleteSystemProcess(name: String) {
    SystemProcessesTable.deleteWhere {
        SystemProcessesTable.name eq name
    }
}
