package org.solyton.solawi.bid.module.application.repository

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.application.exception.ApplicationException
import org.solyton.solawi.bid.module.application.schema.LifecycleStageEntity
import org.solyton.solawi.bid.module.application.schema.LifecycleStagesTable
import org.solyton.solawi.bid.module.application.schema.LifecycleTransitionEntity
import org.solyton.solawi.bid.module.application.schema.LifecycleTransitionsTable
import java.util.UUID

fun Transaction.createLifecycleStage(nameofLifecycle: String, descriptionOfLifecycle: String, creatorId: UUID): LifecycleStageEntity {
    val nameAvailable = LifecycleStageEntity.find{ LifecycleStagesTable.name eq nameofLifecycle }.empty()
    if(!nameAvailable) throw ApplicationException.DuplicateLifecycleStage(nameofLifecycle)

    return LifecycleStageEntity.new {
        name = nameofLifecycle.uppercase()
        description = descriptionOfLifecycle
        createdBy = creatorId
    }
}

fun Transaction.updateLifecycleStage(lifecycleStageId: UUID, nameofLifecycle: String, descriptionOfLifecycle: String, modifierId: UUID): LifecycleStageEntity {
    val lifecycleStage = LifecycleStageEntity.find{ LifecycleStagesTable.id eq lifecycleStageId }.firstOrNull()
        ?: throw ApplicationException.NoSuchLifecycleStage(lifecycleStageId.toString())

    val nameAvailable = LifecycleStageEntity.find {
        LifecycleStagesTable.id neq lifecycleStageId and
        (LifecycleStagesTable.name eq nameofLifecycle)
    }.empty()

    if(!nameAvailable) throw ApplicationException.DuplicateLifecycleStage(nameofLifecycle)

    lifecycleStage.name = nameofLifecycle
    lifecycleStage.description = descriptionOfLifecycle
    lifecycleStage.modifiedBy = modifierId
    lifecycleStage.modifiedAt = DateTime.now()

    return lifecycleStage
}

fun Transaction.transitionTargetOf(lifecycleStageId: UUID, to: UUID): LifecycleStageEntity {
    LifecycleTransitionEntity.find{
        (LifecycleTransitionsTable.fromId eq lifecycleStageId) and
        (LifecycleTransitionsTable.toId eq to)
    }.firstOrNull()?: throw ApplicationException.ForbiddenLifecycleTransition(lifecycleStageId.toString(), to.toString())

    LifecycleStageEntity.find { LifecycleStagesTable.id eq lifecycleStageId }.firstOrNull()
        ?: throw ApplicationException.NoSuchLifecycleStage(lifecycleStageId.toString())

    return LifecycleStageEntity.find { LifecycleStagesTable.id eq to }.firstOrNull()
        ?: throw ApplicationException.NoSuchLifecycleStage(to.toString())

}

fun Transaction.createLifecycleTransition(
    fromId: UUID,
    toId: UUID,
    descriptionOfTransition: String ,
    creatorId: UUID
): LifecycleTransitionEntity {
    val fromStage = LifecycleStageEntity.find { LifecycleStagesTable.id eq fromId }.firstOrNull()
        ?: throw ApplicationException.NoSuchLifecycleStage(fromId.toString())

    val toStage = LifecycleStageEntity.find { LifecycleStagesTable.id eq toId }.firstOrNull()
        ?: throw ApplicationException.NoSuchLifecycleStage(toId.toString())

    val transitionAvailable = LifecycleTransitionEntity.find{
        (LifecycleTransitionsTable.fromId eq fromId) and (LifecycleTransitionsTable.toId eq toId)
    }.empty()

    if(!transitionAvailable) throw ApplicationException.DuplicateLifecycleTransition(fromId.toString(), toId.toString())

    return LifecycleTransitionEntity.new {
        from = fromStage
        to = toStage
        description = descriptionOfTransition
        createdBy = creatorId
    }
}

@Suppress("UNUSED_PARAMETER")
fun Transaction.updateLifecycleTransaction(fromId: UUID, toId: UUID, descriptionOfTransition: String , creatorId: UUID): LifecycleTransitionEntity = TODO("Dev")
