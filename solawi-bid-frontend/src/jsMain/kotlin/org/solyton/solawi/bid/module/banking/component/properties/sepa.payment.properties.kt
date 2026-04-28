package org.solyton.solawi.bid.module.banking.component.properties

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.PropertiesStyles
import org.evoleq.compose.layout.Property
import org.evoleq.compose.layout.ReadOnlyProperties
import org.jetbrains.compose.web.css.flexGrow
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.solyton.solawi.bid.module.banking.data.sepa.payment.SepaPayment

@Markup
@Composable
@Suppress("FunctionName")
fun PaymentsProperties(payments: List<SepaPayment>) =
    ReadOnlyProperties(listOf(
        Property("Number", payments.size),
        Property("Total Amount", payments.sumOf { it.amount })
    ),
        PropertiesStyles().modifyContainerStyle {
            flexGrow(1)
        }.modifyPropertyStyles {
            modifyKeyStyle {
                width(50.percent)
            }.modifyValueStyle {
                width(50.percent)
            }
        }
    )

