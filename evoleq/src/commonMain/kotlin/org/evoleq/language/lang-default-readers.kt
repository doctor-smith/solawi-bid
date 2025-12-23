package org.evoleq.language

import org.evoleq.math.Reader


val date: Reader<Lang.Block, String> = Reader { lang -> lang["date"] }
val description: Reader<Lang.Block, String> = Reader { lang -> lang["description"] }
val subTitle: Reader<Lang.Block, String> = Reader { lang -> lang["subTitle"] }
val title: Reader<Lang.Block, String> = Reader { lang -> lang["title"] }
val text: Reader<Lang.Block, String> = Reader { lang -> lang["text"] }
val tooltip: Reader<Lang.Block, String> = Reader{ lang -> lang["tooltip"]}
