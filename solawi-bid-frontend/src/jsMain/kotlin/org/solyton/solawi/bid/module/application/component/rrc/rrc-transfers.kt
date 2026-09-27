package org.solyton.solawi.bid.module.application.component.rrc

import androidx.compose.runtime.*
import org.evoleq.compose.conditional.When
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.layout.Vertical
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.math.Source
import org.evoleq.math.contains
import org.evoleq.math.emit
import org.evoleq.math.map
import org.evoleq.optics.lens.DeepSearch
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.storage.Read
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.add
import org.evoleq.optics.storage.remove
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.RadioInput
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.ui.page.user.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.application.data.management.ApplicationManagement
import org.solyton.solawi.bid.module.application.data.management.availablePermissions
import org.solyton.solawi.bid.module.control.button.AnglesLeftButton
import org.solyton.solawi.bid.module.control.button.AnglesRightButton
import org.solyton.solawi.bid.module.control.button.TrashCanButton
import org.solyton.solawi.bid.module.control.dropdown.Dropdown
import org.solyton.solawi.bid.module.control.dropdown.DropdownStyles
import org.solyton.solawi.bid.module.control.dropdown.SimpleUpDown
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.list.style.ListStyles
import org.solyton.solawi.bid.module.permission.data.ContextId
import org.solyton.solawi.bid.module.permission.data.RightId
import org.solyton.solawi.bid.module.permissions.data.*
import org.solyton.solawi.bid.module.scrollable.Scrollable
import org.solyton.solawi.bid.module.scrollable.ScrollableStyles


sealed class TransferMode {
    /**
     * Move rights from source to target role or vice versa
     */
    data object Move : TransferMode()

    /**
     * Copy rights from source to target role or vice versa
     */
    data object Copy : TransferMode()

    /**
     * Remove selected rights
     * Should only be possible if there is at least on Role providing the right which is to be removed
     */
    data object Delete : TransferMode()
}

data class RoleRightTransferData(
    val transferMode: TransferMode = TransferMode.Move,
    val sourceRole: Role? = null,
    val targetRole: Role? = null,
    val checkedSourceRights: Set<RightId> = emptySet(),
    val checkedTargetRights: Set<RightId> = emptySet(),
    // val nonRemovableRights: Set<RightId> = emptySet()
)

data class RoleRightTransferStyles(
    val containerStyles: StyleScope.()->Unit = {
        width(100.percent)
        height(80.vh)
    },
    val dropdownStyles: DropdownStyles = DropdownStyles(),
    val scrollableStyles: ScrollableStyles = ScrollableStyles().modifyContainerStyle {
        height(60.vh)
    },
    val listStyles: ListStyles = ListStyles(),
)

