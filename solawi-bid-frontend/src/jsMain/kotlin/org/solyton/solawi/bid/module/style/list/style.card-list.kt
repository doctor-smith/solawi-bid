package org.solyton.solawi.bid.module.style.list

import org.jetbrains.compose.web.css.paddingLeft
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.solyton.solawi.bid.module.list.style.defaultListStyles


val cardListStyles = defaultListStyles.modifyHeader {
    paddingLeft(10.px)
}.modifyDataWrapper {
    paddingLeft(10.px)
}.modifyTitleWrapper {
    width(100.percent)
}
