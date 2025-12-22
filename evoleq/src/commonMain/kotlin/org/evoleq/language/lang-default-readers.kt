package org.evoleq.language

import org.evoleq.math.Reader

val title: Reader<Lang.Block, String> = Reader { lang -> lang["title"] }
val subTitle: Reader<Lang.Block, String> = Reader { lang -> lang["subTitle"] }
val date: Reader<Lang.Block, String> = Reader { lang -> lang["date"] }
val text: Reader<Lang.Block, String> = Reader { lang -> lang["text"] }
val tooltip: Reader<Lang.Block, String> = Reader{ lang -> lang["tooltip"]}
