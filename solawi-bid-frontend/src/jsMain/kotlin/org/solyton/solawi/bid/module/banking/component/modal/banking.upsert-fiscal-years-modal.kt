package org.solyton.solawi.bid.module.banking.component.modal

import org.evoleq.compose.date.format
import org.evoleq.compose.date.parse
import org.evoleq.kotlinx.date.toDateTime
import org.evoleq.language.Locale
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.dom.Input
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.deviceData
import org.solyton.solawi.bid.module.style.form.dateInputDesktopStyle
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.solyton.solawi.bid.module.banking.data.fiscalyear.FiscalYear
import org.solyton.solawi.bid.module.control.dropdown.DropdownStyles
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.evoleq.compose.Markup
import org.evoleq.compose.attribute.dataId
import org.evoleq.compose.form.field.Field
import org.evoleq.compose.form.label.Label
import org.evoleq.compose.modal.Modal
import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.modal.Modals
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.device.data.mediaType
import org.evoleq.kotlinx.date.data.DateInterval
import org.evoleq.kotlinx.date.data.cutOff
import org.evoleq.kotlinx.date.today
import org.evoleq.language.Lang
import org.evoleq.math.Source
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.Form
import org.jetbrains.compose.web.dom.H4
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.module.banking.data.fiscalyear.start
import org.solyton.solawi.bid.module.bid.component.styles.auctionModalStyles
import org.solyton.solawi.bid.module.shares.data.management.ShareManagement
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.w3c.dom.HTMLElement
import kotlin.time.Duration.Companion.days


@Markup
@Suppress("FunctionName")
fun UpsertFiscalYearsModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    storage: Storage<BankingApplication>,
    device: Source<DeviceType>,
    fiscalYears: List<FiscalYear>,
    fiscalYear: FiscalYear?,
    setFiscalYear: (FiscalYear)->Unit,
    update: ()->Unit
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    id,
    modals,
    storage * deviceData * mediaType.get,
    onOk = {
        update()
    },
    onCancel = {},
    texts = texts,
    styles = auctionModalStyles(device),
) {
    var fiscalYear by remember { mutableStateOf< FiscalYear?>(fiscalYear) }
    val forbiddenTimeIntervals = when(val fY = fiscalYear) {
        null -> fiscalYears
        else -> fiscalYears.filter { it.fiscalYearId != fY.fiscalYearId }
    }.fold(DateInterval()){ interval, date ->
        val start = when{
            interval.start <= date.start -> interval.start
            else -> date.start
        }
        val end = when{
            interval.end >= date.end -> interval.end
            else-> date.end
        }
        DateInterval(start, end)
    }.let {
        when{
            fiscalYear == null -> listOf(it)
            else -> it.cutOff(DateInterval(fiscalYear!!.start, fiscalYear!!.end))
        }
    }

    val guessedNextFiscalYear: DateInterval = when(val fY = fiscalYear) {
        null -> with(forbiddenTimeIntervals.last()) last@{
            DateInterval(this@last.end.plus(DatePeriod(days = 1)), this@last.end.plus(DatePeriod(years = 1)))
        }
        else -> DateInterval(fY.start, fY.end)
    }

    Wrap{
        H4{ Text("Constraints on fiscal years") }
        P{ Text("No overlaps with other fiscal years allowed: Forbidden time intervals:") }
        P{ Text(forbiddenTimeIntervals.joinToString(separator = ", ") { interval ->
            "[${interval.start}, ${interval.end}]"
        }) }
    }
    Form {
        Field(fieldDesktopStyle) {
            Label(
                "Start Date",
                id = "start-of-fiscal-year",
                labelStyle = formLabelDesktopStyle,
                isRequired = true
            )

            key(fiscalYear?.start) {
                val dateString = fiscalYear?.start?.toString() ?: guessedNextFiscalYear.start.toString()
                Input(InputType.Date) {
                    id("start-date")
                    dataId("upsert-fiscal-year.form.input.date.start")
                    value(dateString )
                    style { dateInputDesktopStyle() }
                    onInput {
                        val start = it.value.parse(Locale.Iso).toDateTime().date
                        fiscalYear = (fiscalYear ?: FiscalYear.default).copy(start = start)
                        setFiscalYear(fiscalYear!!)
                    }
                }
            }
        }

        Field(fieldDesktopStyle) {
            Label(
                "End Date",
                id = "end-of-fiscal-year",
                labelStyle = formLabelDesktopStyle,
                isRequired = true
            )
            // State
            key(fiscalYear?.end) {
                val dateString = fiscalYear?.end?.toString() ?: guessedNextFiscalYear.start.toString()
                Input(InputType.Date) {
                    id("end-date")
                    dataId("upsert-fiscal-year.form.input.date.end")
                    value(dateString)
                    style { dateInputDesktopStyle() }
                    onInput {
                        val end = it.value.parse(Locale.Iso).toDateTime().date
                        fiscalYear = (fiscalYear ?: FiscalYear.default).copy(end = end)
                        setFiscalYear(fiscalYear!!)
                    }
                }
            }
        }
    }
}

@Markup
fun Storage<Modals<Int>>.showUpsertFiscalYearsModal(
    storage: Storage<BankingApplication>,
    texts: Lang.Block,
    device: Source<DeviceType>,
    fiscalYears: List<FiscalYear>,
    fiscalYear: FiscalYear?,
    setFiscalYear: (FiscalYear)->Unit = {},
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        UpsertFiscalYearsModal(
            this,
            texts,
            this@showUpsertFiscalYearsModal,
            storage,
            device,
            fiscalYears,
            fiscalYear,
            setFiscalYear,
            update = update
        )
    ) )
}
