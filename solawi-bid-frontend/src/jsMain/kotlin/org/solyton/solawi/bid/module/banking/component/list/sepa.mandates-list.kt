package org.solyton.solawi.bid.module.banking.component.list

import androidx.compose.runtime.*
import kotlinx.datetime.LocalDateTime
import org.evoleq.compose.Markup
import org.evoleq.compose.conditional.When
import org.evoleq.compose.date.format
import org.evoleq.language.Locale
import org.jetbrains.compose.web.css.paddingLeft
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.ui.page.user.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.banking.data.SepaMandateId
import org.solyton.solawi.bid.module.banking.data.sepa.MandateStatus
import org.solyton.solawi.bid.module.banking.data.sepa.mandate.SepaMandate
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.list.style.ListStyles
import org.solyton.solawi.bid.module.scrollable.Scrollable


data class OverAllMandatesActionData(
    val visibleItems: List<SepaMandateId>,
    val checkedItems: Map<SepaMandateId, Boolean> = emptyMap()
)

data class SepaMandatesFilter(
    val debtor: (String) -> Boolean = { true },
    val status: (MandateStatus) -> Boolean = { true },
    val signedAt: (LocalDateTime) -> Boolean = { true },
    val mandateReference: (String) -> Boolean = { true },
) {
    infix fun applyTo(mandate: SepaMandate): Boolean = with(mandate) {
        debtor(debtorName) &&
        status(status) &&
        signedAt(signedAt) &&
        mandateReference(mandateReference.value)
    }
}

data class SepaMandatesOrder(
    val debtor: SortOrder = SortOrder.ASC,
    val signedAt: SortOrder = SortOrder.DESC,
    val status: SortOrder = SortOrder.NONE,
    val mandateReference: SortOrder = SortOrder.ASC,
) {
    fun toComparator(): Comparator<SepaMandate> = Comparator { o1, o2 ->
        var result = 0
        if (result == 0 && debtor != SortOrder.NONE) {
            result = compareValues(o1.debtorName, o2.debtorName)
            if (debtor == SortOrder.DESC) result = -result
        }
        if (result == 0 && signedAt != SortOrder.NONE) {
            result = compareValues(o1.signedAt, o2.signedAt)
            if (signedAt == SortOrder.DESC) result = -result
        }
        if (result == 0 && status != SortOrder.NONE) {
            result = compareValues(o1.status.name, o2.status.name)
            if (status == SortOrder.DESC) result = -result
        }
        if (result == 0 && mandateReference != SortOrder.NONE) {
            result = compareValues(o1.mandateReference.value, o2.mandateReference.value)
            if (mandateReference == SortOrder.DESC) result = -result
        }
        result
    }
}
fun List<SepaMandate>.sortedBy(order: SepaMandatesOrder): List<SepaMandate> {
    return sortedWith(order.toComparator())
}

