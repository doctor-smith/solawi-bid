package org.solyton.solawi.bid.module.application.i18n

import org.evoleq.language.Lang
import org.evoleq.language.subComp
import org.evoleq.math.Reader

val name: Reader<Lang.Block, Lang.Block> = subComp("name")

val inputs: Reader<Lang.Block, Lang.Block> = subComp("inputs")
