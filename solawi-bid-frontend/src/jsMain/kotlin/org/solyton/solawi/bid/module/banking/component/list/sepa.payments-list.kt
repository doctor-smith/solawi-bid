package org.solyton.solawi.bid.module.banking.component.list

import androidx.compose.runtime.Composable
import kotlinx.datetime.format
import org.evoleq.compose.Markup
import org.evoleq.compose.conditional.When
import org.evoleq.compose.date.format
import org.evoleq.language.Locale
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.ui.page.user.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.banking.data.sepa.payment.SepaPayment
import org.solyton.solawi.bid.module.list.component.ActionsWrapper
import org.solyton.solawi.bid.module.list.component.DataWrapper
import org.solyton.solawi.bid.module.list.component.Header
import org.solyton.solawi.bid.module.list.component.HeaderCell
import org.solyton.solawi.bid.module.list.component.HeaderWrapper
import org.solyton.solawi.bid.module.list.component.ListItemWrapper
import org.solyton.solawi.bid.module.list.component.ListItemsIndexed
import org.solyton.solawi.bid.module.list.component.ListWrapper
import org.solyton.solawi.bid.module.list.component.TextCell
import org.solyton.solawi.bid.module.list.component.Title
import org.solyton.solawi.bid.module.list.component.TitleWrapper
import org.solyton.solawi.bid.module.list.style.ListStyles
import org.solyton.solawi.bid.module.scrollable.Scrollable

@Markup
@Composable
@Suppress("FunctionName")
fun ListOfPayments(
    title: String? = null,
    payments: List<SepaPayment>,
    styles: ListStyles = ListStyles(),
    // overallActions: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {}
){
    val sortedPayments = payments.sortedByDescending { it.executionDate }

    ListWrapper(styles.listWrapper) {
        When(title != null) {
            TitleWrapper(styles.titleWrapper) {
                Title(styles.title) { H3 { Text(requireNotNull(title)) } }
            }
        }
        HeaderWrapper(styles.headerWrapper) {
            Header(styles.header) {
                HeaderCell("Execution Date"){ width(20.percent) }
                HeaderCell("Amount") { width(10.percent) }
                HeaderCell("Sequence Type") {width(20.percent)}
                HeaderCell("Failure Reason") {width(50.percent)}
            }
        }
        Scrollable {
            ListItemsIndexed(sortedPayments) { index, payment ->
                ListItemWrapper({
                    listItemWrapperStyle(index)
                }) {
                    DataWrapper(styles.dataWrapper) {
                        TextCell(payment.executionDate.format(Locale.Iso)) { width(20.percent) }
                        TextCell("${payment.amount}") { width(10.percent) }
                        TextCell(payment.sequenceType.name) { width(20.percent) }
                        TextCell(payment.failureReason ?: "--") { width(50.percent) }
                    }
                    ActionsWrapper(styles.actionsWrapper) {
                        actions()
                    }
                }
            }
        }
    }
}