@Markup
@Composable
@Suppress("FunctionName")
fun ListOfMandateWithoutPayments(
    title: String? = null,
    mandates: List<SepaMandate>,
    styles: ListStyles = ListStyles(),
    displayIfEmpty: Boolean = true,
    overallActions: @Composable (data: OverAllMandatesActionData) -> Unit = {},
    actions: @Composable () -> Unit = {}
) {
    When(displayIfEmpty || mandates.isNotEmpty()) {
        val listData = mandates


        // stores the filtered list of payments, based on the filter state
        var filteredList by remember { mutableStateOf(listData) }
        // stores the filter state
        var filter by remember { mutableStateOf(SepaMandatesFilter()) }
        // stores the sort order state
        var sortOrder by remember { mutableStateOf(SepaMandatesOrder()) }
        // stores checked state of each item in the list of payments, regardless of the filter state
        val checkedMap = rememberMutableStateMapOf<SepaMandateId, Boolean>()
        // data to be passed to the overall actions component
        val overallActionsData by remember {
            derivedStateOf {
                OverAllMandatesActionData(
                    visibleItems = filteredList.map {
                        it.sepaMandateId
                    },
                    checkedItems = checkedMap.toMap()
                )
            }
        }

        LaunchedEffect(filter, listData, sortOrder) {
            filteredList = listData.filter {
                filter applyTo it
            }.sortedBy(sortOrder)
        }

        ListWrapper(styles.listWrapper) {
            When(title != null) {
                TitleWrapper(styles.titleWrapper) {
                    Title(styles.title) { H3 { Text(requireNotNull(title)) } }
                }
            }
            OverallActionsWrapper(styles.overallActionsWrapper) {
                OverallActions(styles.overallActions) {
                    key(overallActionsData) {
                        overallActions(overallActionsData)
                    }
                }
            }

            FilterWrapper(styles.modifyFilterWrapper { width(80.percent) }.filterWrapper) {

                Filter(styles.modifyFilter {
                    width(20.percent)
                    paddingLeft(20.px)
                }.filter) {
                    TextFilter("Filter by Status") {
                        filter = when {
                            it.isBlank() -> filter.copy(status = { true })
                            else -> filter.copy(status = { status -> it.toFilter().applyTo(status.name) })
                        }
                    }
                }

                Filter(styles.modifyFilter {
                    width(25.percent)
                    //paddingLeft(20.px)
                }.filter) {
                    TextFilter("Filter by Debtor") {
                        filter = when {
                            it.isBlank() -> filter.copy(debtor = { true })
                            else -> filter.copy(debtor = { debtor -> it.toFilter().applyTo(debtor) })
                        }
                    }
                }
            }

            HeaderWrapper(styles.headerWrapper) {
                Header(styles.header) {
                    val allChecked =
                        filteredList.isNotEmpty() && filteredList.all { checkedMap[it.sepaMandateId] == true }
                    key(allChecked) {
                        CheckBoxCell({ allChecked }, { width(2.percent) }) {
                            val newCheckedState = !allChecked
                            if (newCheckedState) {
                                filteredList.forEach {
                                    checkedMap[it.sepaMandateId] = true
                                }
                            } else {
                                filteredList.forEach {
                                    checkedMap.remove(it.sepaMandateId)
                                }
                            }
                        }
                    }

                    HeaderCellWithActions(
                        text = { "Status" },
                        styles = HeaderCellStyles().width(10.percent),
                        ordering = { SortByDrop { order: SortOrder -> sortOrder = sortOrder.copy(status = order) } }
                    )
                    HeaderCellWithActions(
                        text = { "Debtor" },
                        styles = HeaderCellStyles().width(25.percent),
                        ordering = { SortByDrop { order: SortOrder -> sortOrder = sortOrder.copy(debtor = order) } }
                    )
                    HeaderCellWithActions(
                        text = { "Signed At" },
                        styles = HeaderCellStyles().width(15.percent),
                        ordering = { SortByDrop { order: SortOrder -> sortOrder = sortOrder.copy(signedAt = order) } }
                    )
                    HeaderCellWithActions(
                        text = { "Mandate Reference" },
                        styles = HeaderCellStyles().width(20.percent),
                        ordering = { SortByDrop { order: SortOrder -> sortOrder = sortOrder.copy(mandateReference = order) } }
                    )
                }
            }
            Scrollable {
                ListItemsIndexed(filteredList) { index, listItem ->
                    val isChecked = checkedMap[listItem.sepaMandateId] == true
                    key(listItem.sepaMandateId, isChecked) {
                        ListItemWrapper({
                            listItemWrapperStyle(index)
                        }) {
                            DataWrapper(styles.dataWrapper) {
                                CheckBoxCell({ isChecked }, { width(2.percent) }) {
                                    if (isChecked) {
                                        checkedMap.remove(listItem.sepaMandateId)
                                    } else {
                                        checkedMap[listItem.sepaMandateId] = true
                                    }
                                }
                                TextCell(listItem.status.name) { width(10.percent) }
                                TextCell(listItem.debtorName) { width(25.percent) }
                                TextCell(listItem.signedAt.format(Locale.Iso)) { width(15.percent) }
                                TextCell(listItem.mandateReference.value) { width(20.percent) }
                                /*
                            TextCell(listItem.payment.executionDate.format(Locale.Iso)) { width(15.percent) }
                            TextCell("${listItem.payment.amount}") { width(10.percent) }
                            TextCell(listItem.payment.sequenceType.name) { width(10.percent) }
                            TextCell(
                                text = listItem.payment.failureReason ?: "--",
                                tooltip = listItem.payment.failureReason
                            ) {
                                minWidth(0.px)
                                width(20.percent)

                                flexGrow(0)
                                flexShrink(0)
                                whiteSpace(WhiteSpace.NoWrap)
                                overflow(Overflow.Hidden)
                                textOverflow(TextOverflow.Ellipsis)
                            }

                             */
                            }
                        }
                        ActionsWrapper(styles.actionsWrapper) {
                            actions()
                        }
                    }
                }
            }
        }
    }
}