@Composable
@Suppress("FunctionName")
fun RoleRightTransfer(

    storage: Storage<ApplicationManagement>,
    contextId: ContextId,
    styles: RoleRightTransferStyles,
    deviceType: Source<DeviceType>,
    data: RoleRightTransferData,
    setData: (RoleRightTransferData) -> Unit
) {
    val context = storage * availablePermissions * contexts * DeepSearch { it.contextId == contextId.value }
    val roles = context * roles
    val allRights = Read(roles) map { roles -> roles.flatMap { it.rights }.distinctBy { right -> right.rightName } }
    
    val removableRights = Read(roles) map { roles ->
        roles.flatMap { it.rights }
            .groupBy { it.rightId }
            .filter { it.value.size >= 2 }
            .mapValues { RightId(it.value.first().rightId) }
            .values.toList()
    }

    var sourceRoleState by remember { mutableStateOf(data.sourceRole
        ?: (Read(roles) map { it.first() }).emit())
    }
    var targetRoleState by remember { mutableStateOf(data.targetRole ?: (Read(roles) map {
        it.filterNot{role -> role.roleId == sourceRoleState.roleId }.first()
    }).emit()) }

    if (roles.read().size <= 1) {
        Text("There is only 1 role ore less in the given context")
    }

    val sourceRights = roles * FirstBy { it.roleId == sourceRoleState.roleId } * rights
    val targetRights = roles * FirstBy { it.roleId == targetRoleState.roleId } * rights

    val addableSourceRights = allRights map { list -> list.filterNot { it.rightId in Read(sourceRights) map { rights -> rights.map{ r -> r.rightId} } } }
    val addableTargetRights = allRights map { list -> list.filterNot { it.rightId in Read(targetRights) map { rights -> rights.map{ r -> r.rightId} } } }

    LaunchedEffect(sourceRoleState.roleId, targetRoleState.roleId) {
        if(sourceRoleState.roleId == targetRoleState.roleId ) {
            targetRoleState = (Read(roles) map {
                it.filterNot{role -> role.roleId == sourceRoleState.roleId }.first()
            }).emit()
            return@LaunchedEffect
        }
        setData(data.copy(
            sourceRole = sourceRoleState,
            targetRole = targetRoleState,
            checkedSourceRights = emptySet(),
            checkedTargetRights = emptySet())
        )
    }

    val dropdownStyles = styles.dropdownStyles
    val listStyles = styles.listStyles
    val scrollableStyles = styles.scrollableStyles

    Vertical(styles.containerStyles) {
        // Panels to select the source and target roles
        Horizontal({
            width(100.percent)
        }) {
            Vertical({ width(50.percent) }) {
                Horizontal({
                    paddingRight(10.px)
                    justifyContent(JustifyContent.SpaceBetween)
                }) {
                    val sourceRoleOptions = Read(roles) map {
                            roles -> roles.associateBy ({ it.roleName }) {it}
                    }
                    Dropdown(
                        sourceRoleOptions.emit(),
                        sourceRoleState.roleName,
                        true,
                        dropdownStyles,
                        { opened -> SimpleUpDown(opened) }
                    ) {
                        sourceRoleState = it.value
                        setData(data.copy(sourceRole = it.value))
                    }
                    val addableRightOptions = addableSourceRights.map {
                        list -> list.associateBy ({ it.rightName }) {it}
                    }.emit()
                    Dropdown(
                        addableRightOptions,
                        null,
                        true,
                        dropdownStyles
                    ) {
                        (_, value) -> sourceRights.add(value)
                    }
                }

                val checkableRights = Read(sourceRights).map{ rights ->
                    rights.associateBy({ it }) { it.rightId in data.checkedSourceRights.map { it.value } }
                } map { CheckableRights(it){ right -> RightId(right.rightId) in removableRights } }

                Scrollable(scrollableStyles) {
                    ListOfCheckableRights(
                        listStyles,
                        deviceType,
                        checkableRights.emit(),
                        {rightId -> sourceRights.remove { it.rightId == rightId.value }}
                    ) {
                        setData(data.copy(checkedSourceRights = it.filterValues { jt -> jt }.keys.map { key ->
                            RightId(
                                key.rightId
                            )
                        }.toSet()))
                    }
                }
                When(data.transferMode in listOf(TransferMode.Move, TransferMode.Copy)) {
                    Horizontal({
                        width(100.percent)
                        justifyContent(JustifyContent.FlexEnd)
                    }){
                        AnglesRightButton(
                            color = Color.black,
                            bgColor = Color.white,
                            texts = { "Move or Copy rights to the target role" },
                            deviceType = deviceType,
                            isDisabled = false,
                            dataId = null,
                        ) {
                            roles.write( roles.read().exchangeRights(data, ExchangeDirection.Right) )
                            setData(data.copy(checkedSourceRights = emptySet()))
                        }
                    }
                }
            }

            Vertical({ width(50.percent) }) {
                Horizontal({
                    paddingRight(10.px)
                    justifyContent(JustifyContent.SpaceBetween)
                }) {
                    val targetRoleOptions = Read(roles) map { roles ->
                        roles.filterNot { it.roleId == sourceRoleState.roleId }.associateBy({ it.roleName }) { it }
                    }
                    Dropdown(
                        targetRoleOptions.emit(),
                        targetRoleState.roleName,
                        true,
                        dropdownStyles,
                        { opened -> SimpleUpDown(opened) }
                    ) {
                        targetRoleState = it.value
                        setData(data.copy(targetRole = it.value))
                    }

                    val addableRightOptions = addableTargetRights.map {
                            list -> list.associateBy ({ it.rightName }) {it}
                    }.emit()
                    Dropdown(
                        addableRightOptions,
                        null,
                        true,
                        dropdownStyles
                    ) {
                            (_, value) -> targetRights.add(value)
                    }
                }

                val checkableRights = Read(targetRights).map{ rights ->
                    rights.associateBy({ it }) { it.rightId in data.checkedTargetRights.map { it.value } }
                } map { CheckableRights(it){ right -> RightId(right.rightId) in removableRights } }

                Scrollable(scrollableStyles) {
                    ListOfCheckableRights(
                        listStyles,
                        deviceType,
                        checkableRights.emit(),
                        {rightId -> targetRights.remove { it.rightId == rightId.value }}
                    ) {
                        setData(data.copy(checkedTargetRights = it.filterValues { jt -> jt }.keys.map { key ->
                            RightId(
                                key.rightId
                            )
                        }.toSet()))
                    }
                }

                When(data.transferMode in listOf(TransferMode.Move, TransferMode.Copy)) {
                    Horizontal({
                        width(100.percent)
                    }){
                        AnglesLeftButton(
                            color = Color.black,
                            bgColor = Color.white,
                            texts = { "Move or Copy rights to the source role" },
                            deviceType = deviceType,
                            isDisabled = false,
                            dataId = null,
                        ) {
                            roles.write( roles.read().exchangeRights(data, ExchangeDirection.Left) )
                            setData(data.copy(checkedTargetRights = emptySet()))
                        }
                    }
                }
            }
        }
        Horizontal({}) {
            // Radio buttons
            Vertical({
                width(50.percent)
            }) {
                Horizontal({
                    padding(10.px)
                }) {
                    RadioInput(data.transferMode is TransferMode.Move) {
                        onChange {
                            setData(data.copy(transferMode = TransferMode.Move))
                        }
                    }
                    Text("Move Rights")
                }
                Horizontal({
                    padding(10.px)
                }) {
                    RadioInput(data.transferMode is TransferMode.Copy) {
                        onChange {
                            setData(data.copy(transferMode = TransferMode.Copy))
                        }
                    }
                    Text("Copy Rights")
                }
                /*
                Horizontal({
                    padding(10.px)
                }) {
                    RadioInput(data.transferMode is TransferMode.Delete) {
                        onChange {
                            setData(data.copy(transferMode = TransferMode.Delete))
                        }
                    }
                    Text("Delete Rights")
                }

                 */
            }

            // Actions

        }
    }
}

