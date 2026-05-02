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
import org.solyton.solawi.bid.module.banking.data.SepaPaymentId
import org.solyton.solawi.bid.module.banking.data.sepa.mandate.SepaMandate
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
import org.solyton.solawi.bid.module.user.data.profile.UserProfile
import org.solyton.solawi.bid.module.user.service.profile.fullname

data class SepaPaymentListData(
    val userProfile: UserProfile
)

@Markup
@Composable
@Suppress("FunctionName")
fun ListOfPayments(
    title: String? = null,
    mandates: List<SepaMandate>,
    payments: List<SepaPayment>,
    styles: ListStyles = ListStyles(),
    // overallActions: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {}
){
    val sortedPayments = payments.sortedByDescending { it.executionDate }
    val mandatesMap = payments.associateBy({it.sepaPaymentId}) { payment ->
        mandates.firstOrNull { it.sepaMandateId == payment.sepaMandateId }
    }

    ListWrapper(styles.listWrapper) {
        When(title != null) {
            TitleWrapper(styles.titleWrapper) {
                Title(styles.title) { H3 { Text(requireNotNull(title)) } }
            }
        }
        HeaderWrapper(styles.headerWrapper) {
            Header(styles.header) {
                HeaderCell("Debtor"){ width(25.percent) }
                HeaderCell("Exec. Date"){ width(15.percent) }
                HeaderCell("Amount") { width(10.percent) }
                HeaderCell("Seq Type") {width(10.percent)}
                HeaderCell("Failure Reason") {width(40.percent)}
            }
        }
        Scrollable {
            ListItemsIndexed(sortedPayments) { index, payment ->
                ListItemWrapper({
                    listItemWrapperStyle(index)
                }) {
                    val name = mandatesMap[payment.sepaPaymentId]?.debtorName?: "--"
                    DataWrapper(styles.dataWrapper) {
                        TextCell(name) { width(25.percent) }
                        TextCell(payment.executionDate.format(Locale.Iso)) { width(15.percent) }
                        TextCell("${payment.amount}") { width(10.percent) }
                        TextCell(payment.sequenceType.name) { width(10.percent) }
                        TextCell(payment.failureReason ?: "--") { width(40.percent) }
                    }
                    ActionsWrapper(styles.actionsWrapper) {
                        actions()
                    }
                }
            }
        }
    }
}
