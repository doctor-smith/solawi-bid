package org.solyton.solawi.bid.module.bid.component.dropdown

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.dropdown.addDropdownCloseHandler
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.map
import org.evoleq.math.x
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.transform.application.import
import org.solyton.solawi.bid.module.control.dropdown.Dropdown
import org.solyton.solawi.bid.module.modal.constants.DIALOG_LAYER_INDEX
import org.solyton.solawi.bid.module.navbar.component.SimpleUpDown
import org.solyton.solawi.bid.module.user.component.dropdown.dropdownStyles
import org.solyton.solawi.bid.module.user.component.dropdown.generateOrganizationOptions
import org.solyton.solawi.bid.module.user.component.dropdown.nameOfSelectedOrganization
import org.solyton.solawi.bid.module.user.data.organization.Organization


@Markup
@Composable
@Suppress("FunctionName")
fun OrganizationsDropdown(
    selected: Source<Organization?>,
    organizations: Source<List<Organization>>,
    isSelectable: Organization.() -> Boolean,
    select: (Organization) -> Unit
) {
    var selectedOrganization by remember(selected.emit()) { mutableStateOf<Organization?>(null) }
    LaunchedEffect(selected.emit()) {
        selectedOrganization = selected.emit()
    }
    val organizationsMap = generateOrganizationOptions(organizations.emit() )
        .filter { (_, org) -> org.isSelectable() }

    Dropdown(
        options = organizationsMap,
        selected = organizationsMap.nameOfSelectedOrganization(selectedOrganization?.organizationId?:"")?: "Select",
        closeOnSelect = true,
        styles = dropdownStyles,
        iconContent =  { open ->
            SimpleUpDown(open)
        },
    ) { (_, organization) ->
        selectedOrganization = organization
        select(organization)
    }
}

fun List<Organization>.flat(): Map<String, Organization> = map {
    it.flat()
}.fold(mapOf()){
    accumulator, element -> mapOf(
        *accumulator.entries.map { (key, value) -> key to value}.toTypedArray(),
        *element.entries.map { (key, value) -> key to value}.toTypedArray()
    )
}


fun Organization.flat(): Map<String, Organization> = mapOf (
    name to this,
    *subOrganizations.flat().entries.map { (key, value) -> "$name >> $key" to value}.toTypedArray()
)

fun List<Organization>.flatById(): Map<String, Organization> = map {
    it.flatById()
}.fold(mapOf()){
    accumulator, element -> mapOf(
        *accumulator.entries.map { (key, value) -> key to value}.toTypedArray(),
        *element.entries.map { (key, value) -> key to value}.toTypedArray()
    )
}

fun Organization.flatById(): Map<String, Organization> = mapOf (
    organizationId to this,
    *subOrganizations.flatById().entries.map { (key, value) -> organizationId to value}.toTypedArray()
)
