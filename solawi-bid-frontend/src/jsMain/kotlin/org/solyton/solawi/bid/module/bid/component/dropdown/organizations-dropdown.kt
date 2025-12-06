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
import org.solyton.solawi.bid.module.modal.constants.DIALOG_LAYER_INDEX
import org.solyton.solawi.bid.module.navbar.component.SimpleUpDown
import org.solyton.solawi.bid.module.user.data.organization.Organization


@Markup
@Composable
@Suppress("FunctionName")
fun OrganizationsDropdown(
    layerIndex: Int = DIALOG_LAYER_INDEX + 1,
    selected: Source<Organization?>,
    organizations: Source<List<Organization>>,
    scope: CoroutineScope,
    select: (Organization) -> Unit
) {
    var open by remember { mutableStateOf(false) }

    val selectedText: Source<String> = selected x organizations map {
        val (selected, organizations) = it
        when {
            organizations.isEmpty() -> "You need to connect the auction app to at least one organization"
            else -> selected?.name?: "Click to choose an organizations"
        }
    }

    // Dropdown Container
    Div(attrs = {
        style {
            position(Position.Relative)
            cursor("pointer")
        }
        onClick { open = !open }
    }) {
        // Display current value
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.FlexStart)
                backgroundColor(Color.white)
                border(1.px, LineStyle.Solid, Color.black)
                padding(4.px)
            }
        }) {
            SimpleUpDown(open)
            // organization name
            Text(selectedText.emit())
        }

        // Dropdown-List
        if (open && organizations.emit().isNotEmpty()) {
            addDropdownCloseHandler {
                open = false
            }

            Div(attrs = {
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    position(Position.Absolute)
                    top(100.percent)
                    left(0.px)
                    width(100.percent)
                    backgroundColor(Color.white)
                    border(1.px, LineStyle.Solid, Color.black)
                    borderRadius(4.px)
                    property("z-index", layerIndex)
                }
            }) {
                organizations.emit().forEach { org ->
                    Div(attrs = {
                        style {
                            display(DisplayStyle.Flex)
                            // flexGrow(1)
                            minHeight(20.px)
                            alignItems(AlignItems.FlexStart)
                            padding(4.px)
                        }
                        onClick { event ->
                            scope.launch{
                                select(org)
                            }
                        }
                    }) {
                        Text(org.name)
                    }
                }
            }
        }
    }
}