data class CheckableRights(
    val all: Map<Right, Boolean>,
    val isRemovable: (Right) -> Boolean
)

@Composable
@Suppress("FunctionName")
fun ListOfCheckableRights(
    styles: ListStyles = ListStyles(),
    deviceType: Source<DeviceType>,
    checkableRights: CheckableRights,
    deleteRight: (RightId) -> Unit,
    checkRights: (Map<Right, Boolean>) -> Unit
) {

    ListWrapper(styles.listWrapper) {
        ListItemsIndexed(checkableRights.all.entries.toList()) { index, entry ->
            val right = entry.key
            ListItemWrapper({listItemWrapperStyle(index)}) {
                key(entry.key.rightId) {
                    DataWrapper(styles.dataWrapper) {
                        CheckBoxCell({ entry.value }, { width(2.percent) }) {
                            val checked = checkableRights.all.toMutableMap()
                            checked[entry.key] = !checked[entry.key]!!
                            checkRights(checked)
                        }
                        TextCell(entry.key.rightName) { width(98.percent) }
                    }
                    ActionsWrapper {
                        fun Right.isRemovable() = checkableRights.isRemovable(this)
                        When(right.isRemovable()) {
                            TrashCanButton(
                                color = Color.black,
                                bgColor = Color.white,
                                texts = {"Delete Right"},
                                deviceType = deviceType
                            ) {
                                deleteRight(RightId(right.rightId))
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed class ExchangeDirection {
    // Source to target
    data object Left : ExchangeDirection()
    // Target to source
    data object Right : ExchangeDirection()
    // Both
    data object Both : ExchangeDirection()
}

fun List<Role>.exchangeRights(data: RoleRightTransferData, direction: ExchangeDirection): List<Role> = when(data.transferMode) {
    is TransferMode.Delete -> this
    is TransferMode.Copy -> {
        val sourceRoleId = requireNotNull(data.sourceRole){ "rrc transfer / exchange rights: source role is null" }.roleId
        val targetRoleId  = requireNotNull(data.targetRole){ "rrc transfer / exchange rights: target role is null" }.roleId

        val sourceRole = requireNotNull(this.firstOrNull { it.roleId == sourceRoleId }) { "rrc transfer / exchange rights: source role not found" }
        val targetRole = requireNotNull(this.firstOrNull { it.roleId == targetRoleId }) { "rrc transfer / exchange rights: target role not found" }

        val newTargetRights = sourceRole.rights.filter { RightId(it.rightId) in data.checkedSourceRights /* && it.rightId !in targetRole.rights.map { it.rightId } */ }
        val newSourceRights = targetRole.rights.filter { RightId(it.rightId) in data.checkedTargetRights /* && it.rightId !in sourceRole.rights.map { it.rightId } */ }

        val sourceRightIds = sourceRole.rights.map { it.rightId }
        val targetRightIds = targetRole.rights.map { it.rightId }



        val newSourceRole = when(direction){
            is ExchangeDirection.Right -> sourceRole
            is ExchangeDirection.Left,
            is ExchangeDirection.Both -> sourceRole.copy(rights = sourceRole.rights + newSourceRights.filter { it.rightId !in sourceRightIds })

        }
        val newTargetRole = when(direction) {
            is ExchangeDirection.Left -> targetRole
            is ExchangeDirection.Right,
            is ExchangeDirection.Both -> targetRole.copy(rights = targetRole.rights + newTargetRights.filter { it.rightId !in targetRightIds })
        }

        val newRoles = this.filter { it.roleId != sourceRole.roleId && it.roleId != targetRole.roleId } + newSourceRole + newTargetRole

        newRoles
    }
    is TransferMode.Move -> {
        val sourceRoleId = requireNotNull(data.sourceRole){ "rrc transfer / exchange rights: source role is null" }.roleId
        val targetRoleId = requireNotNull(data.targetRole){ "rrc transfer / exchange rights: target role is null" }.roleId

        val sourceRole = requireNotNull(this.firstOrNull { it.roleId == sourceRoleId }) { "rrc transfer / exchange rights: source role not found" }
        val targetRole = requireNotNull(this.firstOrNull { it.roleId == targetRoleId }) { "rrc transfer / exchange rights: target role not found" }

        val newTargetRights = sourceRole.rights.filter { it.rightId in data.checkedSourceRights.map{r -> r.value} }
        val newSourceRights = targetRole.rights.filter { it.rightId in data.checkedTargetRights.map{r -> r.value} }

        val sourceRightIds = sourceRole.rights.map { it.rightId }
        val targetRightIds = targetRole.rights.map { it.rightId }

        val newSourceRole = when(direction){
            ExchangeDirection.Both -> sourceRole.copy(
                rights = sourceRole.rights + newSourceRights.filter { it.rightId !in sourceRightIds } - newTargetRights.toSet()
            )
            ExchangeDirection.Left -> sourceRole.copy(rights = sourceRole.rights + newSourceRights.filter { it.rightId !in sourceRightIds })
            ExchangeDirection.Right -> sourceRole.copy(rights = sourceRole.rights - newTargetRights.toSet() )
        }
        val newTargetRole = when(direction) {
            ExchangeDirection.Both -> targetRole.copy(rights = targetRole.rights + newTargetRights.filter { it.rightId !in targetRightIds } - newSourceRights.toSet() )
            ExchangeDirection.Left -> targetRole.copy(rights = targetRole.rights - newSourceRights.toSet())
            ExchangeDirection.Right -> targetRole.copy(
                rights = targetRole.rights + newTargetRights.filter { it.rightId !in targetRightIds }
            )
        }

        val newRoles = listOf(
            *this.filter { it.roleId != sourceRole.roleId && it.roleId != targetRole.roleId }.toTypedArray(),
            newSourceRole,
            newTargetRole
        )

        newRoles
    }
}
