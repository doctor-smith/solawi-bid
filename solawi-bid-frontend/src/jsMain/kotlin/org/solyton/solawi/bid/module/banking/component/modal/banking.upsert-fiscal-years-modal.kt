package org.solyton.solawi.bid.module.banking.component.modal

import androidx.compose.runtime.*
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.evoleq.compose.Markup
import org.evoleq.compose.attribute.dataId
import org.evoleq.compose.date.parse
import org.evoleq.compose.form.field.Field
import org.evoleq.compose.form.label.Label
import org.evoleq.compose.modal.Modal
import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.modal.Modals
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.device.data.mediaType
import org.evoleq.kotlinx.date.data.DateInterval
import org.evoleq.kotlinx.date.data.contains
import org.evoleq.kotlinx.date.toDateTime
import org.evoleq.kotlinx.date.today
import org.evoleq.language.Lang
import org.evoleq.language.Locale
import org.evoleq.math.Source
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.dom.*
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.deviceData
import org.solyton.solawi.bid.module.banking.data.fiscalyear.FiscalYear
import org.solyton.solawi.bid.module.style.form.dateInputDesktopStyle
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.style.modal.commonModalStyles
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.w3c.dom.HTMLElement


@Markup
@Suppress("FunctionName")
fun UpsertFiscalYearsModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    storage: Storage<BankingApplication>,
    device: Source<DeviceType>,
    isOkButtonDisabled:  () -> Boolean ,
    fiscalYears: List<FiscalYear>,
    fiscalYear: FiscalYear?,
    setFiscalYear: (FiscalYear)->Unit,
    update: ()->Unit
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    type = ModalType.Dialog,
    id = id,
    modals = modals,
    device = storage * deviceData * mediaType.get,
    isOkButtonDisabled = isOkButtonDisabled,
    onOk = {
        update()
    },
    onCancel = {},
    texts = texts,
    styles = commonModalStyles(device),
    dataId = "banking.upsert-fiscal-years"
) {
    val forbiddenTimeIntervals = when{
        fiscalYears.isEmpty() -> emptyList<DateInterval>()
        else -> when(val fY = fiscalYear) {
            null -> fiscalYears
            else -> fiscalYears.filter { it.fiscalYearId != fY.fiscalYearId }
        }.map{ y -> DateInterval(y.start, y.end) }
    }

    val guessedNextFiscalYear: DateInterval = when(val fY = fiscalYear) {
        null -> with(forbiddenTimeIntervals.lastOrNull()) last@{
            this@last?.let{DateInterval(this@last.end.plus(DatePeriod(days = 1)), this@last.end.plus(DatePeriod(years = 1)))}
                ?: DateInterval(today(), today().plus(DatePeriod(years = 1)))
        }
        else -> DateInterval(fY.start, fY.end)
    }

    var startState by remember { mutableStateOf<LocalDate>(fiscalYear?.start?: guessedNextFiscalYear.start) }
    var endState by remember { mutableStateOf<LocalDate>(fiscalYear?.end?: guessedNextFiscalYear.end) }

    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(fiscalYear) {
        if(fiscalYear == null) {
            setFiscalYear(FiscalYear.default.copy(start = startState, end = endState))
        }
    }

    Wrap{
        H4{ Text("Constraints on fiscal years") }
        P{ Text("No overlaps with other fiscal years allowed: Forbidden time intervals:") }
        P{ Text(forbiddenTimeIntervals.joinToString(separator = ", ") { interval ->
            "[${interval.start}, ${interval.end}]"
        }) }

        if(error != null) P({style { color(Color.red) }}) { Text("$error") }
    }
    Form {
        Field(fieldDesktopStyle) {
            Label(
                "Start Date",
                id = "start-of-fiscal-year",
                labelStyle = formLabelDesktopStyle,
                isRequired = true
            )

            key(startState, endState) {
                val dateString = startState.toString()
                Input(InputType.Date) {
                    id("start-date")
                    dataId("upsert-fiscal-year.form.input.date.start")
                    value(dateString)
                    style { dateInputDesktopStyle() }
                    onInput {
                        val start = it.value.parse(Locale.Iso).toDateTime().date
                        startState = start
                        error = isValid(start, endState, forbiddenTimeIntervals)?.toString()
                        if( error == null) {
                            setFiscalYear(fiscalYear?: FiscalYear.default.copy(start = start, end = endState!!))
                        }
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
            key(startState, endState) {
                val dateString = endState.toString()
                Input(InputType.Date) {
                    id("end-date")
                    dataId("upsert-fiscal-year.form.input.date.end")
                    value(dateString)
                    style { dateInputDesktopStyle() }
                    onInput {
                        val end = it.value.parse(Locale.Iso).toDateTime().date
                        endState = end
                        error = isValid(startState, end, forbiddenTimeIntervals)?.toString()
                        if( error == null) {
                            setFiscalYear(fiscalYear?: FiscalYear.default.copy(start = startState, end = end))
                        }
                    }
                }
            }
        }
    }
}

sealed class FiscalYearError {
    object NoStartDate: FiscalYearError()
    object NoEndDate: FiscalYearError()
    object EndBeforeStart: FiscalYearError()
    object DurationTooLong: FiscalYearError()
    object Overlap: FiscalYearError()

    override fun toString(): String = when(this){
        is NoStartDate -> "No start date"
        is NoEndDate -> "No end date"
        is EndBeforeStart -> "End date before start date"
        is DurationTooLong -> "Duration too long"
        is Overlap -> "Overlapping fiscal years"
    }
}

fun isValid(
    start: LocalDate?,
    end: LocalDate?,
    forbidden: List<DateInterval>
): FiscalYearError? =
    when{
        start == null -> FiscalYearError.NoStartDate
        end == null -> FiscalYearError.NoEndDate
        end <= start -> FiscalYearError.EndBeforeStart
        forbidden.any { it.contains(start) or it.contains(end) } -> FiscalYearError.Overlap
        end.year > start.year + 1 -> FiscalYearError.DurationTooLong
        else -> null
    }


@Markup
fun Storage<Modals<Int>>.showUpsertFiscalYearsModal(
    storage: Storage<BankingApplication>,
    texts: Lang.Block,
    device: Source<DeviceType>,
    isOkButtonDisabled: () -> Boolean,
    fiscalYears: List<FiscalYear>,
    fiscalYear: FiscalYear?,
    setFiscalYear: (FiscalYear)->Unit = {},
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(this,
        ModalType.Dialog,
        UpsertFiscalYearsModal(
            this,
            texts,
            this@showUpsertFiscalYearsModal,
            storage,
            device,
            isOkButtonDisabled,
            fiscalYears,
            fiscalYear,
            setFiscalYear,
            update = update
        )
    ) )
}
